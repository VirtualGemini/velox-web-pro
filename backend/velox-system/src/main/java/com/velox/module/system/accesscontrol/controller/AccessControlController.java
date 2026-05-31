package com.velox.module.system.accesscontrol.controller;

import com.velox.common.result.Result;
import com.velox.framework.security.api.annotation.RequirePermission;
import com.velox.module.system.accesscontrol.dto.AccessControlChannelsCommand;
import com.velox.module.system.accesscontrol.dto.AccessControlToggleCommand;
import com.velox.module.system.accesscontrol.service.AccessControlService;
import com.velox.module.system.accesscontrol.vo.AccessControlRespVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "openapi.settings.access-control.tag.name", description = "openapi.settings.access-control.tag.description")
@RestController
@RequestMapping("/settings/access-control")
@Validated
public class AccessControlController {

    private final AccessControlService accessControlService;

    public AccessControlController(AccessControlService accessControlService) {
        this.accessControlService = accessControlService;
    }

    @GetMapping
    @Operation(summary = "openapi.settings.access-control.get.summary")
    @RequirePermission("settings:access-control:query")
    public Result<AccessControlRespVO> getConfig() {
        return Result.ok(accessControlService.getConfig());
    }

    @PutMapping("/general-register")
    @Operation(summary = "openapi.settings.access-control.general_register.summary")
    @RequirePermission("settings:access-control:general-register")
    public Result<Boolean> updateGeneralRegister(@Valid @RequestBody AccessControlToggleCommand command) {
        accessControlService.updateGeneralRegister(command.getEnabled());
        return Result.ok(true);
    }

    @PutMapping("/forgot-password")
    @Operation(summary = "openapi.settings.access-control.forgot_password.summary")
    @RequirePermission("settings:access-control:forgot-password")
    public Result<Boolean> updateForgotPassword(@Valid @RequestBody AccessControlToggleCommand command) {
        accessControlService.updateForgotPassword(command.getEnabled());
        return Result.ok(true);
    }

    @PutMapping("/login-method")
    @Operation(summary = "openapi.settings.access-control.login_method.summary")
    @RequirePermission("settings:access-control:login-method")
    public Result<Boolean> updateLoginMethods(@Valid @RequestBody AccessControlChannelsCommand command) {
        accessControlService.updateLoginMethods(command.getValues());
        return Result.ok(true);
    }

    @PutMapping("/third-party-login")
    @Operation(summary = "openapi.settings.access-control.third_party_login.summary")
    @RequirePermission("settings:access-control:third-party-login")
    public Result<Boolean> updateThirdPartyLogin(@Valid @RequestBody AccessControlChannelsCommand command) {
        accessControlService.updateThirdPartyLogin(command.getValues());
        return Result.ok(true);
    }

    @PutMapping("/third-party-register")
    @Operation(summary = "openapi.settings.access-control.third_party_register.summary")
    @RequirePermission("settings:access-control:third-party-register")
    public Result<Boolean> updateThirdPartyRegister(@Valid @RequestBody AccessControlChannelsCommand command) {
        accessControlService.updateThirdPartyRegister(command.getValues());
        return Result.ok(true);
    }
}
