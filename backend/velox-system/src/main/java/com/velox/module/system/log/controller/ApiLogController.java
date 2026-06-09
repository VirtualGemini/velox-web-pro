package com.velox.module.system.log.controller;

import com.velox.common.result.PageResult;
import com.velox.common.result.Result;
import com.velox.framework.security.api.annotation.RequirePermission;
import com.velox.module.system.log.annotation.OperationLog;
import com.velox.module.system.log.annotation.OperationType;
import com.velox.module.system.log.dto.ApiLogDTO;
import com.velox.module.system.log.dto.ApiLogQuery;
import com.velox.module.system.log.service.ApiLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "openapi.system.log.api.tag.name")
@RestController
@RequestMapping("/log/api")
public class ApiLogController {

    private final ApiLogService apiLogService;

    public ApiLogController(ApiLogService apiLogService) {
        this.apiLogService = apiLogService;
    }

    @Operation(summary = "openapi.system.log.api.list.summary")
    @RequirePermission("system:log:api:query")
    @OperationLog(
            module = "system.log.api",
            action = "query",
            type = OperationType.QUERY,
            queryParamNames = {
                    "username", "accountId", "result", "clientIp", "countryName", "provinceName",
                    "cityName", "traceId", "requestMethod", "requestUri", "httpStatus",
                    "businessCode", "costTimeMin", "costTimeMax", "apiTimeStart", "apiTimeEnd"
            }
    )
    @GetMapping("/list")
    public Result<PageResult<ApiLogDTO>> list(ApiLogQuery query) {
        return Result.ok(apiLogService.list(query));
    }

    @Operation(summary = "openapi.system.log.api.get.summary")
    @RequirePermission("system:log:api:query")
    @GetMapping("/{id}")
    public Result<ApiLogDTO> get(@PathVariable("id") String id) {
        return Result.ok(apiLogService.get(id));
    }

    @Operation(summary = "openapi.system.log.api.delete_batch.summary")
    @OperationLog(module = "system.log.api", action = "delete_batch", type = OperationType.DELETE)
    @RequirePermission("system:log:api:delete")
    @DeleteMapping("/delete-batch")
    public Result<Boolean> deleteBatch(@RequestParam("ids") List<String> ids) {
        return Result.ok(apiLogService.deleteBatch(ids));
    }

    @Operation(summary = "openapi.system.log.api.clean.summary")
    @OperationLog(module = "system.log.api", action = "clean", type = OperationType.CLEAN, saveRequest = false)
    @RequirePermission("system:log:api:clean")
    @DeleteMapping("/clean")
    public Result<Boolean> clean() {
        return Result.ok(apiLogService.clean());
    }
}
