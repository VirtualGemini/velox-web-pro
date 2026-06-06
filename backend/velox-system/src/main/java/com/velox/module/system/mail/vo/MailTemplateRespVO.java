package com.velox.module.system.mail.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "openapi.system.mail_template.resp.schema")
public class MailTemplateRespVO {

    @Schema(description = "openapi.common.id")
    private String id;

    @Schema(description = "openapi.system.mail_template.field.name")
    private String name;

    @Schema(description = "openapi.system.mail_template.field.send_type")
    private String sendType;

    /** 兼容前端旧字段：等同 sendType。 */
    @Schema(description = "openapi.system.mail_template.field.type")
    private String type;

    @Schema(description = "openapi.system.mail_template.field.template_type")
    private String templateType;

    @Schema(description = "openapi.system.mail_template.field.subject")
    private String subject;

    /** 列表查询不返回，仅详情返回完整 HTML */
    @Schema(description = "openapi.system.mail_template.field.content")
    private String content;

    @Schema(description = "openapi.common.enabled")
    private Integer enabled;

    @Schema(description = "openapi.system.mail_template.field.sort")
    private Integer sort;

    @Schema(description = "openapi.common.remark")
    private String remark;

    @Schema(description = "openapi.common.audit.create_time")
    private String createTime;

    @Schema(description = "openapi.common.audit.update_time")
    private String updateTime;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }
}
