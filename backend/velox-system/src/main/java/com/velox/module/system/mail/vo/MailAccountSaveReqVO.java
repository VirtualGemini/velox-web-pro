package com.velox.module.system.mail.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

@Schema(description = "openapi.system.mail_account.save_req.schema")
public class MailAccountSaveReqVO {

    @Schema(description = "openapi.common.id")
    private String id;

    @Schema(description = "openapi.system.mail_account.field.name", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{validation.system.mail_account.name.not_empty}")
    private String name;

    @Schema(description = "openapi.system.mail_account.field.group_id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{validation.system.mail_account.group_id.not_empty}")
    private String groupId;

    @Schema(description = "openapi.system.mail_account.field.channel_id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{validation.system.mail_account.channel_id.not_empty}")
    private String channelId;

    @Schema(description = "openapi.system.mail_account.field.username", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{validation.system.mail_account.username.not_empty}")
    private String username;

    @Schema(description = "openapi.system.mail_account.field.password")
    private String password;

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
    @Min(value = 1, message = "{validation.system.mail_account.weight.min}")
    private Integer weight;

    @Schema(description = "openapi.system.mail_account.field.fail_threshold")
    @Min(value = 1, message = "{validation.system.mail_account.fail_threshold.min}")
    private Integer failThreshold;

    @Schema(description = "openapi.system.mail_account.field.retry_interval")
    @Min(value = 0, message = "{validation.system.mail_account.retry_interval.min}")
    private Integer retryInterval;

    @Schema(description = "openapi.system.mail_account.field.max_unavailable")
    @Min(value = 1, message = "{validation.system.mail_account.max_unavailable.min}")
    private Integer maxUnavailable;

    @Schema(description = "openapi.common.enabled")
    private Integer enabled;

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
}
