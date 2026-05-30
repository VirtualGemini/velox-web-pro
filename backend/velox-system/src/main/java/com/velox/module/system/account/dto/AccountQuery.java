package com.velox.module.system.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "openapi.system.user.query.schema")
public class AccountQuery {

    @Schema(description = "openapi.common.page.current", example = "1")
    private Long current = 1L;

    @Schema(description = "openapi.common.page.size", example = "20")
    private Long size = 20L;

    @Schema(description = "openapi.system.user.query.username")
    private String username;

    @Schema(description = "openapi.system.user.query.email")
    private String email;

    @Schema(description = "openapi.system.user.query.remark")
    private String remark;

    @Schema(description = "openapi.system.user.query.status")
    private String status;

    @Schema(description = "openapi.system.user.query.active_status")
    private String activeStatus;

    @Schema(description = "openapi.common.audit.create_time_start", example = "2026-05-22 10:00:00")
    private String createTimeStart;

    @Schema(description = "openapi.common.audit.create_time_end", example = "2026-05-22 18:00:00")
    private String createTimeEnd;

    @Schema(description = "openapi.common.audit.update_time_start", example = "2026-05-22 10:00:00")
    private String updateTimeStart;

    @Schema(description = "openapi.common.audit.update_time_end", example = "2026-05-22 18:00:00")
    private String updateTimeEnd;

    public Long getCurrent() {
        return current;
    }

    public void setCurrent(Long current) {
        this.current = current;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getActiveStatus() {
        return activeStatus;
    }

    public void setActiveStatus(String activeStatus) {
        this.activeStatus = activeStatus;
    }

    public String getCreateTimeStart() {
        return createTimeStart;
    }

    public void setCreateTimeStart(String createTimeStart) {
        this.createTimeStart = createTimeStart;
    }

    public String getCreateTimeEnd() {
        return createTimeEnd;
    }

    public void setCreateTimeEnd(String createTimeEnd) {
        this.createTimeEnd = createTimeEnd;
    }

    public String getUpdateTimeStart() {
        return updateTimeStart;
    }

    public void setUpdateTimeStart(String updateTimeStart) {
        this.updateTimeStart = updateTimeStart;
    }

    public String getUpdateTimeEnd() {
        return updateTimeEnd;
    }

    public void setUpdateTimeEnd(String updateTimeEnd) {
        this.updateTimeEnd = updateTimeEnd;
    }
}
