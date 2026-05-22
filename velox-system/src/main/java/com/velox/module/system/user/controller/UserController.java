package com.velox.module.system.user.controller;

import com.velox.common.result.Result;
import com.velox.module.system.user.dto.AvatarUpdateCommand;
import com.velox.module.system.user.dto.LanguageUpdateCommand;
import com.velox.module.system.user.dto.UserInfoDTO;
import com.velox.module.system.user.dto.UserPasswordUpdateCommand;
import com.velox.module.system.user.dto.UserProfileUpdateCommand;
import com.velox.module.system.user.service.UserInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户信息", description = "当前登录用户信息接口")
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserInfoService userInfoService;

    public UserController(UserInfoService userInfoService) {
        this.userInfoService = userInfoService;
    }

    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/info")
    public Result<UserInfoDTO> getUserInfo() {
        return Result.ok(userInfoService.getUserInfoDTO());
    }

    @Operation(summary = "更新当前登录用户资料")
    @PutMapping("/profile")
    public Result<Boolean> updateProfile(@Valid @RequestBody UserProfileUpdateCommand command) {
        return Result.ok(userInfoService.updateCurrentUserProfile(command));
    }

    @Operation(summary = "修改当前登录用户密码")
    @PutMapping("/password")
    public Result<Boolean> updatePassword(@Valid @RequestBody UserPasswordUpdateCommand command) {
        return Result.ok(userInfoService.updateCurrentUserPassword(command));
    }

    @Operation(summary = "更新当前登录用户头像")
    @PutMapping("/avatar")
    public Result<UserInfoDTO> updateAvatar(@RequestBody AvatarUpdateCommand command) {
        userInfoService.updateCurrentUserAvatar(command.getAvatarUrl());
        return Result.ok(userInfoService.getUserInfoDTO());
    }

    @Operation(summary = "更新当前登录用户语言偏好")
    @PutMapping("/language")
    public Result<Boolean> updateLanguage(@Valid @RequestBody LanguageUpdateCommand command) {
        return Result.ok(userInfoService.updateCurrentUserLanguage(command.getLanguage()));
    }
}
