package com.velox.module.system.mail.domain.model;

import com.velox.domain.model.BaseEntity;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 发件渠道（发件协议）
 * <p>
 * 渠道等价于发件协议（当前支持 SMTP/SMTPS），属代码级扩展点；作为发件池的过滤条件之一。
 * 一个邮箱只用一个渠道。渠道不开放删除，仅可切换生效状态。
 */
@TableName(value = "sys_mail_channel")
public class MailChannel extends BaseEntity {

    /** 渠道名称 */
    private String name;

    /** 协议(SMTP/SMTPS) */
    private String protocol;

    /** 是否生效(0-否 1-是)，作为发件池过滤条件 */
    private Integer active;

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

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public Integer getActive() {
        return active;
    }

    public void setActive(Integer active) {
        this.active = active;
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
