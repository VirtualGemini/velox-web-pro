package com.velox.module.system.auth.support;

import java.security.SecureRandom;

/**
 * 安全验证码生成器（CSPRNG）。
 *
 * <p>替代 Hutool {@code RandomUtil}（底层 {@code ThreadLocalRandom}，非密码学安全）。
 * 邮箱/重置/MFA 验证码与 TOTP 恢复码等安全敏感随机串必须使用 {@link SecureRandom}，避免可预测。
 */
public final class SecureCodeGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final char[] DIGITS = "0123456789".toCharArray();
    private static final char[] UPPER_ALNUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();

    private SecureCodeGenerator() {
    }

    /** 生成指定长度的纯数字验证码。 */
    public static String numeric(int length) {
        return random(DIGITS, length);
    }

    /** 生成指定长度的大写字母+数字串（用于恢复码等）。 */
    public static String upperAlphaNumeric(int length) {
        return random(UPPER_ALNUM, length);
    }

    private static String random(char[] alphabet, int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(alphabet[RANDOM.nextInt(alphabet.length)]);
        }
        return sb.toString();
    }
}
