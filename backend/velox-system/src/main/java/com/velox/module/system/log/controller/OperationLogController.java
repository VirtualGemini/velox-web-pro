package com.velox.module.system.log.controller;

import com.velox.common.result.PageResult;
import com.velox.common.result.Result;
import com.velox.framework.security.api.annotation.RequirePermission;
import com.velox.module.system.log.annotation.OperationLog;
import com.velox.module.system.log.annotation.OperationType;
import com.velox.module.system.log.dto.OperationLogDTO;
import com.velox.module.system.log.dto.OperationLogQuery;
import com.velox.module.system.log.service.OperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "openapi.system.log.operation.tag.name")
@RestController
@RequestMapping("/log/operation")
public class OperationLogController {

    private final OperationLogService operationLogService;

    public OperationLogController(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @Operation(summary = "openapi.system.log.operation.list.summary")
    @RequirePermission("system:log:operation:query")
    @OperationLog(
            module = "system.log.operation",
            action = "query",
            type = OperationType.QUERY,
            queryParamNames = {
                    "username", "accountId", "result", "clientIp", "countryName", "provinceName",
                    "cityName", "traceId", "moduleName", "actionName", "operationType",
                    "operationTimeStart", "operationTimeEnd"
            }
    )
    @GetMapping("/list")
    public Result<PageResult<OperationLogDTO>> list(OperationLogQuery query) {
        return Result.ok(operationLogService.list(query));
    }

    @Operation(summary = "openapi.system.log.operation.get.summary")
    @RequirePermission("system:log:operation:query")
    @GetMapping("/{id}")
    public Result<OperationLogDTO> get(@PathVariable("id") String id) {
        return Result.ok(operationLogService.get(id));
    }

    @Operation(summary = "openapi.system.log.operation.delete_batch.summary")
    @OperationLog(module = "system.log.operation", action = "delete_batch", type = OperationType.DELETE)
    @RequirePermission("system:log:operation:delete")
    @DeleteMapping("/delete-batch")
    public Result<Boolean> deleteBatch(@RequestParam("ids") List<String> ids) {
        return Result.ok(operationLogService.deleteBatch(ids));
    }

    @Operation(summary = "openapi.system.log.operation.clean.summary")
    @OperationLog(module = "system.log.operation", action = "clean", type = OperationType.CLEAN, saveRequest = false)
    @RequirePermission("system:log:operation:clean")
    @DeleteMapping("/clean")
    public Result<Boolean> clean() {
        return Result.ok(operationLogService.clean());
    }
}
