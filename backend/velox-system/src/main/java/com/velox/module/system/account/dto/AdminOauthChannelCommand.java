package com.velox.module.system.account.dto;

/** 管理员开启/禁用他人某第三方登录渠道（被禁用渠道用户不可开启或使用）。 */
public class AdminOauthChannelCommand {

    private Boolean disabled;

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }
}
