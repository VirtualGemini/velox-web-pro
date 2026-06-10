package com.velox.module.system.log.controller;

import com.velox.common.result.PageResult;
import com.velox.common.result.Result;
import com.velox.framework.security.api.annotation.RequirePermission;
import com.velox.module.system.log.annotation.OperationLog;
import com.velox.module.system.log.annotation.OperationType;
import com.velox.module.system.log.dto.LoginLogDTO;
import com.velox.module.system.log.dto.LoginLogQuery;
import com.velox.module.system.log.service.LoginLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "openapi.system.log.login.tag.name")
@RestController
@RequestMapping("/log/login")
public class LoginLogController {

    private final LoginLogService loginLogService;

    public LoginLogController(LoginLogService loginLogService) {
        this.loginLogService = loginLogService;
    }

    @Operation(summary = "openapi.system.log.login.list.summary")
    @RequirePermission("system:log:login:query")
    @OperationLog(
            module = "system.log.login",
            action = "query",
            type = OperationType.QUERY,
            queryParamNames = {
                    "username", "accountId", "result", "clientIp", "countryName", "provinceName",
                    "cityName", "traceId", "eventType", "loginMethod", "mfaType", "failureCode",
                    "eventTimeStart", "eventTimeEnd"
            }
    )
    @GetMapping("/list")
    public Result<PageResult<LoginLogDTO>> list(LoginLogQuery query) {
        return Result.ok(loginLogService.list(query));
    }

    @Operation(summary = "openapi.system.log.login.get.summary")
    @RequirePermission("system:log:login:query")
    @GetMapping("/{id}")
    public Result<LoginLogDTO> get(@PathVariable("id") String id) {
        return Result.ok(loginLogService.get(id));
    }

    @Operation(summary = "openapi.system.log.login.delete_batch.summary")
    @OperationLog(module = "system.log.login", action = "delete_batch", type = OperationType.DELETE)
    @RequirePermission("system:log:login:delete")
    @DeleteMapping("/delete-batch")
    public Result<Boolean> deleteBatch(@RequestParam("ids") List<String> ids) {
        return Result.ok(loginLogService.deleteBatch(ids));
    }

    @Operation(summary = "openapi.system.log.login.clean.summary")
    @OperationLog(module = "system.log.login", action = "clean", type = OperationType.CLEAN, saveRequest = false)
    @RequirePermission("system:log:login:clean")
    @DeleteMapping("/clean")
    public Result<Boolean> clean() {
        return Result.ok(loginLogService.clean());
    }
}
