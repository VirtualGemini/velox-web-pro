package com.velox.module.system.account.security.dto;

import jakarta.validation.constraints.Size;

public class EmailUnbindCommand {

    @Size(max = 12)
    private String currentEmailCode;

    @Size(max = 12)
    private String totpCode;

    public String getCurrentEmailCode() {
        return currentEmailCode;
    }

    public void setCurrentEmailCode(String currentEmailCode) {
        this.currentEmailCode = currentEmailCode;
    }

    public String getTotpCode() {
        return totpCode;
    }

    public void setTotpCode(String totpCode) {
        this.totpCode = totpCode;
    }
}
