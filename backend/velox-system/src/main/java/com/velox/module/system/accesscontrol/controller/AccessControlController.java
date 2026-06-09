package com.velox.module.system.accesscontrol.controller;

import com.velox.common.result.Result;
import com.velox.framework.security.api.annotation.RequirePermission;
import com.velox.framework.security.api.authorization.SecurityAuthorizationService;
import com.velox.module.system.accesscontrol.dto.AccessControlChannelsCommand;
import com.velox.module.system.accesscontrol.dto.AccessControlToggleCommand;
import com.velox.module.system.accesscontrol.service.AccessControlService;
import com.velox.module.system.accesscontrol.vo.AccessControlRespVO;
import com.velox.module.system.log.annotation.OperationLog;
import com.velox.module.system.log.annotation.OperationType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "openapi.settings.access-control.tag.name", description = "openapi.settings.access-control.tag.description")
@RestController
@RequestMapping("/settings/access-control")
@Validated
public class AccessControlController {

    /** 访问控制的管理权限集合：持有任意一个即可读取配置（已取消独立查询权限）。 */
    private static final List<String> MANAGE_PERMISSIONS = List.of(
            "settings:access-control:general-register",
            "settings:access-control:forgot-password",
            "settings:access-control:login-method",
            "settings:access-control:third-party-login",
            "settings:access-control:third-party-register"
    );

    private final AccessControlService accessControlService;
    private final SecurityAuthorizationService securityAuthorizationService;

    public AccessControlController(AccessControlService accessControlService,
                                  SecurityAuthorizationService securityAuthorizationService) {
        this.accessControlService = accessControlService;
        this.securityAuthorizationService = securityAuthorizationService;
    }

    @GetMapping
    @Operation(summary = "openapi.settings.access-control.get.summary")
    public Result<AccessControlRespVO> getConfig() {
        securityAuthorizationService.checkAnyPermission(MANAGE_PERMISSIONS);
        return Result.ok(accessControlService.getConfig());
    }

    @PutMapping("/general-register")
    @Operation(summary = "openapi.settings.access-control.general_register.summary")
    @RequirePermission("settings:access-control:general-register")
    @OperationLog(module = "settings.access_control", action = "update_general_register", type = OperationType.UPDATE)
    public Result<Boolean> updateGeneralRegister(@Valid @RequestBody AccessControlToggleCommand command) {
        accessControlService.updateGeneralRegister(command.getEnabled());
        return Result.ok(true);
    }

    @PutMapping("/forgot-password")
    @Operation(summary = "openapi.settings.access-control.forgot_password.summary")
    @RequirePermission("settings:access-control:forgot-password")
    @OperationLog(module = "settings.access_control", action = "update_forgot_password", type = OperationType.UPDATE)
    public Result<Boolean> updateForgotPassword(@Valid @RequestBody AccessControlToggleCommand command) {
        accessControlService.updateForgotPassword(command.getEnabled());
        return Result.ok(true);
    }

    @PutMapping("/login-method")
    @Operation(summary = "openapi.settings.access-control.login_method.summary")
    @RequirePermission("settings:access-control:login-method")
    @OperationLog(module = "settings.access_control", action = "update_login_method", type = OperationType.UPDATE)
    public Result<Boolean> updateLoginMethods(@Valid @RequestBody AccessControlChannelsCommand command) {
        accessControlService.updateLoginMethods(command.getValues());
        return Result.ok(true);
    }

    @PutMapping("/third-party-login")
    @Operation(summary = "openapi.settings.access-control.third_party_login.summary")
    @RequirePermission("settings:access-control:third-party-login")
    @OperationLog(module = "settings.access_control", action = "update_third_party_login", type = OperationType.UPDATE)
    public Result<Boolean> updateThirdPartyLogin(@Valid @RequestBody AccessControlChannelsCommand command) {
        accessControlService.updateThirdPartyLogin(command.getValues());
        return Result.ok(true);
    }

    @PutMapping("/third-party-register")
    @Operation(summary = "openapi.settings.access-control.third_party_register.summary")
    @RequirePermission("settings:access-control:third-party-register")
    @OperationLog(module = "settings.access_control", action = "update_third_party_register", type = OperationType.UPDATE)
    public Result<Boolean> updateThirdPartyRegister(@Valid @RequestBody AccessControlChannelsCommand command) {
        accessControlService.updateThirdPartyRegister(command.getValues());
        return Result.ok(true);
    }
}
