package com.velox.module.system.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AccountUsernameUpdateCommand {

    @NotBlank(message = "{validation.system.user.save.username.not_blank}")
    @Size(min = 2, max = 50, message = "{validation.system.user.save.username.size}")
    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
