package com.velox.module.system.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.velox.domain.model.BaseEntity;

import java.time.LocalDateTime;

@TableName("sys_account_security")
public class AccountSecurity extends BaseEntity {

    private String accountId;

    /** 逗号分隔: password,email_code */
    private String loginMethods;

    private String email;

    private Integer mfaEmailEnabled;

    private Integer mfaTotpEnabled;

    private String mfaTotpSecret;

    private String mfaTotpRecoveryCodes;

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
