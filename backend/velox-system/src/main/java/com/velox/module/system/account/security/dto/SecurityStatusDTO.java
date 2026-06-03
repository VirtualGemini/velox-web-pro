package com.velox.module.system.account.security.dto;

import java.util.List;

public class SecurityStatusDTO {

    private String email;
    private String emailMasked;
    private List<String> loginMethods;
    private List<String> effectiveLoginMethods;
    private List<String> allowedLoginMethods;
    private List<String> adminDisabledLoginMethods;
    private boolean passwordRequired;
    private MfaStatus mfa = new MfaStatus();
    private String emailVerifiedAt;
    private String lastPasswordChangeAt;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmailMasked() {
        return emailMasked;
    }

    public void setEmailMasked(String emailMasked) {
        this.emailMasked = emailMasked;
    }

    public List<String> getLoginMethods() {
        return loginMethods;
    }

    public void setLoginMethods(List<String> loginMethods) {
        this.loginMethods = loginMethods;
    }

    public List<String> getEffectiveLoginMethods() {
        return effectiveLoginMethods;
    }

    public void setEffectiveLoginMethods(List<String> effectiveLoginMethods) {
        this.effectiveLoginMethods = effectiveLoginMethods;
    }

    public List<String> getAllowedLoginMethods() {
        return allowedLoginMethods;
    }

    public void setAllowedLoginMethods(List<String> allowedLoginMethods) {
        this.allowedLoginMethods = allowedLoginMethods;
    }

    public List<String> getAdminDisabledLoginMethods() {
        return adminDisabledLoginMethods;
    }

    public void setAdminDisabledLoginMethods(List<String> adminDisabledLoginMethods) {
        this.adminDisabledLoginMethods = adminDisabledLoginMethods;
    }

    public boolean isPasswordRequired() {
        return passwordRequired;
    }

    public void setPasswordRequired(boolean passwordRequired) {
        this.passwordRequired = passwordRequired;
    }

    public MfaStatus getMfa() {
        return mfa;
    }

    public void setMfa(MfaStatus mfa) {
        this.mfa = mfa;
    }

    public String getEmailVerifiedAt() {
        return emailVerifiedAt;
    }

    public void setEmailVerifiedAt(String emailVerifiedAt) {
        this.emailVerifiedAt = emailVerifiedAt;
    }

    public String getLastPasswordChangeAt() {
        return lastPasswordChangeAt;
    }

    public void setLastPasswordChangeAt(String lastPasswordChangeAt) {
        this.lastPasswordChangeAt = lastPasswordChangeAt;
    }

    public static class MfaStatus {
        private boolean email;
        private boolean totp;

        public boolean isEmail() {
            return email;
        }

        public void setEmail(boolean email) {
            this.email = email;
        }

        public boolean isTotp() {
            return totp;
        }

        public void setTotp(boolean totp) {
            this.totp = totp;
        }
    }
}
