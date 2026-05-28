package com.velox.module.system.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.velox.domain.model.BaseEntity;

import java.time.LocalDateTime;

@TableName("sys_account_session")
public class AccountSession extends BaseEntity {

    private String accountId;

    private String tokenHash;

    private Integer status;

    private LocalDateTime loginTime;

    private LocalDateTime lastActiveTime;

    private LocalDateTime logoutTime;

    private LocalDateTime presenceExpireTime;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = normalizeIdentifier(accountId);
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = normalizeIdentifier(tokenHash);
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(LocalDateTime loginTime) {
        this.loginTime = loginTime;
    }

    public LocalDateTime getLastActiveTime() {
        return lastActiveTime;
    }

    public void setLastActiveTime(LocalDateTime lastActiveTime) {
        this.lastActiveTime = lastActiveTime;
    }

    public LocalDateTime getLogoutTime() {
        return logoutTime;
    }

    public void setLogoutTime(LocalDateTime logoutTime) {
        this.logoutTime = logoutTime;
    }

    public LocalDateTime getPresenceExpireTime() {
        return presenceExpireTime;
    }

    public void setPresenceExpireTime(LocalDateTime presenceExpireTime) {
        this.presenceExpireTime = presenceExpireTime;
    }
}
