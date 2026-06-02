package com.velox.module.system.mail.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "openapi.system.mail_channel.resp.schema")
public class MailChannelRespVO {

    @Schema(description = "openapi.common.id")
    private String id;

    @Schema(description = "openapi.system.mail_channel.field.name")
    private String name;

    @Schema(description = "openapi.system.mail_channel.field.protocol")
    private String protocol;

    @Schema(description = "openapi.system.mail_channel.field.active")
    private Integer active;

    @Schema(description = "openapi.system.mail_channel.field.sort")
    private Integer sort;

    @Schema(description = "openapi.system.mail_channel.field.account_count")
    private Long accountCount;

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

    public Long getAccountCount() {
        return accountCount;
    }

    public void setAccountCount(Long accountCount) {
        this.accountCount = accountCount;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
