package com.velox.module.system.mail.domain.model;

import com.velox.domain.model.BaseEntity;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 邮件模板
 */
@TableName(value = "sys_mail_template")
public class MailTemplate extends BaseEntity {

    /** 模板名称（全局唯一） */
    private String name;

    /** 发件类型 code，见 {@link com.velox.module.system.mail.template.MailTemplateType} */
    private String sendType;

    /** 模板类型：SYSTEM / CUSTOM */
    private String templateType;

    /** 邮件主题，可含 {@code {{变量}}} */
    private String subject;

    /** 邮件正文 HTML，含 {@code {{变量}}} 占位 */
    private String content;

    /** 是否启用(0-否 1-是)；同一发件类型至多一条启用 */
    private Integer enabled;

    /** 排序 */
    private Integer sort;

    /** 备注 */
    private String remark;

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
