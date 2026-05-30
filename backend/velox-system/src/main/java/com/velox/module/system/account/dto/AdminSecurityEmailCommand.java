package com.velox.module.system.account.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/** 管理员设置/更换/清除他人安全邮箱（无需验证码）。email 为空表示清除绑定。 */
public class AdminSecurityEmailCommand {

    @Email(message = "{validation.system.user.profile.email.invalid}")
    @Size(max = 100, message = "{validation.system.user.profile.email.size}")
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
