package com.velox.module.system.accesscontrol.domain.model;

import com.velox.domain.model.BaseEntity;

import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Objects;

/**
 * 访问控制全局配置（单例）
 * <p>
 * 统一管理「未授权时可访问页面」的全局开关：通用注册、忘记密码、
 * 普通登录方式、第三方登录/注册渠道。被关闭的功能在前端不展示入口，
 * 同时后端拒绝相关请求（双重保护）。
 */
@TableName(value = "sys_access_control")
public class AccessControlConfig extends BaseEntity {

    /** 通用注册总开关 (0-关闭 1-开启) */
    private Integer generalRegisterEnabled;

    /** 忘记密码总开关 (0-关闭 1-开启) */
    private Integer forgotPasswordEnabled;

    /** 已启用的普通登录方式 CSV（如 password,email_code） */
    private String loginMethods;

    /** 已启用的第三方登录渠道 CSV（如 github,linuxdo） */
    private String thirdPartyLoginChannels;

    /** 已启用的第三方注册渠道 CSV */
    private String thirdPartyRegisterChannels;

    public Integer getGeneralRegisterEnabled() {
        return generalRegisterEnabled;
    }

    public void setGeneralRegisterEnabled(Integer generalRegisterEnabled) {
        this.generalRegisterEnabled = generalRegisterEnabled;
    }

    public Integer getForgotPasswordEnabled() {
        return forgotPasswordEnabled;
    }

    public void setForgotPasswordEnabled(Integer forgotPasswordEnabled) {
        this.forgotPasswordEnabled = forgotPasswordEnabled;
    }

    public String getLoginMethods() {
        return loginMethods;
    }

    public void setLoginMethods(String loginMethods) {
        this.loginMethods = loginMethods;
    }

    public String getThirdPartyLoginChannels() {
        return thirdPartyLoginChannels;
    }

    public void setThirdPartyLoginChannels(String thirdPartyLoginChannels) {
        this.thirdPartyLoginChannels = thirdPartyLoginChannels;
    }

    public String getThirdPartyRegisterChannels() {
        return thirdPartyRegisterChannels;
    }

    public void setThirdPartyRegisterChannels(String thirdPartyRegisterChannels) {
        this.thirdPartyRegisterChannels = thirdPartyRegisterChannels;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AccessControlConfig that = (AccessControlConfig) o;
        return Objects.equals(generalRegisterEnabled, that.generalRegisterEnabled) &&
                Objects.equals(forgotPasswordEnabled, that.forgotPasswordEnabled) &&
                Objects.equals(loginMethods, that.loginMethods) &&
                Objects.equals(thirdPartyLoginChannels, that.thirdPartyLoginChannels) &&
                Objects.equals(thirdPartyRegisterChannels, that.thirdPartyRegisterChannels);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), generalRegisterEnabled, forgotPasswordEnabled,
                loginMethods, thirdPartyLoginChannels, thirdPartyRegisterChannels);
    }
}
