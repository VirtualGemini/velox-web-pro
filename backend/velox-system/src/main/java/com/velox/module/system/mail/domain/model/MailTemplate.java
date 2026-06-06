package com.velox.module.system.mail.domain.model;

import com.velox.domain.model.BaseEntity;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 邮件模板
 * <p>
 * 每个发件类型只需要一行模板，行内保存中英文两套主题/正文；发件时按收件人语言取对应内容，
 * 若该语言为空则回退到另一种语言，再回退到内置默认模板。
 */
@TableName(value = "sys_mail_template")
public class MailTemplate extends BaseEntity {

    /** 模板名称（全局唯一） */
    private String name;

    /** 发件类型 code，见 {@link com.velox.module.system.mail.template.MailTemplateType} */
    private String sendType;

    /** 模板类型：SYSTEM / CUSTOM */
    private String templateType;

    /** 中文邮件主题，可含 {@code {{变量}}} */
    private String subjectZh;

    /** 中文邮件正文 HTML，含 {@code {{变量}}} 占位 */
    private String contentZh;

    /** 英文邮件主题，可含 {@code {{变量}}} */
    private String subjectEn;

    /** 英文邮件正文 HTML，含 {@code {{变量}}} 占位 */
    private String contentEn;

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
