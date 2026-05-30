package com.velox.module.system.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 管理员重置他人密码（无需原密码、无需二次验证）。 */
public class AdminPasswordResetCommand {

    @NotBlank(message = "{validation.system.user.password.new_password.not_blank}")
    @Size(min = 6, max = 32, message = "{validation.system.user.password.new_password.size}")
    private String newPassword;

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
