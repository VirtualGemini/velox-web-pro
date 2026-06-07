package com.velox.module.system.auth.store;

import com.velox.module.system.auth.properties.SystemAuthProperties;
import com.velox.module.system.verification.common.EffectivePolicy;
import com.velox.module.system.verification.common.VerificationScene;
import com.velox.module.system.verification.service.VerificationPolicyService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.time.Duration;
import java.util.List;

public class RedisVerificationCodeStore extends AbstractVerificationCodeStore {

    private static final DefaultRedisScript<Long> CONSUME_AND_COMPARE_SCRIPT = new DefaultRedisScript<>(
            """
                    local stored = redis.call('GET', KEYS[1])
                    if not stored then
                        return 0
                    end
                    redis.call('DEL', KEYS[1])
                    if stored == ARGV[1] then
                        return 2
                    end
                    return 1
                    """,
            Long.class
    );

    private static final DefaultRedisScript<Long> COMPARE_WITH_ATTEMPTS_SCRIPT = new DefaultRedisScript<>(
            """
                    local stored = redis.call('GET', KEYS[1])
                    if not stored then
                        return 0
                    end
                    local attemptsKey = KEYS[1] .. ':attempts'
                    if stored == ARGV[1] then
                        redis.call('DEL', KEYS[1])
                        redis.call('DEL', attemptsKey)
                        return 2
                    end
                    local attempts = redis.call('INCR', attemptsKey)
                    if attempts == 1 then
                        local ttl = redis.call('TTL', KEYS[1])
                        if ttl > 0 then
                            redis.call('EXPIRE', attemptsKey, ttl)
                        end
                    end
                    if attempts >= tonumber(ARGV[2]) then
                        redis.call('DEL', KEYS[1])
                        redis.call('DEL', attemptsKey)
                        return 3
                    end
                    return 1
                    """,
            Long.class
    );

    private static final DefaultRedisScript<Long> SAVE_CODE_IF_ALLOWED_SCRIPT = new DefaultRedisScript<>(
            """
                    if redis.call('EXISTS', KEYS[2]) == 1 then
                        return 0
                    end
                    redis.call('SET', KEYS[1], ARGV[1], 'EX', ARGV[2])
                    redis.call('SET', KEYS[2], '1', 'EX', ARGV[3])
                    redis.call('DEL', KEYS[1] .. ':attempts')
                    return 1
                    """,
            Long.class
    );

    private final StringRedisTemplate stringRedisTemplate;
    private final VerificationPolicyService policyService;

    public RedisVerificationCodeStore(StringRedisTemplate stringRedisTemplate,
                                      SystemAuthProperties authProperties,
                                      VerificationPolicyService policyService) {
        super(authProperties);
        this.stringRedisTemplate = stringRedisTemplate;
        this.policyService = policyService;
    }

    @Override
    public void saveCaptcha(String key, String code) {
        stringRedisTemplate.opsForValue().set(
                CAPTCHA_PREFIX + key,
                digest(code),
                Duration.ofSeconds(authProperties.getCaptcha().getTtlSeconds())
        );
    }

    @Override
    public VerificationResult consumeCaptcha(String key, String code) {
        return executeConsumeAndCompare(CAPTCHA_PREFIX + key, digest(code));
    }

