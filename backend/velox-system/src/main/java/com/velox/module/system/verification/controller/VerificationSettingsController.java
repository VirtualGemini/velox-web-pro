package com.velox.module.system.verification.controller;

import com.velox.common.exception.ApiException;
import com.velox.common.exception.BusinessErrorCode;
import com.velox.common.result.Result;
import com.velox.framework.security.api.authorization.SecurityAuthorizationService;
import com.velox.module.system.verification.common.VerificationScene;
import com.velox.module.system.verification.dto.VerificationPolicyUpdateCommand;
import com.velox.module.system.verification.service.VerificationPolicyService;
import com.velox.module.system.verification.vo.VerificationPolicyRespVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "openapi.settings.verification-settings.tag.name",
        description = "openapi.settings.verification-settings.tag.description")
@RestController
@RequestMapping("/settings/verification-settings")
@Validated
public class VerificationSettingsController {

    /** 场景键 -> 该场景的管理权限标识（已取消独立查询/编辑权限，改为按场景授权）。 */
    private static final Map<String, String> SCENE_PERMISSIONS = Map.of(
            VerificationScene.LOGIN, "settings:verification-settings:login",
            VerificationScene.VERIFY_CODE, "settings:verification-settings:verify-code",
            VerificationScene.CAPTCHA, "settings:verification-settings:captcha",
            VerificationScene.SEND_CODE, "settings:verification-settings:send-code",
            VerificationScene.MFA, "settings:verification-settings:mfa"
    );

    private final VerificationPolicyService verificationPolicyService;
    private final SecurityAuthorizationService securityAuthorizationService;

    public VerificationSettingsController(VerificationPolicyService verificationPolicyService,
                                          SecurityAuthorizationService securityAuthorizationService) {
        this.verificationPolicyService = verificationPolicyService;
        this.securityAuthorizationService = securityAuthorizationService;
    }

    @GetMapping
    @Operation(summary = "openapi.settings.verification-settings.get.summary")
    public Result<List<VerificationPolicyRespVO>> list() {
        securityAuthorizationService.checkAnyPermission(SCENE_PERMISSIONS.values());
        return Result.ok(verificationPolicyService.listScenes());
    }

    @PutMapping("/{sceneKey}")
    @Operation(summary = "openapi.settings.verification-settings.update.summary")
    public Result<Boolean> update(@PathVariable("sceneKey") String sceneKey,
                                  @Valid @RequestBody VerificationPolicyUpdateCommand command) {
        String permission = SCENE_PERMISSIONS.get(sceneKey);
        if (permission == null) {
            throw new ApiException(BusinessErrorCode.DATA_NOT_FOUND);
        }
        securityAuthorizationService.checkPermission(permission);
        verificationPolicyService.updateScene(sceneKey, command);
        return Result.ok(true);
    }
}
