package com.velox.module.system.domain.model;

import com.velox.domain.model.BaseEntity;

import com.baomidou.mybatisplus.annotation.TableName;

@TableName("sys_account_role")
public class AccountRole extends BaseEntity {

    private String accountId;

    private String roleId;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = normalizeIdentifier(accountId);
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = normalizeIdentifier(roleId);
    }
}
