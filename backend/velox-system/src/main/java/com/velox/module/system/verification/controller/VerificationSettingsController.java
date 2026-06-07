package com.velox.module.system.verification.controller;

import com.velox.common.result.Result;
import com.velox.framework.security.api.annotation.RequirePermission;
import com.velox.module.system.verification.dto.VerificationPolicyUpdateCommand;
import com.velox.module.system.verification.service.VerificationPolicyService;
import com.velox.module.system.verification.vo.VerificationPolicyRespVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "openapi.settings.verification-settings.tag.name",
        description = "openapi.settings.verification-settings.tag.description")
@RestController
@RequestMapping("/settings/verification-settings")
@Validated
public class VerificationSettingsController {

    private final VerificationPolicyService verificationPolicyService;

    public VerificationSettingsController(VerificationPolicyService verificationPolicyService) {
        this.verificationPolicyService = verificationPolicyService;
    }

    @GetMapping
    @Operation(summary = "openapi.settings.verification-settings.get.summary")
    @RequirePermission("settings:verification-settings:query")
    public Result<List<VerificationPolicyRespVO>> list() {
        return Result.ok(verificationPolicyService.listScenes());
    }

    @PutMapping("/{sceneKey}")
    @Operation(summary = "openapi.settings.verification-settings.update.summary")
    @RequirePermission("settings:verification-settings:update")
    public Result<Boolean> update(@PathVariable("sceneKey") String sceneKey,
                                  @Valid @RequestBody VerificationPolicyUpdateCommand command) {
        verificationPolicyService.updateScene(sceneKey, command);
        return Result.ok(true);
    }
}
