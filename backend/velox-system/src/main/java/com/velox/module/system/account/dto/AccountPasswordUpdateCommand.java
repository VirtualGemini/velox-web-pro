package com.velox.module.system.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AccountPasswordUpdateCommand {

    private String mfaType;

    @NotBlank(message = "{validation.system.user.password.current_password.not_blank}")
    private String currentPassword;

    @NotBlank(message = "{validation.system.user.password.new_password.not_blank}")
    @Size(min = 6, max = 32, message = "{validation.system.user.password.new_password.size}")
    private String newPassword;

    @NotBlank(message = "{validation.system.user.password.confirm_password.not_blank}")
    private String confirmPassword;

    private String mfaEmailCode;

    private String mfaTotpCode;

    public String getMfaType() {
        return mfaType;
    }

    public void setMfaType(String mfaType) {
        this.mfaType = mfaType;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getMfaEmailCode() {
        return mfaEmailCode;
    }

    public void setMfaEmailCode(String mfaEmailCode) {
        this.mfaEmailCode = mfaEmailCode;
    }

    public String getMfaTotpCode() {
        return mfaTotpCode;
    }

    public void setMfaTotpCode(String mfaTotpCode) {
        this.mfaTotpCode = mfaTotpCode;
    }
}
