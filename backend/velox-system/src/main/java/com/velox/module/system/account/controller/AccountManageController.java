package com.velox.module.system.account.controller;

import com.velox.common.result.Result;
import com.velox.framework.security.api.annotation.RequirePermission;
import com.velox.module.system.id.frontend.SystemFrontendIdCodecSupport;
import com.velox.module.system.log.annotation.OperationLog;
import com.velox.module.system.log.annotation.OperationType;
import com.velox.module.system.account.dto.AccountDetailCardDTO;
import com.velox.module.system.account.dto.AccountListItemDTO;
import com.velox.module.system.account.dto.AccountQuery;
import com.velox.module.system.account.dto.AccountSaveCommand;
import com.velox.module.system.account.dto.AdminProfileUpdateCommand;
import com.velox.module.system.account.service.AccountManageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.velox.common.result.PageResult;

import java.util.List;

@Tag(name = "openapi.system.account.manage.tag.name", description = "openapi.system.account.manage.tag.description")
@RestController
@RequestMapping("/account")
public class AccountManageController {

    private final AccountManageService accountManageService;
    private final SystemFrontendIdCodecSupport frontendIdCodecSupport;

    public AccountManageController(
            AccountManageService accountManageService,
            SystemFrontendIdCodecSupport frontendIdCodecSupport
    ) {
        this.accountManageService = accountManageService;
        this.frontendIdCodecSupport = frontendIdCodecSupport;
    }

    @Operation(summary = "openapi.system.account.manage.list.summary")
    @RequirePermission("system:account:query")
    @OperationLog(
            module = "system.account",
            action = "query",
            type = OperationType.QUERY,
            queryParamNames = {
                    "username", "email", "remark", "status", "activeStatus",
                    "createTimeStart", "createTimeEnd", "updateTimeStart", "updateTimeEnd"
            }
    )
    @GetMapping("/list")
    public Result<PageResult<AccountListItemDTO>> list(AccountQuery query) {
        return Result.ok(accountManageService.list(query));
    }

    @Operation(summary = "openapi.system.account.manage.detail_card.summary")
    @RequirePermission("system:account:query")
    @GetMapping("/{accountId}/detail-card")
    public Result<AccountDetailCardDTO> getDetailCard(@PathVariable("accountId") String accountId) {
        return Result.ok(accountManageService.getDetailCard(accountId));
    }

    @Operation(summary = "openapi.system.account.manage.create.summary")
    @RequirePermission("system:account:create")
    @OperationLog(module = "system.account", action = "create", type = OperationType.CREATE, excludeParamNames = {"password"})
    @PostMapping
    public Result<String> create(@Valid @RequestBody AccountSaveCommand command) {
        return Result.ok(frontendIdCodecSupport.encodeIdentifier(accountManageService.create(command)));
    }

    @Operation(summary = "openapi.system.account.manage.update.summary")
    @RequirePermission("system:account:update")
    @OperationLog(module = "system.account", action = "update", type = OperationType.UPDATE, targetType = "account", targetIdExpression = "#arg0", excludeParamNames = {"password"})
    @PutMapping("/{accountId}")
    public Result<Boolean> update(@PathVariable("accountId") String accountId, @Valid @RequestBody AccountSaveCommand command) {
        return Result.ok(accountManageService.update(accountId, command));
    }

    @Operation(summary = "openapi.system.account.manage.update_profile.summary")
    @RequirePermission("system:account:update")
    @OperationLog(module = "system.account", action = "update_profile", type = OperationType.UPDATE, targetType = "account", targetIdExpression = "#arg0")
    @PutMapping("/{accountId}/profile")
    public Result<Boolean> updateProfile(@PathVariable("accountId") String accountId, @Valid @RequestBody AdminProfileUpdateCommand command) {
        return Result.ok(accountManageService.updateProfile(accountId, command));
    }

    @Operation(summary = "openapi.system.account.manage.delete.summary")
    @RequirePermission("system:account:delete")
    @OperationLog(module = "system.account", action = "delete", type = OperationType.DELETE, targetType = "account", targetIdExpression = "#arg0")
    @DeleteMapping("/{accountId}")
    public Result<Boolean> delete(@PathVariable("accountId") String accountId) {
        return Result.ok(accountManageService.delete(accountId));
    }

    @Operation(summary = "openapi.system.account.manage.delete_batch.summary")
    @RequirePermission("system:account:delete")
    @OperationLog(module = "system.account", action = "delete_batch", type = OperationType.DELETE)
    @DeleteMapping("/delete-batch")
    public Result<Boolean> deleteBatch(@RequestParam("ids") List<String> ids) {
        return Result.ok(accountManageService.deleteBatch(ids));
    }

    @Operation(summary = "openapi.system.account.manage.cancel_batch.summary")
    @RequirePermission("system:account:update")
    @OperationLog(module = "system.account", action = "cancel_batch", type = OperationType.UPDATE)
    @PutMapping("/cancel-batch")
    public Result<Boolean> cancelBatch(@RequestParam("ids") List<String> ids) {
        return Result.ok(accountManageService.cancelBatch(ids));
    }
}
