package com.velox.module.system.auth.service.impl;

import com.velox.framework.config.SecurityProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnProperty(prefix = "velox.redis", name = "enabled", havingValue = "false")
public class InMemoryVerificationCodeStore extends AbstractVerificationCodeStore {

    private static final int CLEANUP_INTERVAL = 128;

    private final Map<String, ExpiringValue> store = new ConcurrentHashMap<>();
    private final AtomicInteger operationCounter = new AtomicInteger();
    private final Object resetCodeMutex = new Object();

    public InMemoryVerificationCodeStore(SecurityProperties securityProperties) {
        super(securityProperties);
    }

    @Override
    public void saveCaptcha(String key, String code) {
        put(CAPTCHA_PREFIX + key, digest(code), Duration.ofSeconds(securityProperties.getCaptcha().getTtlSeconds()));
    }

    @Override
    public VerificationResult consumeCaptcha(String key, String code) {
        String storeKey = CAPTCHA_PREFIX + key;
        return consumeAndCompare(storeKey, digest(code));
    }

    @Override
    public boolean captchaExists(String key) {
        return get(CAPTCHA_PREFIX + key) != null;
    }

    @Override
    public boolean trySaveResetCode(String email, String code) {
        synchronized (resetCodeMutex) {
            cleanupExpiredEntriesIfNeeded();
            if (peek(RESET_SENT_PREFIX + email) != null) {
                return false;
            }
            put(
                    RESET_PREFIX + email,
                    digest(code),
                    Duration.ofSeconds(securityProperties.getVerification().getResetCodeTtlSeconds())
            );
            put(
                    RESET_SENT_PREFIX + email,
                    "1",
                    Duration.ofSeconds(securityProperties.getVerification().getResetCodeResendIntervalSeconds())
            );
            return true;
        }
    }

    @Override
    public void invalidateResetCode(String email) {
        synchronized (resetCodeMutex) {
            store.remove(RESET_PREFIX + email);
            store.remove(RESET_SENT_PREFIX + email);
        }
    }

    @Override
    public VerificationResult verifyResetCode(String email, String code) {
        return compareAndDeleteIfMatch(RESET_PREFIX + email, digest(code));
    }

    @Override
    public boolean resetCodeExists(String email) {
        return get(RESET_PREFIX + email) != null;
    }

    private void put(String key, String value, Duration ttl) {
        cleanupExpiredEntriesIfNeeded();
        store.put(key, new ExpiringValue(value, System.currentTimeMillis() + ttl.toMillis()));
    }

    private ExpiringValue get(String key) {
        cleanupExpiredEntriesIfNeeded();
        ExpiringValue value = store.get(key);
        if (value == null) {
            return null;
        }
        if (value.expiredAtMillis() <= System.currentTimeMillis()) {
            store.remove(key, value);
            return null;
        }
        return value;
    }

    private ExpiringValue peek(String key) {
        ExpiringValue value = store.get(key);
        if (value == null) {
            return null;
        }
        if (value.expiredAtMillis() <= System.currentTimeMillis()) {
            store.remove(key, value);
            return null;
        }
        return value;
    }

    private VerificationResult consumeAndCompare(String key, String expectedDigest) {
        cleanupExpiredEntriesIfNeeded();
        AtomicReference<VerificationResult> result = new AtomicReference<>(VerificationResult.EXPIRED);
        store.compute(key, (ignored, existing) -> {
            if (existing == null || existing.expiredAtMillis() <= System.currentTimeMillis()) {
                result.set(VerificationResult.EXPIRED);
                return null;
            }
            if (existing.value().equals(expectedDigest)) {
                result.set(VerificationResult.MATCHED);
            } else {
                result.set(VerificationResult.INVALID);
            }
            return null;
        });
        return result.get();
    }

    private VerificationResult compareAndDeleteIfMatch(String key, String expectedDigest) {
        cleanupExpiredEntriesIfNeeded();
        AtomicReference<VerificationResult> result = new AtomicReference<>(VerificationResult.EXPIRED);
        store.compute(key, (ignored, existing) -> {
            if (existing == null || existing.expiredAtMillis() <= System.currentTimeMillis()) {
                result.set(VerificationResult.EXPIRED);
                return null;
            }
            if (existing.value().equals(expectedDigest)) {
                result.set(VerificationResult.MATCHED);
                return null;
            }
            result.set(VerificationResult.INVALID);
            return existing;
        });
        return result.get();
    }

    int storedEntryCount() {
        cleanupExpiredEntries();
        return store.size();
    }

    private void cleanupExpiredEntriesIfNeeded() {
        if (operationCounter.incrementAndGet() % CLEANUP_INTERVAL == 0) {
            cleanupExpiredEntries();
        }
    }

    private void cleanupExpiredEntries() {
        long now = System.currentTimeMillis();
        for (Map.Entry<String, ExpiringValue> entry : store.entrySet()) {
            ExpiringValue value = entry.getValue();
            if (value.expiredAtMillis() <= now) {
                store.remove(entry.getKey(), value);
            }
        }
    }

    private record ExpiringValue(String value, long expiredAtMillis) {
    }
}