    @Override
    public boolean captchaExists(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(CAPTCHA_PREFIX + key));
    }

    @Override
    public boolean trySaveResetCode(String email, String code) {
        Long result = stringRedisTemplate.execute(
                SAVE_CODE_IF_ALLOWED_SCRIPT,
                List.of(RESET_PREFIX + email, RESET_SENT_PREFIX + email),
                digest(code),
                String.valueOf(authProperties.getVerification().getResetCodeTtlSeconds()),
                String.valueOf(authProperties.getVerification().getResetCodeResendIntervalSeconds())
        );
        return Long.valueOf(1L).equals(result);
    }

    @Override
    public void invalidateResetCode(String email) {
        stringRedisTemplate.delete(List.of(RESET_PREFIX + email, RESET_SENT_PREFIX + email));
    }

    @Override
    public VerificationResult verifyResetCode(String email, String code) {
        return executeCompareAndDeleteIfMatch(RESET_PREFIX + email, digest(code));
    }

    @Override
    public boolean resetCodeExists(String email) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(RESET_PREFIX + email));
    }

    @Override
    public boolean trySaveLoginCode(String target, String code) {
        Long result = stringRedisTemplate.execute(
                SAVE_CODE_IF_ALLOWED_SCRIPT,
                List.of(LOGIN_CODE_PREFIX + target, LOGIN_CODE_SENT_PREFIX + target),
                digest(code),
                String.valueOf(authProperties.getVerification().getResetCodeTtlSeconds()),
                String.valueOf(authProperties.getVerification().getResetCodeResendIntervalSeconds())
        );
        return Long.valueOf(1L).equals(result);
    }

    @Override
    public void invalidateLoginCode(String target) {
        stringRedisTemplate.delete(List.of(LOGIN_CODE_PREFIX + target, LOGIN_CODE_SENT_PREFIX + target));
    }

    @Override
    public VerificationResult verifyLoginCode(String target, String code) {
        return executeCompareAndDeleteIfMatch(LOGIN_CODE_PREFIX + target, digest(code));
    }

    @Override
    public boolean loginCodeExists(String target) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(LOGIN_CODE_PREFIX + target));
    }

    @Override
    public boolean trySaveRebindCode(String scope, String target, String code, int ttlSeconds, int resendIntervalSeconds) {
        Long result = stringRedisTemplate.execute(
                SAVE_CODE_IF_ALLOWED_SCRIPT,
                List.of(rebindKey(scope, target), rebindSentKey(scope, target)),
                digest(code),
                String.valueOf(ttlSeconds),
                String.valueOf(resendIntervalSeconds)
        );
        return Long.valueOf(1L).equals(result);
    }

    @Override
    public void invalidateRebindCode(String scope, String target) {
        stringRedisTemplate.delete(List.of(rebindKey(scope, target), rebindSentKey(scope, target)));
    }

    @Override
    public VerificationResult verifyRebindCode(String scope, String target, String code) {
        return executeCompareAndDeleteIfMatch(rebindKey(scope, target), digest(code));
    }

    @Override
    public boolean rebindCodeExists(String scope, String target) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(rebindKey(scope, target)));
    }

    @Override
    public boolean trySaveMfaCode(String userId, String code, int ttlSeconds, int resendIntervalSeconds) {
        Long result = stringRedisTemplate.execute(
                SAVE_CODE_IF_ALLOWED_SCRIPT,
                List.of(MFA_CODE_PREFIX + userId, MFA_CODE_SENT_PREFIX + userId),
                digest(code),
                String.valueOf(ttlSeconds),
                String.valueOf(resendIntervalSeconds)
        );
        return Long.valueOf(1L).equals(result);
    }

    @Override
    public void invalidateMfaCode(String userId) {
        stringRedisTemplate.delete(List.of(MFA_CODE_PREFIX + userId, MFA_CODE_SENT_PREFIX + userId));
    }

    @Override
    public VerificationResult verifyMfaCode(String userId, String code) {
        return executeCompareAndDeleteIfMatch(MFA_CODE_PREFIX + userId, digest(code));
    }

    @Override
    public boolean mfaCodeExists(String userId) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(MFA_CODE_PREFIX + userId));
    }

    @Override
    public void saveMfaChallenge(String challengeToken, String userId, int ttlSeconds) {
        stringRedisTemplate.opsForValue().set(
                MFA_CHALLENGE_PREFIX + challengeToken,
                userId,
                Duration.ofSeconds(ttlSeconds)
        );
    }

    @Override
    public String consumeMfaChallenge(String challengeToken) {
        String key = MFA_CHALLENGE_PREFIX + challengeToken;
        String userId = stringRedisTemplate.opsForValue().get(key);
        if (userId == null) {
            return null;
        }
        stringRedisTemplate.delete(key);
        return userId;
    }

    @Override
    public String peekMfaChallenge(String challengeToken) {
        return stringRedisTemplate.opsForValue().get(MFA_CHALLENGE_PREFIX + challengeToken);
    }

    @Override
    public void saveProofTicket(String scene, String proofTicket, String userId, int ttlSeconds) {
        stringRedisTemplate.opsForValue().set(
                proofTicketKey(scene, proofTicket),
                userId,
                Duration.ofSeconds(ttlSeconds)
        );
    }

    @Override
    public String consumeProofTicket(String scene, String proofTicket) {
        String key = proofTicketKey(scene, proofTicket);
        String userId = stringRedisTemplate.opsForValue().get(key);
        if (userId == null) {
            return null;
        }
        stringRedisTemplate.delete(key);
        return userId;
    }

    @Override
    public String peekProofTicket(String scene, String proofTicket) {
        return stringRedisTemplate.opsForValue().get(proofTicketKey(scene, proofTicket));
    }

    @Override
    public long incrementTotpAttempt(String challenge, int ttlSeconds) {
        String key = MFA_TOTP_ATTEMPT_PREFIX + challenge;
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            stringRedisTemplate.expire(key, Duration.ofSeconds(ttlSeconds));
        }
        return count == null ? 0L : count;
    }

    @Override
    public void clearTotpAttempts(String challenge) {
        stringRedisTemplate.delete(MFA_TOTP_ATTEMPT_PREFIX + challenge);
    }

    private VerificationResult executeConsumeAndCompare(String redisKey, String digestedCode) {
        Long result = stringRedisTemplate.execute(
                CONSUME_AND_COMPARE_SCRIPT,
                List.of(redisKey),
                digestedCode
        );
        if (Long.valueOf(2L).equals(result)) {
            return VerificationResult.MATCHED;
        }
        if (Long.valueOf(1L).equals(result)) {
            return VerificationResult.INVALID;
        }
        return VerificationResult.EXPIRED;
    }

    private VerificationResult executeCompareAndDeleteIfMatch(String redisKey, String digestedCode) {
        // 验证码错误次数上限由 verify_code 策略动态驱动；策略关闭即视为不限次（no-op）。
        EffectivePolicy verifyCode = policyService.getEffectivePolicy(VerificationScene.VERIFY_CODE);
        int maxAttempts = verifyCode.enabled() ? verifyCode.maxAttempts() : Integer.MAX_VALUE;
        Long result = stringRedisTemplate.execute(
                COMPARE_WITH_ATTEMPTS_SCRIPT,
                List.of(redisKey),
                digestedCode,
                String.valueOf(Math.max(1, maxAttempts))
        );
        if (Long.valueOf(2L).equals(result)) {
            return VerificationResult.MATCHED;
        }
        if (Long.valueOf(3L).equals(result)) {
            return VerificationResult.TOO_MANY_ATTEMPTS;
        }
        if (Long.valueOf(1L).equals(result)) {
            return VerificationResult.INVALID;
        }
        return VerificationResult.EXPIRED;
    }
}
