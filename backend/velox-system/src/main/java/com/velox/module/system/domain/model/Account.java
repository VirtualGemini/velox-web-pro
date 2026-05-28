package com.velox.module.system.domain.model;

import com.velox.domain.model.BaseEntity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("sys_account")
public class Account extends BaseEntity {

    private String username;

    private String password;

    private String remark;

    private Integer status;

    private Integer loginFailCount;

    private LocalDateTime loginFailTime;

    private LocalDateTime deletionRequestedAt;

    private LocalDateTime deletionExpiresAt;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getLoginFailCount() {
        return loginFailCount;
    }

    public void setLoginFailCount(Integer loginFailCount) {
        this.loginFailCount = loginFailCount;
    }

    public LocalDateTime getLoginFailTime() {
        return loginFailTime;
    }

    public void setLoginFailTime(LocalDateTime loginFailTime) {
        this.loginFailTime = loginFailTime;
    }

    public LocalDateTime getDeletionRequestedAt() {
        return deletionRequestedAt;
    }

    public void setDeletionRequestedAt(LocalDateTime deletionRequestedAt) {
        this.deletionRequestedAt = deletionRequestedAt;
    }

    public LocalDateTime getDeletionExpiresAt() {
        return deletionExpiresAt;
    }

    public void setDeletionExpiresAt(LocalDateTime deletionExpiresAt) {
        this.deletionExpiresAt = deletionExpiresAt;
    }
}
