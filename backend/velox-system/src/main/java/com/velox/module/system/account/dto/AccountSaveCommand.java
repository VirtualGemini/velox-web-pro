package com.velox.module.system.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public class AccountSaveCommand {

    @NotBlank(message = "{validation.system.user.save.username.not_blank}")
    @Size(min = 2, max = 50, message = "{validation.system.user.save.username.size}")
    private String username;

    @Size(min = 6, max = 32, message = "{validation.system.user.save.password.size}")
    private String password;

    @Size(max = 255, message = "{validation.system.user.save.remark.size}")
    private String remark;

    @NotEmpty(message = "{validation.system.user.save.role_codes.not_empty}")
    private List<String> roleCodes;

    /** 账号状态(1-启用 2-禁用 3-异常 4-注销)，仅编辑时生效，新增忽略并默认启用 */
    private Integer accountStatus;

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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public List<String> getRoleCodes() {
        return roleCodes;
    }

    public void setRoleCodes(List<String> roleCodes) {
        this.roleCodes = roleCodes;
    }

    public Integer getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(Integer accountStatus) {
        this.accountStatus = accountStatus;
    }
}
