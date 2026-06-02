package com.velox.module.system.mail.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "openapi.system.mail_account.resp.schema")
public class MailAccountRespVO {

    @Schema(description = "openapi.common.id")
    private String id;

    @Schema(description = "openapi.system.mail_account.field.name")
    private String name;

    @Schema(description = "openapi.system.mail_account.field.group_id")
    private String groupId;

    @Schema(description = "openapi.system.mail_account.field.group_name")
    private String groupName;

    @Schema(description = "openapi.system.mail_account.field.channel_id")
    private String channelId;

    @Schema(description = "openapi.system.mail_account.field.channel_name")
    private String channelName;

    @Schema(description = "openapi.system.mail_channel.field.protocol")
    private String protocol;

    @Schema(description = "openapi.system.mail_account.field.username")
    private String username;

    @Schema(description = "openapi.system.mail_account.field.password_set")
    private Boolean passwordSet;

    @Schema(description = "openapi.system.mail_account.field.from_address")
    private String fromAddress;

    @Schema(description = "openapi.system.mail_account.field.from_name")
    private String fromName;

    @Schema(description = "openapi.system.mail_account.field.host")
    private String host;

    @Schema(description = "openapi.system.mail_account.field.port")
    private Integer port;

    @Schema(description = "openapi.system.mail_account.field.ssl")
    private Integer sslEnabled;

    @Schema(description = "openapi.system.mail_account.field.starttls")
    private Integer starttls;

    @Schema(description = "openapi.system.mail_account.field.weight")
    private Integer weight;

    @Schema(description = "openapi.system.mail_account.field.fail_threshold")
    private Integer failThreshold;

    @Schema(description = "openapi.system.mail_account.field.retry_interval")
    private Integer retryInterval;

    @Schema(description = "openapi.system.mail_account.field.max_unavailable")
    private Integer maxUnavailable;

    @Schema(description = "openapi.system.mail_account.field.health_status")
    private Integer healthStatus;

    @Schema(description = "openapi.system.mail_account.field.usage_count")
    private Long usageCount;

    @Schema(description = "openapi.system.mail_account.field.fail_count")
    private Integer failCount;

    @Schema(description = "openapi.system.mail_account.field.unavailable_count")
    private Integer unavailableCount;

    @Schema(description = "openapi.system.mail_account.field.next_retry_time")
    private String nextRetryTime;

    @Schema(description = "openapi.system.mail_account.field.last_used_time")
    private String lastUsedTime;

    @Schema(description = "openapi.common.enabled")
    private Integer enabled;

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

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Boolean getPasswordSet() {
        return passwordSet;
    }

    public void setPasswordSet(Boolean passwordSet) {
        this.passwordSet = passwordSet;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Integer getSslEnabled() {
        return sslEnabled;
    }

    public void setSslEnabled(Integer sslEnabled) {
        this.sslEnabled = sslEnabled;
    }

    public Integer getStarttls() {
        return starttls;
    }

    public void setStarttls(Integer starttls) {
        this.starttls = starttls;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public Integer getFailThreshold() {
        return failThreshold;
    }

    public void setFailThreshold(Integer failThreshold) {
        this.failThreshold = failThreshold;
    }

    public Integer getRetryInterval() {
        return retryInterval;
    }

    public void setRetryInterval(Integer retryInterval) {
        this.retryInterval = retryInterval;
    }

    public Integer getMaxUnavailable() {
        return maxUnavailable;
    }

    public void setMaxUnavailable(Integer maxUnavailable) {
        this.maxUnavailable = maxUnavailable;
    }

    public Integer getHealthStatus() {
        return healthStatus;
    }

    public void setHealthStatus(Integer healthStatus) {
        this.healthStatus = healthStatus;
    }

    public Long getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(Long usageCount) {
        this.usageCount = usageCount;
    }

    public Integer getFailCount() {
        return failCount;
    }

    public void setFailCount(Integer failCount) {
        this.failCount = failCount;
    }

    public Integer getUnavailableCount() {
        return unavailableCount;
    }

    public void setUnavailableCount(Integer unavailableCount) {
        this.unavailableCount = unavailableCount;
    }

    public String getNextRetryTime() {
        return nextRetryTime;
    }

    public void setNextRetryTime(String nextRetryTime) {
        this.nextRetryTime = nextRetryTime;
    }

    public String getLastUsedTime() {
        return lastUsedTime;
    }

    public void setLastUsedTime(String lastUsedTime) {
        this.lastUsedTime = lastUsedTime;
    }

    public Integer getEnabled() {
        return enabled;
    }

    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
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
