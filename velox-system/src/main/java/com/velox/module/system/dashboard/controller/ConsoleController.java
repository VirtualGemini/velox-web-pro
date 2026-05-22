package com.velox.module.system.dashboard.controller;

import com.velox.common.result.Result;
import com.velox.framework.security.api.annotation.RequirePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "工作台", description = "工作台相关接口")
@RestController
@RequestMapping("/dashboard/console")
public class ConsoleController {

    @Operation(summary = "工作台访问校验")
    @GetMapping("/access")
    @RequirePermission("dashboard:console")
    public Result<Boolean> access() {
        return Result.ok(true);
    }
}
