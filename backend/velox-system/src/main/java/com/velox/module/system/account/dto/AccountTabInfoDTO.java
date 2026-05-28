package com.velox.module.system.account.dto;

public class AccountTabInfoDTO {

    private String accountId;

    private String username;

    private String remark;

    private String deletionRequestedAt;

    private String deletionExpiresAt;

    private boolean pendingDeletion;

    private String securityEmail;

    private boolean emailMfaEnabled;

    private boolean totpMfaEnabled;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getDeletionRequestedAt() {
        return deletionRequestedAt;
    }

    public void setDeletionRequestedAt(String deletionRequestedAt) {
        this.deletionRequestedAt = deletionRequestedAt;
    }

    public String getDeletionExpiresAt() {
        return deletionExpiresAt;
    }

    public void setDeletionExpiresAt(String deletionExpiresAt) {
        this.deletionExpiresAt = deletionExpiresAt;
    }

    public boolean isPendingDeletion() {
        return pendingDeletion;
    }

    public void setPendingDeletion(boolean pendingDeletion) {
        this.pendingDeletion = pendingDeletion;
    }

    public String getSecurityEmail() {
        return securityEmail;
    }

    public void setSecurityEmail(String securityEmail) {
        this.securityEmail = securityEmail;
    }

    public boolean isEmailMfaEnabled() {
        return emailMfaEnabled;
    }

    public void setEmailMfaEnabled(boolean emailMfaEnabled) {
        this.emailMfaEnabled = emailMfaEnabled;
    }

    public boolean isTotpMfaEnabled() {
        return totpMfaEnabled;
    }

    public void setTotpMfaEnabled(boolean totpMfaEnabled) {
        this.totpMfaEnabled = totpMfaEnabled;
    }
}
