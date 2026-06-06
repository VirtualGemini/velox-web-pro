package com.velox.module.system.mail.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "openapi.system.mail_template.preview_sample_resp.schema")
public class MailTemplatePreviewSampleRespVO {

    @Schema(description = "openapi.common.name")
    private String usernameZh;

    @Schema(description = "openapi.common.name")
    private String usernameEn;

    @Schema(description = "openapi.system.auth.code_login_command.code")
    private String code;

    @Schema(description = "openapi.common.name")
    private String appName;

    @Schema(description = "openapi.system.mail_template.field.from_address")
    private String fromAddress;

    @Schema(description = "openapi.system.mail_template.field.to_address")
    private String toAddress;

    public String getUsernameZh() {
        return usernameZh;
    }

    public void setUsernameZh(String usernameZh) {
        this.usernameZh = usernameZh;
    }

    public String getUsernameEn() {
        return usernameEn;
    }

    public void setUsernameEn(String usernameEn) {
        this.usernameEn = usernameEn;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }
}
