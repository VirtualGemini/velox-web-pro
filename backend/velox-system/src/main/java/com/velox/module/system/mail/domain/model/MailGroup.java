package com.velox.module.system.mail.domain.model;

import com.velox.domain.model.BaseEntity;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 发件分组
 * <p>
 * 管理员自定义命名分组，作为发件池的过滤条件之一；一个邮箱只属于一个分组。
 */
@TableName(value = "sys_mail_group")
public class MailGroup extends BaseEntity {

    /** 分组名称 */
    private String name;

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
