package com.velox.module.system.account.security.dto;

/**
 * 解绑 TOTP：支持当前认证器动态口令或恢复码二选一。
 */
public class MfaTotpDisableCommand {

    private String code;

    private String recoveryCode;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getRecoveryCode() {
        return recoveryCode;
    }

    public void setRecoveryCode(String recoveryCode) {
        this.recoveryCode = recoveryCode;
    }
}
