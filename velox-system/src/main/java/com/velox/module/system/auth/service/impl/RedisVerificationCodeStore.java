package com.velox.module.system.auth.service.impl;

import com.velox.framework.config.SecurityProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
@ConditionalOnProperty(prefix = "velox.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
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

    private static final DefaultRedisScript<Long> COMPARE_AND_DELETE_IF_MATCH_SCRIPT = new DefaultRedisScript<>(
            """
                    local stored = redis.call('GET', KEYS[1])
                    if not stored then
                        return 0
                    end
                    if stored == ARGV[1] then
                        redis.call('DEL', KEYS[1])
                        return 2
                    end
                    return 1
                    """,
            Long.class
    );

    private static final DefaultRedisScript<Long> SAVE_RESET_CODE_IF_ALLOWED_SCRIPT = new DefaultRedisScript<>(
            """
                    if redis.call('EXISTS', KEYS[2]) == 1 then
                        return 0
                    end
                    redis.call('SET', KEYS[1], ARGV[1], 'EX', ARGV[2])
                    redis.call('SET', KEYS[2], '1', 'EX', ARGV[3])
                    return 1
                    """,
            Long.class
    );

    private final StringRedisTemplate stringRedisTemplate;

    public RedisVerificationCodeStore(StringRedisTemplate stringRedisTemplate, SecurityProperties securityProperties) {
        super(securityProperties);
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void saveCaptcha(String key, String code) {
        stringRedisTemplate.opsForValue().set(
                CAPTCHA_PREFIX + key,
                digest(code),
                Duration.ofSeconds(securityProperties.getCaptcha().getTtlSeconds())
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
                SAVE_RESET_CODE_IF_ALLOWED_SCRIPT,
                List.of(RESET_PREFIX + email, RESET_SENT_PREFIX + email),
                digest(code),
                String.valueOf(securityProperties.getVerification().getResetCodeTtlSeconds()),
                String.valueOf(securityProperties.getVerification().getResetCodeResendIntervalSeconds())
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
        Long result = stringRedisTemplate.execute(
                COMPARE_AND_DELETE_IF_MATCH_SCRIPT,
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
}
