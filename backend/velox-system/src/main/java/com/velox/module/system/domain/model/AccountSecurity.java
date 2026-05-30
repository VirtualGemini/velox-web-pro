package com.velox.module.system.domain.model;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.velox.domain.model.BaseEntity;

import java.time.LocalDateTime;

@TableName("sys_account_security")
public class AccountSecurity extends BaseEntity {

    private String accountId;

    /** 逗号分隔: password,email_code */
    private String loginMethods;

    /**
     * 管理员禁用的登录方式（逗号分隔），用户不可开启或使用。
     * 需要支持清空为 NULL，故使用 ALWAYS 策略覆盖默认的 NOT_NULL（updateById 会跳过 null）。
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String disabledLoginMethods;

    /**
     * 管理员禁用的第三方登录渠道（逗号分隔），用户不可开启或使用。
     * 同样需要支持清空为 NULL，使用 ALWAYS 策略。
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String disabledOauthChannels;

    /** 安全邮箱，解绑时需要清空为 NULL，使用 ALWAYS 策略。 */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String email;

    private Integer mfaEmailEnabled;

    private Integer mfaTotpEnabled;

    /** 关闭 TOTP 时需要清空密钥为 NULL，使用 ALWAYS 策略。 */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String mfaTotpSecret;

    /** 关闭 TOTP 时需要清空恢复码为 NULL，使用 ALWAYS 策略。 */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String mfaTotpRecoveryCodes;

    /** 邮箱验证时间，解绑邮箱时需要清空为 NULL，使用 ALWAYS 策略。 */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private LocalDateTime emailVerifiedAt;

    private LocalDateTime lastPasswordChangeAt;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = normalizeIdentifier(accountId);
    }

    public String getLoginMethods() {
        return loginMethods;
    }

    public void setLoginMethods(String loginMethods) {
        this.loginMethods = loginMethods;
    }

    public String getDisabledLoginMethods() {
        return disabledLoginMethods;
    }

    public void setDisabledLoginMethods(String disabledLoginMethods) {
        this.disabledLoginMethods = disabledLoginMethods;
    }

    public String getDisabledOauthChannels() {
        return disabledOauthChannels;
    }

    public void setDisabledOauthChannels(String disabledOauthChannels) {
        this.disabledOauthChannels = disabledOauthChannels;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getMfaEmailEnabled() {
        return mfaEmailEnabled;
    }

    public void setMfaEmailEnabled(Integer mfaEmailEnabled) {
        this.mfaEmailEnabled = mfaEmailEnabled;
    }

    public Integer getMfaTotpEnabled() {
        return mfaTotpEnabled;
    }

    public void setMfaTotpEnabled(Integer mfaTotpEnabled) {
        this.mfaTotpEnabled = mfaTotpEnabled;
    }

    public String getMfaTotpSecret() {
        return mfaTotpSecret;
    }

    public void setMfaTotpSecret(String mfaTotpSecret) {
        this.mfaTotpSecret = mfaTotpSecret;
    }

    public String getMfaTotpRecoveryCodes() {
        return mfaTotpRecoveryCodes;
    }

    public void setMfaTotpRecoveryCodes(String mfaTotpRecoveryCodes) {
        this.mfaTotpRecoveryCodes = mfaTotpRecoveryCodes;
    }

    public LocalDateTime getEmailVerifiedAt() {
        return emailVerifiedAt;
    }

    public void setEmailVerifiedAt(LocalDateTime emailVerifiedAt) {
        this.emailVerifiedAt = emailVerifiedAt;
    }

    public LocalDateTime getLastPasswordChangeAt() {
        return lastPasswordChangeAt;
    }

    public void setLastPasswordChangeAt(LocalDateTime lastPasswordChangeAt) {
        this.lastPasswordChangeAt = lastPasswordChangeAt;
    }
}
