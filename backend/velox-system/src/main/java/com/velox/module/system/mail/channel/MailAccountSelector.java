package com.velox.module.system.mail.channel;

import com.velox.module.system.mail.domain.model.MailAccount;
import com.velox.module.system.mail.domain.model.MailChannel;
import com.velox.module.system.mail.domain.model.MailGroup;
import com.velox.module.system.mail.persistence.MailAccountMapper;
import com.velox.module.system.mail.persistence.MailChannelMapper;
import com.velox.module.system.mail.persistence.MailGroupMapper;
import com.velox.module.system.mail.support.MailHealthStatus;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 发件邮箱选择器
 * <p>
 * 负责从发件池中按权重挑选可用邮箱，并在发送结果回写后维护健康状态机。
 * <ul>
 *   <li>可用判定：启用 + 分组生效 + 渠道生效 + 非死亡 +（正常 或 异常且已过重试时间）。</li>
 *   <li>加权挑选：选择 usage_count × weight 最小者（权重数字越低越优先）。该“负载最小”策略形成保底——
 *       高权重（数字小）邮箱的累计使用次数始终不少于低权重邮箱，低权重邮箱被使用次数不可能超过高权重邮箱。</li>
 *   <li>失败降级：连续失败达到阈值 → 标记异常（累计 +1，安排下次重试）；累计达到上限 → 标记死亡（不可自动恢复）。</li>
 *   <li>成功恢复：清零连续失败计数；若处于异常（重试成功）则恢复为正常。</li>
 * </ul>
 */
@Component
public class MailAccountSelector {

    private static final int DEFAULT_WEIGHT = 100;
    private static final int DEFAULT_FAIL_THRESHOLD = 3;
    private static final int DEFAULT_RETRY_INTERVAL = 300;
    private static final int DEFAULT_MAX_UNAVAILABLE = 5;

    private final MailAccountMapper mailAccountMapper;
    private final MailGroupMapper mailGroupMapper;
    private final MailChannelMapper mailChannelMapper;

    public MailAccountSelector(MailAccountMapper mailAccountMapper,
                               MailGroupMapper mailGroupMapper,
                               MailChannelMapper mailChannelMapper) {
        this.mailAccountMapper = mailAccountMapper;
        this.mailGroupMapper = mailGroupMapper;
        this.mailChannelMapper = mailChannelMapper;
    }

    /**
     * 选择下一个可用发件邮箱，排除本次发送已尝试过的账号；无可用时返回 {@code null}。
     */
    public MailAccount selectNext(Collection<String> excludedIds) {
        Set<String> excluded = excludedIds == null ? Set.of() : new HashSet<>(excludedIds);
        Set<String> activeGroupIds = activeGroupIds();
        Set<String> activeChannelIds = activeChannelIds();
        if (activeGroupIds.isEmpty() || activeChannelIds.isEmpty()) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        List<MailAccount> candidates = mailAccountMapper.selectList(new LambdaQueryWrapper<MailAccount>()
                .eq(MailAccount::getEnabled, 1)
                .ne(MailAccount::getHealthStatus, MailHealthStatus.DEAD));
        return candidates.stream()
                .filter(account -> !excluded.contains(account.getId()))
                .filter(account -> activeGroupIds.contains(account.getGroupId()))
                .filter(account -> activeChannelIds.contains(account.getChannelId()))
                .filter(account -> isTriable(account, now))
                .min(loadComparator())
                .orElse(null);
    }

    /**
     * 记录一次成功发送：使用次数 +1、清零连续失败；异常经重试成功则恢复为正常。
     */
    public void onSuccess(String accountId) {
        MailAccount account = mailAccountMapper.selectById(accountId);
        if (account == null) {
            return;
        }
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        LambdaUpdateWrapper<MailAccount> wrapper = new LambdaUpdateWrapper<MailAccount>()
                .eq(MailAccount::getId, accountId)
                .setSql("usage_count = usage_count + 1")
                .set(MailAccount::getFailCount, 0)
                .set(MailAccount::getLastUsedTime, now)
                .set(MailAccount::getUpdateTime, now);
        if (currentStatus(account) == MailHealthStatus.UNAVAILABLE) {
            wrapper.set(MailAccount::getHealthStatus, MailHealthStatus.NORMAL);
            wrapper.set(MailAccount::getNextRetryTime, null);
        }
        mailAccountMapper.update(null, wrapper);
    }

