package com.velox.module.system.auth.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "登录请求")
public class LoginCommand {

    @Schema(description = "用户名", example = "admin")
    @JsonAlias("userName")
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20个字符之间")
    private String username;

    @Schema(description = "密码", example = "123456")
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 64, message = "密码长度必须在6-64个字符之间")
    private String password;

    @Schema(description = "验证码", example = "aB7D")
    private String captchaCode;

    @Schema(description = "验证码Key")
    private String captchaCodeKey;

    public LoginCommand() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCaptchaCode() {
        return captchaCode;
    }

    public void setCaptchaCode(String captchaCode) {
        this.captchaCode = captchaCode;
    }

    public String getCaptchaCodeKey() {
        return captchaCodeKey;
    }

    public void setCaptchaCodeKey(String captchaCodeKey) {
        this.captchaCodeKey = captchaCodeKey;
    }
}
