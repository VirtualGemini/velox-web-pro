package com.velox.module.system.mail.domain.model;

import com.velox.domain.model.BaseEntity;

import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 发件邮箱
 * <p>
 * 发件池中的一个发件账号。归属一个分组、使用一个渠道（协议）。
 * 留空的 host/port/ssl/starttls 会在发送时按域名自动识别。
 * 运行时健康状态与计数随发送结果回写：连续失败达到阈值标记“异常”，
 * 过重试间隔后试用，累计被标记次数达到上限则标记“死亡”，死亡不可自动恢复。
 */
@TableName(value = "sys_mail_account")
public class MailAccount extends BaseEntity {

    /** 名称/别名 */
    private String name;

    /** 所属分组ID */
    private String groupId;

    /** 所属渠道ID */
    private String channelId;

    /** SMTP 用户名 */
    private String username;

    /** SMTP 授权码/密码（明文存储，TODO 后续接入加解密工具） */
    private String password;

    /** 发件地址（留空时使用 username） */
    private String fromAddress;

    /** 发件人名称 */
    private String fromName;

    /** SMTP 主机（留空按域名自动识别） */
    private String host;

    /** SMTP 端口（留空按域名自动识别） */
    private Integer port;

    /** 是否启用 SSL(0-否 1-是 null-自动识别)；列名 ssl_enabled，规避 MySQL 保留字 ssl */
    private Integer sslEnabled;

    /** 是否启用 STARTTLS(0-否 1-是 null-自动识别) */
    private Integer starttls;

    /** 权重（数字越低权重越高，被选中概率越大） */
    private Integer weight;

    /** 触发“异常”的连续失败次数阈值 */
    private Integer failThreshold;

    /** 重试间隔（秒），异常后经过该时长可再次试用 */
    private Integer retryInterval;

    /** 累计被标记“异常”达到该次数后标记为“死亡” */
    private Integer maxUnavailable;

    /** 健康状态(0-正常 1-异常 2-死亡) */
    private Integer healthStatus;

    /** 已使用次数 */
    private Long usageCount;

    /** 当前连续失败次数 */
    private Integer failCount;

    /** 累计被标记“异常”次数 */
    private Integer unavailableCount;

    /** 下次可重试时间 */
    private LocalDateTime nextRetryTime;

    /** 上次使用时间 */
    private LocalDateTime lastUsedTime;

    /** 备注 */
    private String remark;

    /** 启用状态(0-禁用 1-启用) */
    private Integer enabled;

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

    public LocalDateTime getNextRetryTime() {
        return nextRetryTime;
    }

    public void setNextRetryTime(LocalDateTime nextRetryTime) {
        this.nextRetryTime = nextRetryTime;
    }

    public LocalDateTime getLastUsedTime() {
        return lastUsedTime;
    }

    public void setLastUsedTime(LocalDateTime lastUsedTime) {
        this.lastUsedTime = lastUsedTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getEnabled() {
        return enabled;
    }

    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
    }
}