    /**
     * 记录一次失败发送并推进健康状态机。
     */
    public void onFailure(String accountId) {
        MailAccount account = mailAccountMapper.selectById(accountId);
        if (account == null) {
            return;
        }
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        int failThreshold = intValue(account.getFailThreshold(), DEFAULT_FAIL_THRESHOLD, 1);
        int maxUnavailable = intValue(account.getMaxUnavailable(), DEFAULT_MAX_UNAVAILABLE, 1);
        int retryInterval = intValue(account.getRetryInterval(), DEFAULT_RETRY_INTERVAL, 0);
        int currentStatus = currentStatus(account);
        int failCount = intValue(account.getFailCount(), 0, 0) + 1;
        int unavailableCount = intValue(account.getUnavailableCount(), 0, 0);

        LambdaUpdateWrapper<MailAccount> wrapper = new LambdaUpdateWrapper<MailAccount>()
                .eq(MailAccount::getId, accountId)
                .set(MailAccount::getLastUsedTime, now)
                .set(MailAccount::getUpdateTime, now);

        if (failCount >= failThreshold) {
            // 连续失败达到阈值：累计一次“异常”，重置连续失败计数
            unavailableCount += 1;
            wrapper.set(MailAccount::getFailCount, 0);
            wrapper.set(MailAccount::getUnavailableCount, unavailableCount);
            if (unavailableCount >= maxUnavailable) {
                wrapper.set(MailAccount::getHealthStatus, MailHealthStatus.DEAD);
                wrapper.set(MailAccount::getNextRetryTime, null);
            } else {
                wrapper.set(MailAccount::getHealthStatus, MailHealthStatus.UNAVAILABLE);
                wrapper.set(MailAccount::getNextRetryTime, now.plusSeconds(retryInterval));
            }
        } else {
            wrapper.set(MailAccount::getFailCount, failCount);
            // 处于异常的试用期内再次失败但未达阈值：顺延下次重试时间，避免频繁试探
            if (currentStatus == MailHealthStatus.UNAVAILABLE) {
                wrapper.set(MailAccount::getNextRetryTime, now.plusSeconds(retryInterval));
            }
        }
        mailAccountMapper.update(null, wrapper);
    }

    private boolean isTriable(MailAccount account, LocalDateTime now) {
        int status = currentStatus(account);
        if (status == MailHealthStatus.NORMAL) {
            return true;
        }
        if (status == MailHealthStatus.UNAVAILABLE) {
            LocalDateTime nextRetry = account.getNextRetryTime();
            return nextRetry == null || !now.isBefore(nextRetry);
        }
        return false;
    }

    private Comparator<MailAccount> loadComparator() {
        return Comparator.comparingLong(this::load)
                .thenComparingInt(account -> intValue(account.getWeight(), DEFAULT_WEIGHT, 1))
                .thenComparing(MailAccount::getLastUsedTime, Comparator.nullsFirst(Comparator.naturalOrder()))
                .thenComparing(MailAccount::getId, Comparator.nullsLast(Comparator.naturalOrder()));
    }

    private long load(MailAccount account) {
        long usage = account.getUsageCount() == null ? 0L : account.getUsageCount();
        long weight = intValue(account.getWeight(), DEFAULT_WEIGHT, 1);
        return usage * weight;
    }

    private Set<String> activeGroupIds() {
        return new HashSet<>(mailGroupMapper.selectObjs(new LambdaQueryWrapper<MailGroup>()
                .select(MailGroup::getId)
                .eq(MailGroup::getActive, 1))
                .stream().map(String::valueOf).toList());
    }

    private Set<String> activeChannelIds() {
        return new HashSet<>(mailChannelMapper.selectObjs(new LambdaQueryWrapper<MailChannel>()
                .select(MailChannel::getId)
                .eq(MailChannel::getActive, 1))
                .stream().map(String::valueOf).toList());
    }

    private int currentStatus(MailAccount account) {
        return account.getHealthStatus() == null ? MailHealthStatus.NORMAL : account.getHealthStatus();
    }

    private int intValue(Integer value, int defaultValue, int min) {
        int resolved = value == null ? defaultValue : value;
        return Math.max(resolved, min);
    }
}
