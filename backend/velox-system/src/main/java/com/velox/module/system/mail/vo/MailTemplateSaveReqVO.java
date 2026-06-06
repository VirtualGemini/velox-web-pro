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

    /** 当前编辑语言，兼容前端编辑器按语言保存。 */
    @Schema(description = "openapi.system.mail_template.field.language")
    private String language;

    @Schema(description = "openapi.system.mail_template.field.subject")
    private String subject;

    @Schema(description = "openapi.system.mail_template.field.content")
    private String content;

    @Schema(description = "openapi.system.mail_template.field.subject_zh")
    private String subjectZh;

    @Schema(description = "openapi.system.mail_template.field.content_zh")
    private String contentZh;

    @Schema(description = "openapi.system.mail_template.field.subject_en")
    private String subjectEn;

    @Schema(description = "openapi.system.mail_template.field.content_en")
    private String contentEn;

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

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
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

    public String getSubjectZh() {
        return subjectZh;
    }

    public void setSubjectZh(String subjectZh) {
        this.subjectZh = subjectZh;
    }

    public String getContentZh() {
        return contentZh;
    }

    public void setContentZh(String contentZh) {
        this.contentZh = contentZh;
    }

    public String getSubjectEn() {
        return subjectEn;
    }

    public void setSubjectEn(String subjectEn) {
        this.subjectEn = subjectEn;
    }

    public String getContentEn() {
        return contentEn;
    }

    public void setContentEn(String contentEn) {
        this.contentEn = contentEn;
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
