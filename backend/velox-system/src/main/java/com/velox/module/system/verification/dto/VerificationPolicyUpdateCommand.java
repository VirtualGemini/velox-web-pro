package com.velox.module.system.verification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "openapi.settings.verification-settings.update.schema")
public class VerificationPolicyUpdateCommand {

    @Schema(description = "openapi.common.enabled", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{validation.verification-settings.enabled.required}")
    private Boolean enabled;

    @Schema(description = "openapi.settings.verification-settings.field.max_attempts", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{validation.verification-settings.max-attempts.required}")
    @Min(value = 1, message = "{validation.verification-settings.max-attempts.min}")
    private Integer maxAttempts;

    @Schema(description = "openapi.settings.verification-settings.field.recovery_seconds", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{validation.verification-settings.recovery-seconds.required}")
    @Min(value = 1, message = "{validation.verification-settings.recovery-seconds.min}")
    private Integer recoverySeconds;

    @Schema(description = "openapi.settings.verification-settings.field.limit_by_account", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{validation.verification-settings.limit-by-account.required}")
    private Boolean limitByAccount;

    @Schema(description = "openapi.settings.verification-settings.field.limit_by_ip", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{validation.verification-settings.limit-by-ip.required}")
    private Boolean limitByIp;

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
