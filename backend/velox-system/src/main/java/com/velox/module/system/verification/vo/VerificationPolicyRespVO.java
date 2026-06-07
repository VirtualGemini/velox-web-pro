package com.velox.module.system.verification.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "openapi.settings.verification-settings.resp.schema")
public class VerificationPolicyRespVO {

    @Schema(description = "openapi.settings.verification-settings.field.scene_key")
    private String sceneKey;

    @Schema(description = "openapi.common.enabled")
    private Boolean enabled;

    @Schema(description = "openapi.settings.verification-settings.field.max_attempts")
    private Integer maxAttempts;

    @Schema(description = "openapi.settings.verification-settings.field.recovery_seconds")
    private Integer recoverySeconds;

    @Schema(description = "openapi.settings.verification-settings.field.limit_by_account")
    private Boolean limitByAccount;

    @Schema(description = "openapi.settings.verification-settings.field.limit_by_ip")
    private Boolean limitByIp;

    public String getSceneKey() {
        return sceneKey;
    }

    public void setSceneKey(String sceneKey) {
        this.sceneKey = sceneKey;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(Integer maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public Integer getRecoverySeconds() {
        return recoverySeconds;
    }

    public void setRecoverySeconds(Integer recoverySeconds) {
        this.recoverySeconds = recoverySeconds;
    }

    public Boolean getLimitByAccount() {
        return limitByAccount;
    }

    public void setLimitByAccount(Boolean limitByAccount) {
        this.limitByAccount = limitByAccount;
    }

    public Boolean getLimitByIp() {
        return limitByIp;
    }

    public void setLimitByIp(Boolean limitByIp) {
        this.limitByIp = limitByIp;
    }
}
