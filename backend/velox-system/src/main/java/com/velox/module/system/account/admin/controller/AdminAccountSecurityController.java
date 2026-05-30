package com.velox.module.system.account.admin.controller;

import com.velox.common.result.Result;
import com.velox.framework.security.api.annotation.RequirePermission;
import com.velox.module.system.account.admin.service.AdminAccountSecurityService;
import com.velox.module.system.account.dto.AdminLoginMethodsCommand;
import com.velox.module.system.account.dto.AdminMfaEmailCommand;
import com.velox.module.system.account.dto.AdminPasswordResetCommand;
import com.velox.module.system.account.dto.AdminSecurityEmailCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "openapi.system.account.security.manage.tag.name", description = "openapi.system.account.security.manage.tag.description")
@RestController
@RequestMapping("/account/{accountId}/security")
public class AdminAccountSecurityController {

    private final AdminAccountSecurityService adminAccountSecurityService;

    public AdminAccountSecurityController(AdminAccountSecurityService adminAccountSecurityService) {
        this.adminAccountSecurityService = adminAccountSecurityService;
    }

    @Operation(summary = "openapi.system.account.security.manage.password.summary")
    @RequirePermission("system:account:update")
    @PutMapping("/password")
    public Result<Boolean> resetPassword(@PathVariable("accountId") String accountId,
                                         @Valid @RequestBody AdminPasswordResetCommand command) {
        return Result.ok(adminAccountSecurityService.resetPassword(accountId, command));
    }

    @Operation(summary = "openapi.system.account.security.manage.email.summary")
    @RequirePermission("system:account:update")
    @PutMapping("/email")
    public Result<Boolean> updateSecurityEmail(@PathVariable("accountId") String accountId,
                                               @Valid @RequestBody AdminSecurityEmailCommand command) {
        return Result.ok(adminAccountSecurityService.updateSecurityEmail(accountId, command));
    }

    @Operation(summary = "openapi.system.account.security.manage.mfa_email.summary")
    @RequirePermission("system:account:update")
    @PutMapping("/mfa/email")
    public Result<Boolean> updateMfaEmail(@PathVariable("accountId") String accountId,
                                          @RequestBody AdminMfaEmailCommand command) {
        return Result.ok(adminAccountSecurityService.updateMfaEmail(accountId, command));
    }

    @Operation(summary = "openapi.system.account.security.manage.totp_disable.summary")
    @RequirePermission("system:account:update")
    @PutMapping("/mfa/totp/disable")
    public Result<Boolean> disableTotp(@PathVariable("accountId") String accountId) {
        return Result.ok(adminAccountSecurityService.disableTotp(accountId));
    }

    @Operation(summary = "openapi.system.account.security.manage.login_methods.summary")
    @RequirePermission("system:account:update")
    @PutMapping("/login-methods")
    public Result<Boolean> updateLoginMethods(@PathVariable("accountId") String accountId,
                                              @RequestBody AdminLoginMethodsCommand command) {
        return Result.ok(adminAccountSecurityService.updateLoginMethods(accountId, command));
    }
}
