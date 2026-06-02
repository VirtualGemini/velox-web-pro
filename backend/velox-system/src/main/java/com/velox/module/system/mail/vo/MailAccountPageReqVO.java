package com.velox.module.system.mail.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "openapi.system.mail_account.page_req.schema")
public class MailAccountPageReqVO {

    @Schema(description = "openapi.system.mail_account.field.name")
    private String name;

    @Schema(description = "openapi.system.mail_account.field.group_id")
    private String groupId;

    @Schema(description = "openapi.system.mail_account.field.channel_id")
    private String channelId;

    @Schema(description = "openapi.system.mail_account.field.health_status")
    private Integer healthStatus;

    @Schema(description = "openapi.common.enabled")
    private Integer enabled;

    @Schema(description = "openapi.common.audit.create_time_start", example = "2026-05-22 10:00:00")
    private String createTimeStart;

    @Schema(description = "openapi.common.audit.create_time_end", example = "2026-05-22 18:00:00")
    private String createTimeEnd;

    @Schema(description = "openapi.common.audit.update_time_start", example = "2026-05-22 10:00:00")
    private String updateTimeStart;

    @Schema(description = "openapi.common.audit.update_time_end", example = "2026-05-22 18:00:00")
    private String updateTimeEnd;

    @Schema(description = "openapi.common.page.page", example = "1")
    private Integer page = 1;

    @Schema(description = "openapi.common.page.size", example = "10")
    private Integer size = 10;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public Integer getHealthStatus() {
        return healthStatus;
    }

    public void setHealthStatus(Integer healthStatus) {
        this.healthStatus = healthStatus;
    }

    public Integer getEnabled() {
        return enabled;
    }

    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
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

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}
