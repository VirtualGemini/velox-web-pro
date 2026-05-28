package com.velox.module.system.account.dto;

import java.util.List;

/**
 * 维持系统正常运行所需的最小账号信息：
 * 权限标识（buttons）、角色（roles）、基础身份与联系方式、语言偏好。
 * 详细资料请通过 /account/detail 获取。
 */
public class AccountInfoBasicDTO {
    private List<String> buttons;
    private List<String> roles;
    private String accountId;
    private String userName;
    private String email;
    private String phone;
    private String avatar;
    private String language;

    public List<String> getButtons() {
        return buttons;
    }

    public void setButtons(List<String> buttons) {
        this.buttons = buttons;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
