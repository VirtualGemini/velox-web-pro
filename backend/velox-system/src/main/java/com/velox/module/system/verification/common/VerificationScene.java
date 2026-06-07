package com.velox.module.system.verification.common;

/**
 * 验证策略场景键（与 sys_verification_policy.scene_key 一一对应）。
 */
public final class VerificationScene {

    /** 密码登录失败锁定。 */
    public static final String LOGIN = "login";
    /** 邮箱验证码错误次数。 */
    public static final String VERIFY_CODE = "verify_code";
    /** 认证接口 IP 限流（captcha/撞库）。 */
    public static final String CAPTCHA = "captcha";
    /** 发码频率。 */
    public static final String SEND_CODE = "send_code";
    /** MFA（TOTP/邮箱）验证失败。 */
    public static final String MFA = "mfa";

    private VerificationScene() {
    }
}
