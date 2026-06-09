package com.velox.module.system.log.support;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum SensitiveLogField {

    PASSWORD("password"),
    OLD_PASSWORD("oldPassword"),
    NEW_PASSWORD("newPassword"),
    CONFIRM_PASSWORD("confirmPassword"),
    TOKEN("token"),
    ACCESS_TOKEN("accessToken"),
    REFRESH_TOKEN("refreshToken"),
    AUTHORIZATION("authorization"),
    CAPTCHA_CODE("captchaCode"),
    CAPTCHA_CODE_KEY("captchaCodeKey"),
    CAPTCHA_CODE_IMG("captchaCodeImg"),
    CAPTCHA_TICKET("captchaTicket"),
    CODE("code"),
    EMAIL_CODE("emailCode"),
    CURRENT_EMAIL_CODE("currentEmailCode"),
    NEW_EMAIL_CODE("newEmailCode"),
    VERIFICATION_CODE("verificationCode"),
    TOTP("totp"),
    TOTP_CODE("totpCode"),
    OTP("otp"),
    MFA_CODE("mfaCode"),
    MFA_CHALLENGE("mfaChallenge"),
    MFA_TOTP_SECRET("mfaTotpSecret"),
    MFA_TOTP_RECOVERY_CODES("mfaTotpRecoveryCodes"),
    RECOVERY_CODE("recoveryCode"),
    MFA_RECOVERY_CODE("mfaRecoveryCode"),
    SECRET("secret"),
    PRIVATE_KEY("privateKey"),
    API_KEY("apiKey"),
    SESSION_ID("sessionId");

    private final String fieldName;

    SensitiveLogField(String fieldName) {
        this.fieldName = fieldName;
    }

    public String fieldName() {
        return fieldName;
    }

    public static Set<String> normalizedKeys() {
        return Arrays.stream(values())
                .flatMap(field -> Stream.of(field.fieldName, field.name()))
                .map(SensitiveLogField::normalize)
                .collect(Collectors.toUnmodifiableSet());
    }

    public static String textPatternAlternation() {
        return Arrays.stream(values())
                .map(SensitiveLogField::fieldName)
                .collect(Collectors.joining("|"));
    }

    public static String normalize(String key) {
        return key.replace("_", "").replace("-", "").toLowerCase(Locale.ROOT);
    }
}
