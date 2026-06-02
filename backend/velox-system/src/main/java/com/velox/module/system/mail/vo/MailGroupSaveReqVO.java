package com.velox.module.system.mail.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

@Schema(description = "openapi.system.mail_group.save_req.schema")
public class MailGroupSaveReqVO {

    @Schema(description = "openapi.common.id")
    private String id;

    @Schema(description = "openapi.system.mail_group.field.name", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{validation.system.mail_group.name.not_empty}")
    private String name;

    @Schema(description = "openapi.system.mail_group.field.active")
    private Integer active;

    @Schema(description = "openapi.system.mail_group.field.sort")
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
