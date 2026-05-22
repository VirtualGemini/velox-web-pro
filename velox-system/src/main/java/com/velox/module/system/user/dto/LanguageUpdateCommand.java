package com.velox.module.system.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class LanguageUpdateCommand {

    @NotBlank(message = "语言不能为空")
    @Pattern(regexp = "^(zh|en)$", message = "语言仅支持 zh / en")
    private String language;

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
