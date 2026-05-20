package com.velox.module.system.auth.store;

public interface VerificationCodeStore {

    void saveCaptcha(String key, String code);

    VerificationResult consumeCaptcha(String key, String code);

    boolean captchaExists(String key);

    boolean trySaveResetCode(String email, String code);

    void invalidateResetCode(String email);

    VerificationResult verifyResetCode(String email, String code);

    boolean resetCodeExists(String email);

    enum VerificationResult {
        MATCHED,
        INVALID,
        EXPIRED
    }
}
