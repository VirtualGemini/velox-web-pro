package com.velox.module.system.accesscontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "openapi.settings.access-control.toggle.schema")
public class AccessControlToggleCommand {

    @Schema(description = "openapi.common.enabled", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{validation.access-control.enabled.required}")
    private Boolean enabled;

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
