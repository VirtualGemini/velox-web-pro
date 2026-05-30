package com.velox.module.system.account.dto;

/** 管理员开启/关闭他人邮箱二次验证（无需验证码）。开启要求已绑定安全邮箱且 TOTP 未开启。 */
public class AdminMfaEmailCommand {

    private Boolean enabled;

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
