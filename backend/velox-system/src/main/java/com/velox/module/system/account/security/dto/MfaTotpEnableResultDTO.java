package com.velox.module.system.account.security.dto;

import java.util.List;

/**
 * TOTP 启用成功后返回一次性展示的恢复码。
 */
public class MfaTotpEnableResultDTO {

    private List<String> recoveryCodes;

    public List<String> getRecoveryCodes() {
        return recoveryCodes;
    }

    public void setRecoveryCodes(List<String> recoveryCodes) {
        this.recoveryCodes = recoveryCodes;
    }
}
