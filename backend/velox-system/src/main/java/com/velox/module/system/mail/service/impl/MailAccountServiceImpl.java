package com.velox.module.system.mail.service.impl;

import cn.hutool.core.util.StrUtil;
import com.velox.common.exception.ApiException;
import com.velox.common.exception.BusinessErrorCode;
import com.velox.common.exception.MessageUtils;
import com.velox.common.result.PageResult;
import com.velox.email.api.message.SendRequest;
import com.velox.email.api.message.SendResponse;
import com.velox.framework.web.RequestDateTimeFormatter;
import com.velox.module.system.id.frontend.SystemFrontendIdCodecSupport;
import com.velox.module.system.id.generator.SystemEntityIdGenerator;
import com.velox.module.system.mail.channel.MailSenderFactory;
import com.velox.module.system.mail.domain.model.MailAccount;
import com.velox.module.system.mail.domain.model.MailChannel;
import com.velox.module.system.mail.domain.model.MailGroup;
import com.velox.module.system.mail.persistence.MailAccountMapper;
import com.velox.module.system.mail.persistence.MailChannelMapper;
import com.velox.module.system.mail.persistence.MailGroupMapper;
import com.velox.module.system.mail.service.MailAccountService;
import com.velox.module.system.mail.support.MailHealthStatus;
import com.velox.module.system.mail.vo.MailAccountPageReqVO;
import com.velox.module.system.mail.vo.MailAccountRespVO;
import com.velox.module.system.mail.vo.MailAccountSaveReqVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Validated
public class MailAccountServiceImpl implements MailAccountService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private static final int DEFAULT_WEIGHT = 100;
    private static final int DEFAULT_FAIL_THRESHOLD = 3;
    private static final int DEFAULT_RETRY_INTERVAL = 300;
    private static final int DEFAULT_MAX_UNAVAILABLE = 5;

    private final MailAccountMapper mailAccountMapper;
    private final MailGroupMapper mailGroupMapper;
    private final MailChannelMapper mailChannelMapper;
    private final SystemEntityIdGenerator entityIdGenerator;
    private final SystemFrontendIdCodecSupport frontendIdCodecSupport;
    private final MailSenderFactory mailSenderFactory;

    public MailAccountServiceImpl(MailAccountMapper mailAccountMapper,
                                  MailGroupMapper mailGroupMapper,
                                  MailChannelMapper mailChannelMapper,
                                  SystemEntityIdGenerator entityIdGenerator,
                                  SystemFrontendIdCodecSupport frontendIdCodecSupport,
                                  MailSenderFactory mailSenderFactory) {
        this.mailAccountMapper = mailAccountMapper;
        this.mailGroupMapper = mailGroupMapper;
        this.mailChannelMapper = mailChannelMapper;
        this.entityIdGenerator = entityIdGenerator;
        this.frontendIdCodecSupport = frontendIdCodecSupport;
        this.mailSenderFactory = mailSenderFactory;
    }

    @Override
    public String createMailAccount(MailAccountSaveReqVO createReqVO) {
        String groupId = frontendIdCodecSupport.decodeIdentifier(createReqVO.getGroupId());
        String channelId = frontendIdCodecSupport.decodeIdentifier(createReqVO.getChannelId());
        validateGroupExists(groupId);
        validateChannelExists(channelId);

        MailAccount account = new MailAccount();
        account.setId(entityIdGenerator.nextId(MailAccount.class));
        account.setName(createReqVO.getName());
        account.setGroupId(groupId);
        account.setChannelId(channelId);
        account.setUsername(createReqVO.getUsername());
        account.setPassword(createReqVO.getPassword());
        account.setFromAddress(createReqVO.getFromAddress());
        account.setFromName(createReqVO.getFromName());
        account.setHost(createReqVO.getHost());
        account.setPort(createReqVO.getPort());
        account.setSslEnabled(createReqVO.getSslEnabled());
        account.setStarttls(createReqVO.getStarttls());
        account.setWeight(createReqVO.getWeight() != null ? createReqVO.getWeight() : DEFAULT_WEIGHT);
        account.setFailThreshold(createReqVO.getFailThreshold() != null
                ? createReqVO.getFailThreshold() : DEFAULT_FAIL_THRESHOLD);
        account.setRetryInterval(createReqVO.getRetryInterval() != null
                ? createReqVO.getRetryInterval() : DEFAULT_RETRY_INTERVAL);
        account.setMaxUnavailable(createReqVO.getMaxUnavailable() != null
                ? createReqVO.getMaxUnavailable() : DEFAULT_MAX_UNAVAILABLE);
        account.setHealthStatus(MailHealthStatus.NORMAL);
        account.setUsageCount(0L);
        account.setFailCount(0);
        account.setUnavailableCount(0);
        account.setEnabled(createReqVO.getEnabled() != null ? createReqVO.getEnabled() : 1);
        account.setRemark(createReqVO.getRemark());
        mailAccountMapper.insert(account);
        return account.getId();
    }

    @Override
    public void updateMailAccount(MailAccountSaveReqVO updateReqVO) {
        String decodedId = frontendIdCodecSupport.decodeIdentifier(updateReqVO.getId());
        validateAccountExists(decodedId);
        String groupId = frontendIdCodecSupport.decodeIdentifier(updateReqVO.getGroupId());
        String channelId = frontendIdCodecSupport.decodeIdentifier(updateReqVO.getChannelId());
        validateGroupExists(groupId);
        validateChannelExists(channelId);

        LambdaUpdateWrapper<MailAccount> wrapper = new LambdaUpdateWrapper<MailAccount>()
                .eq(MailAccount::getId, decodedId)
                .set(MailAccount::getName, updateReqVO.getName())
                .set(MailAccount::getGroupId, groupId)
                .set(MailAccount::getChannelId, channelId)
                .set(MailAccount::getUsername, updateReqVO.getUsername())
                .set(MailAccount::getFromAddress, updateReqVO.getFromAddress())
                .set(MailAccount::getFromName, updateReqVO.getFromName())
                .set(MailAccount::getHost, updateReqVO.getHost())
                .set(MailAccount::getPort, updateReqVO.getPort())
                .set(MailAccount::getSslEnabled, updateReqVO.getSslEnabled())
                .set(MailAccount::getStarttls, updateReqVO.getStarttls())
                .set(MailAccount::getWeight, updateReqVO.getWeight() != null
                        ? updateReqVO.getWeight() : DEFAULT_WEIGHT)
                .set(MailAccount::getFailThreshold, updateReqVO.getFailThreshold() != null
                        ? updateReqVO.getFailThreshold() : DEFAULT_FAIL_THRESHOLD)
                .set(MailAccount::getRetryInterval, updateReqVO.getRetryInterval() != null
                        ? updateReqVO.getRetryInterval() : DEFAULT_RETRY_INTERVAL)
                .set(MailAccount::getMaxUnavailable, updateReqVO.getMaxUnavailable() != null
                        ? updateReqVO.getMaxUnavailable() : DEFAULT_MAX_UNAVAILABLE)
                .set(MailAccount::getEnabled, updateReqVO.getEnabled() != null ? updateReqVO.getEnabled() : 1)
                .set(MailAccount::getRemark, updateReqVO.getRemark())
                .set(MailAccount::getUpdateTime, LocalDateTime.now(ZoneOffset.UTC));
        // 密码留空表示保留原值，仅在填写新密码时覆盖
        if (StrUtil.isNotBlank(updateReqVO.getPassword())) {
            wrapper.set(MailAccount::getPassword, updateReqVO.getPassword());
        }
        mailAccountMapper.update(null, wrapper);
        mailSenderFactory.invalidate(decodedId);
    }

    @Override
    public void updateMailAccountEnabled(String id, Integer enabled) {
        String decodedId = frontendIdCodecSupport.decodeIdentifier(id);
        validateAccountExists(decodedId);
        mailAccountMapper.update(null, new LambdaUpdateWrapper<MailAccount>()
                .eq(MailAccount::getId, decodedId)
                .set(MailAccount::getEnabled, enabled)
                .set(MailAccount::getUpdateTime, LocalDateTime.now(ZoneOffset.UTC)));
    }

    @Override
    public void deleteMailAccount(String id) {
        String decodedId = frontendIdCodecSupport.decodeIdentifier(id);
        validateAccountExists(decodedId);
        mailAccountMapper.deleteById(decodedId);
        mailSenderFactory.invalidate(decodedId);
    }

    @Override
    public void deleteMailAccountList(List<String> ids) {
        List<String> decodedIds = frontendIdCodecSupport.decodeIdentifiers(ids);
        if (decodedIds.isEmpty()) {
            return;
        }
        mailAccountMapper.deleteByIds(decodedIds);
        decodedIds.forEach(mailSenderFactory::invalidate);
    }

    @Override
    public MailAccountRespVO getMailAccount(String id) {
        String decodedId = frontendIdCodecSupport.decodeIdentifier(id);
        MailAccount account = mailAccountMapper.selectById(decodedId);
        if (account == null) {
            return null;
        }
        return toRespVO(account, loadGroupMap(), loadChannelMap());
    }

    @Override
    public PageResult<MailAccountRespVO> getMailAccountPage(MailAccountPageReqVO pageReqVO) {
        String groupId = frontendIdCodecSupport.decodeIdentifier(pageReqVO.getGroupId());
        String channelId = frontendIdCodecSupport.decodeIdentifier(pageReqVO.getChannelId());
        LambdaQueryWrapper<MailAccount> wrapper = new LambdaQueryWrapper<MailAccount>()
                .like(StrUtil.isNotEmpty(pageReqVO.getName()), MailAccount::getName, pageReqVO.getName())
                .eq(StrUtil.isNotEmpty(groupId), MailAccount::getGroupId, groupId)
                .eq(StrUtil.isNotEmpty(channelId), MailAccount::getChannelId, channelId)
                .eq(pageReqVO.getHealthStatus() != null, MailAccount::getHealthStatus, pageReqVO.getHealthStatus())
                .eq(pageReqVO.getEnabled() != null, MailAccount::getEnabled, pageReqVO.getEnabled())
                .ge(StrUtil.isNotBlank(pageReqVO.getCreateTimeStart()), MailAccount::getCreateTime,
                        RequestDateTimeFormatter.parseToUtc(pageReqVO.getCreateTimeStart()))
                .le(StrUtil.isNotBlank(pageReqVO.getCreateTimeEnd()), MailAccount::getCreateTime,
                        RequestDateTimeFormatter.parseToUtc(pageReqVO.getCreateTimeEnd()))
                .ge(StrUtil.isNotBlank(pageReqVO.getUpdateTimeStart()), MailAccount::getUpdateTime,
                        RequestDateTimeFormatter.parseToUtc(pageReqVO.getUpdateTimeStart()))
                .le(StrUtil.isNotBlank(pageReqVO.getUpdateTimeEnd()), MailAccount::getUpdateTime,
                        RequestDateTimeFormatter.parseToUtc(pageReqVO.getUpdateTimeEnd()))
                .orderByDesc(MailAccount::getCreateTime)
                .orderByDesc(MailAccount::getId);
        Page<MailAccount> page = mailAccountMapper.selectPage(
                new Page<>(pageReqVO.getPage(), pageReqVO.getSize()), wrapper);
        Map<String, MailGroup> groupMap = loadGroupMap();
        Map<String, MailChannel> channelMap = loadChannelMap();
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(),
                page.getRecords().stream()
                        .map(account -> toRespVO(account, groupMap, channelMap))
                        .collect(Collectors.toList()));
    }

    @Override
    public void recoverMailAccount(String id) {
        String decodedId = frontendIdCodecSupport.decodeIdentifier(id);
        validateAccountExists(decodedId);
        mailAccountMapper.update(null, new LambdaUpdateWrapper<MailAccount>()
                .eq(MailAccount::getId, decodedId)
                .set(MailAccount::getHealthStatus, MailHealthStatus.NORMAL)
                .set(MailAccount::getFailCount, 0)
                .set(MailAccount::getUnavailableCount, 0)
                .set(MailAccount::getNextRetryTime, null)
                .set(MailAccount::getUpdateTime, LocalDateTime.now(ZoneOffset.UTC)));
    }

    @Override
    public void testMailAccount(String id, String toEmail) {
        String decodedId = frontendIdCodecSupport.decodeIdentifier(id);
        MailAccount account = validateAccountExists(decodedId);
        String target = StrUtil.trimToEmpty(toEmail);
        if (!EMAIL_PATTERN.matcher(target).matches()) {
            throw new ApiException(BusinessErrorCode.MAIL_TEST_EMAIL_INVALID);
        }
        SendRequest request = SendRequest.builder()
                .to(List.of(target))
                .subject(MessageUtils.message("mail.account.test.subject"))
                .html(MessageUtils.message("mail.account.test.body", account.getName()))
                .maxAttempts(1)
                .async(false)
                .build();
        SendResponse response;
        try {
            response = mailSenderFactory.send(account, request);
        } catch (Exception exception) {
            throw new ApiException(exception, BusinessErrorCode.MAIL_ACCOUNT_TEST_FAILED,
                    StrUtil.blankToDefault(exception.getMessage(), exception.getClass().getSimpleName()));
        }
        if (response == null || !response.success()) {
            String reason = response == null ? "unknown" : StrUtil.blankToDefault(response.error(), "unknown");
            throw new ApiException(BusinessErrorCode.MAIL_ACCOUNT_TEST_FAILED, reason);
        }
    }

    private MailAccount validateAccountExists(String id) {
        MailAccount account = mailAccountMapper.selectById(id);
        if (account == null) {
            throw new ApiException(BusinessErrorCode.MAIL_ACCOUNT_NOT_FOUND);
        }
        return account;
    }

    private void validateGroupExists(String groupId) {
        if (mailGroupMapper.selectById(groupId) == null) {
            throw new ApiException(BusinessErrorCode.MAIL_GROUP_NOT_FOUND);
        }
    }

    private void validateChannelExists(String channelId) {
        if (mailChannelMapper.selectById(channelId) == null) {
            throw new ApiException(BusinessErrorCode.MAIL_CHANNEL_NOT_FOUND);
        }
    }

    private Map<String, MailGroup> loadGroupMap() {
        return mailGroupMapper.selectList(null).stream()
                .collect(Collectors.toMap(MailGroup::getId, Function.identity(), (left, right) -> left, HashMap::new));
    }

    private Map<String, MailChannel> loadChannelMap() {
        return mailChannelMapper.selectList(null).stream()
                .collect(Collectors.toMap(MailChannel::getId, Function.identity(), (left, right) -> left, HashMap::new));
    }

    private MailAccountRespVO toRespVO(MailAccount account,
                                       Map<String, MailGroup> groupMap,
                                       Map<String, MailChannel> channelMap) {
        MailAccountRespVO respVO = new MailAccountRespVO();
        respVO.setId(account.getId());
        respVO.setName(account.getName());
        respVO.setGroupId(account.getGroupId());
        MailGroup group = groupMap.get(account.getGroupId());
        respVO.setGroupName(group != null ? group.getName() : null);
        respVO.setChannelId(account.getChannelId());
        MailChannel channel = channelMap.get(account.getChannelId());
        respVO.setChannelName(channel != null ? channel.getName() : null);
        respVO.setProtocol(channel != null ? channel.getProtocol() : null);
        respVO.setUsername(account.getUsername());
        respVO.setPasswordSet(StrUtil.isNotBlank(account.getPassword()));
        respVO.setFromAddress(account.getFromAddress());
        respVO.setFromName(account.getFromName());
        respVO.setHost(account.getHost());
        respVO.setPort(account.getPort());
        respVO.setSslEnabled(account.getSslEnabled());
        respVO.setStarttls(account.getStarttls());
        respVO.setWeight(account.getWeight());
        respVO.setFailThreshold(account.getFailThreshold());
        respVO.setRetryInterval(account.getRetryInterval());
        respVO.setMaxUnavailable(account.getMaxUnavailable());
        respVO.setHealthStatus(account.getHealthStatus());
        respVO.setUsageCount(account.getUsageCount());
        respVO.setFailCount(account.getFailCount());
        respVO.setUnavailableCount(account.getUnavailableCount());
        respVO.setNextRetryTime(RequestDateTimeFormatter.format(account.getNextRetryTime()));
        respVO.setLastUsedTime(RequestDateTimeFormatter.format(account.getLastUsedTime()));
        respVO.setEnabled(account.getEnabled());
        respVO.setRemark(account.getRemark());
        respVO.setCreateTime(RequestDateTimeFormatter.format(account.getCreateTime()));
        respVO.setUpdateTime(RequestDateTimeFormatter.format(account.getUpdateTime()));
        return respVO;
    }
}
