package com.velox.module.system.account.admin.controller;

import com.velox.common.result.Result;
import com.velox.framework.security.api.annotation.RequirePermission;
import com.velox.module.system.account.admin.service.AdminAccountSecurityService;
import com.velox.module.system.account.dto.AdminOauthChannelCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "openapi.system.account.thirdparty.manage.tag.name", description = "openapi.system.account.thirdparty.manage.tag.description")
@RestController
@RequestMapping("/account/{accountId}/third-party")
public class AdminThirdPartyController {

    private final AdminAccountSecurityService adminAccountSecurityService;

    public AdminThirdPartyController(AdminAccountSecurityService adminAccountSecurityService) {
        this.adminAccountSecurityService = adminAccountSecurityService;
    }

    @Operation(summary = "openapi.system.account.thirdparty.manage.toggle.summary")
    @RequirePermission("system:account:update")
    @PutMapping("/{channel}/disabled")
    public Result<Boolean> toggleChannel(@PathVariable("accountId") String accountId,
                                         @PathVariable("channel") String channel,
                                         @RequestBody AdminOauthChannelCommand command) {
        return Result.ok(adminAccountSecurityService.toggleOauthChannel(accountId, channel, command));
    }

    @Operation(summary = "openapi.system.account.thirdparty.manage.unbind.summary")
    @RequirePermission("system:account:update")
    @DeleteMapping("/{channel}")
    public Result<Boolean> unbindChannel(@PathVariable("accountId") String accountId,
                                         @PathVariable("channel") String channel) {
        return Result.ok(adminAccountSecurityService.unbindOauthChannel(accountId, channel));
    }
}
