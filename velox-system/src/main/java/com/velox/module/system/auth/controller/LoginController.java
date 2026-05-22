package com.velox.module.system.auth.controller;

import com.velox.common.result.Result;
import com.velox.module.system.auth.dto.CaptchaDTO;
import com.velox.module.system.auth.dto.ForgotPasswordCodeCommand;
import com.velox.module.system.auth.dto.LoginCommand;
import com.velox.module.system.auth.dto.RegisterCommand;
import com.velox.module.system.auth.dto.ResetPasswordCommand;
import com.velox.module.system.auth.dto.TokenDTO;
import com.velox.module.system.auth.service.LoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "登录认证", description = "登录认证相关接口")
@RestController
@RequestMapping("/auth")
public class LoginController {

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @Operation(summary = "获取验证码")
    @GetMapping("/captcha")
    public Result<CaptchaDTO> getCaptcha() {
        return Result.ok(loginService.generateCaptcha());
    }

    @Operation(summary = "登录")
    @PostMapping("/login")
    public Result<TokenDTO> login(@Valid @RequestBody LoginCommand command) {
        return Result.ok(loginService.login(command));
    }

    @Operation(summary = "注册")
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterCommand command) {
        loginService.register(command);
        return Result.ok();
    }

    @Operation(summary = "发送找回密码验证码")
    @PostMapping("/forgot-password/code")
    public Result<Void> sendResetPasswordCode(@Valid @RequestBody ForgotPasswordCodeCommand command) {
        loginService.sendResetPasswordCode(command);
        return Result.ok();
    }

    @Operation(summary = "通过邮箱验证码重置密码")
    @PostMapping("/forgot-password/reset")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordCommand command) {
        loginService.resetPassword(command);
        return Result.ok();
    }

    @Operation(summary = "登出")
    @PostMapping("/logout")
    public Result<Void> logout() {
        loginService.logout();
        return Result.ok();
    }
}
