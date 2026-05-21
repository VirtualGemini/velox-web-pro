package com.velox.module.system.menu.controller;

import com.velox.common.result.Result;
import com.velox.framework.security.api.annotation.RequireAuthenticated;
import com.velox.framework.security.api.annotation.RequirePermission;
import com.velox.module.system.id.frontend.SystemFrontendIdCodecSupport;
import com.velox.module.system.menu.dto.MenuSaveCommand;
import com.velox.module.system.menu.dto.MenuRouteDTO;
import com.velox.module.system.menu.service.MenuService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "菜单管理", description = "菜单管理相关接口")
@RestController
@RequestMapping("/v3/system/menus")
public class MenuController {

    private final MenuService menuService;
    private final SystemFrontendIdCodecSupport frontendIdCodecSupport;

    public MenuController(
            MenuService menuService,
            SystemFrontendIdCodecSupport frontendIdCodecSupport
    ) {
        this.menuService = menuService;
        this.frontendIdCodecSupport = frontendIdCodecSupport;
    }

    @Operation(summary = "获取简化菜单列表")
    @RequirePermission("system:menu:query")
    @GetMapping("/simple")
    public Result<List<MenuRouteDTO>> getSimpleMenus() {
        return Result.ok(menuService.getSimpleMenus());
    }

    @Operation(summary = "新增菜单")
    @RequirePermission("system:menu:create")
    @PostMapping
    public Result<String> create(@Valid @RequestBody MenuSaveCommand command) {
        return Result.ok(frontendIdCodecSupport.encodeIdentifier(menuService.create(command)));
    }

    @Operation(summary = "编辑菜单")
    @RequirePermission("system:menu:update")
    @PutMapping("/{menuId}")
    public Result<Boolean> update(@PathVariable("menuId") String menuId, @Valid @RequestBody MenuSaveCommand command) {
        return Result.ok(menuService.update(menuId, command));
    }

    @Operation(summary = "删除菜单")
    @RequirePermission("system:menu:delete")
    @DeleteMapping("/{menuId}")
    public Result<Boolean> delete(@PathVariable("menuId") String menuId) {
        return Result.ok(menuService.delete(menuId));
    }

}
