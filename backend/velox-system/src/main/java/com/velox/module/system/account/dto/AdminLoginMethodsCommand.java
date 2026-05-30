package com.velox.module.system.account.dto;

import java.util.List;

/**
 * 管理员设置他人登录方式。enabledMethods 为该用户已开启的方式，
 * disabledMethods 为管理员禁用的方式（用户不可开启或使用）。两者不可交集。
 */
public class AdminLoginMethodsCommand {

    private List<String> enabledMethods;

    private List<String> disabledMethods;

    public List<String> getEnabledMethods() {
        return enabledMethods;
    }

    public void setEnabledMethods(List<String> enabledMethods) {
        this.enabledMethods = enabledMethods;
    }

    public List<String> getDisabledMethods() {
        return disabledMethods;
    }

    public void setDisabledMethods(List<String> disabledMethods) {
        this.disabledMethods = disabledMethods;
    }
}
