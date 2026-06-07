package com.velox.module.system.verification.enforce;

import com.velox.module.system.verification.common.EffectivePolicy;
import com.velox.module.system.verification.service.VerificationPolicyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 统一验证策略限制器（Redis）。
 *
 * <p>把「登录失败锁定 / 验证码失败 / 接口限流 / 发码频率 / MFA 失败」统一为一个原语：
 * 每个启用维度（账号、IP）累计计数，达到 maxAttempts 即锁定 recoverySeconds。
 * <ul>
 *   <li>失败计数场景（login/verify_code/mfa）：尝试前 {@link #isLocked} 判定；失败 {@link #recordFailure}；成功 {@link #reset}。</li>
 *   <li>请求计数场景（captcha/send_code）：每次请求前 {@link #isLocked}，随后 {@link #recordFailure}（不 reset）。</li>
 * </ul>
 *
 * <p><b>强可用：所有 Redis 调用 fail-open</b>（异常时按"未锁/放行"处理 + 告警），
 * 绝不让限制器自身把登录打挂——与 RedisRateLimiter 一致。
 */
@Component
public class VerificationPolicyEnforcer {

    private static final Logger log = LoggerFactory.getLogger(VerificationPolicyEnforcer.class);

    private static final String DIM_ACCOUNT = "acct";
    private static final String DIM_IP = "ip";

    /** KEYS=[cntKey, lockKey] ARGV=[maxAttempts, recoverySeconds]；达上限即加锁并清计数，返回是否刚锁定。 */
    private static final DefaultRedisScript<Long> RECORD_SCRIPT = new DefaultRedisScript<>(
            """
                    local c = redis.call('INCR', KEYS[1])
                    if c == 1 then
                        redis.call('EXPIRE', KEYS[1], ARGV[2])
                    end
                    if c >= tonumber(ARGV[1]) then
                        redis.call('SET', KEYS[2], '1', 'EX', ARGV[2])
                        redis.call('DEL', KEYS[1])
                        return 1
                    end
                    return 0
                    """,
            Long.class
    );

    private final StringRedisTemplate stringRedisTemplate;
    private final VerificationPolicyService policyService;

    public VerificationPolicyEnforcer(StringRedisTemplate stringRedisTemplate,
                                      VerificationPolicyService policyService) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.policyService = policyService;
    }

    /** 任一启用且身份非空的维度命中锁定即返回 true。fail-open：异常返回 false。 */
    public boolean isLocked(String scene, String account, String ip) {
        EffectivePolicy policy = policyService.getEffectivePolicy(scene);
        if (!policy.enabled()) {
            return false;
        }
        try {
            if (policy.limitByAccount() && StringUtils.hasText(account)
                    && lockExists(scene, DIM_ACCOUNT, account)) {
                return true;
            }
            return policy.limitByIp() && StringUtils.hasText(ip)
                    && lockExists(scene, DIM_IP, ip);
        } catch (RuntimeException ex) {
            log.warn("[VerificationPolicy] isLocked Redis 异常，fail-open scene={}: {}", scene, ex.getMessage());
            return false;
        }
    }

    /** 对每个启用且身份非空的维度自增计数，达上限则锁定 recoverySeconds。fail-open：异常忽略。 */
    public void recordFailure(String scene, String account, String ip) {
        EffectivePolicy policy = policyService.getEffectivePolicy(scene);
        if (!policy.enabled()) {
            return;
        }
        try {
            if (policy.limitByAccount() && StringUtils.hasText(account)) {
                record(scene, DIM_ACCOUNT, account, policy);
            }
            if (policy.limitByIp() && StringUtils.hasText(ip)) {
                record(scene, DIM_IP, ip, policy);
            }
        } catch (RuntimeException ex) {
            log.warn("[VerificationPolicy] recordFailure Redis 异常，fail-open scene={}: {}", scene, ex.getMessage());
        }
    }

    /** 成功后清除两个维度的计数与锁。fail-open：异常忽略。 */
    public void reset(String scene, String account, String ip) {
        try {
            if (StringUtils.hasText(account)) {
                stringRedisTemplate.delete(List.of(cntKey(scene, DIM_ACCOUNT, account), lockKey(scene, DIM_ACCOUNT, account)));
            }
            if (StringUtils.hasText(ip)) {
                stringRedisTemplate.delete(List.of(cntKey(scene, DIM_IP, ip), lockKey(scene, DIM_IP, ip)));
            }
        } catch (RuntimeException ex) {
            log.warn("[VerificationPolicy] reset Redis 异常 scene={}: {}", scene, ex.getMessage());
        }
    }

    private void record(String scene, String dim, String id, EffectivePolicy policy) {
        stringRedisTemplate.execute(
                RECORD_SCRIPT,
                List.of(cntKey(scene, dim, id), lockKey(scene, dim, id)),
                String.valueOf(Math.max(1, policy.maxAttempts())),
                String.valueOf(Math.max(1, policy.recoverySeconds()))
        );
    }

    private boolean lockExists(String scene, String dim, String id) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(lockKey(scene, dim, id)));
    }

    private String cntKey(String scene, String dim, String id) {
        return "vp:" + scene + ":cnt:" + dim + ":" + id;
    }

    private String lockKey(String scene, String dim, String id) {
        return "vp:" + scene + ":lock:" + dim + ":" + id;
    }
}
