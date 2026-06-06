package com.velox.module.system.mail.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

@Schema(description = "openapi.system.mail_template.save_req.schema")
public class MailTemplateSaveReqVO {

    @Schema(description = "openapi.common.id")
    private String id;

    @Schema(description = "openapi.system.mail_template.field.name", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{validation.system.mail_template.name.not_empty}")
    private String name;

    @Schema(description = "openapi.system.mail_template.field.send_type", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{validation.system.mail_template.type.not_empty}")
    private String sendType;

    @Schema(description = "openapi.system.mail_template.field.template_type")
    private String templateType;

    @Schema(description = "openapi.system.mail_template.field.subject", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{validation.system.mail_template.subject.not_empty}")
    private String subject;

    @Schema(description = "openapi.system.mail_template.field.content", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{validation.system.mail_template.content.not_empty}")
    private String content;

    @Schema(description = "openapi.common.enabled")
    private Integer enabled;

    @Schema(description = "openapi.system.mail_template.field.sort")
    private Integer sort;

    @Schema(description = "openapi.common.remark")
    private String remark;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSendType() {
        return sendType;
    }

    public void setSendType(String sendType) {
        this.sendType = sendType;
    }

    public String getTemplateType() {
        return templateType;
    }

    public void setTemplateType(String templateType) {
        this.templateType = templateType;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getEnabled() {
        return enabled;
    }

    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
