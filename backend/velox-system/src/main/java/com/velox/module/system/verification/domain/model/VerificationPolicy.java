package com.velox.module.system.verification.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.velox.domain.model.BaseEntity;

/**
 * 验证策略（多行，一行一场景）。
 * <p>
 * 统一管理「限时且需验证的表单」的反爆破策略：最大次数、恢复时间、限制维度（账号/IP）。
 */
@TableName("sys_verification_policy")
public class VerificationPolicy extends BaseEntity {

    /** 场景键（唯一）：login / verify_code / captcha / send_code / mfa。 */
    private String sceneKey;

    /** 是否启用 (0-关闭 1-开启)。 */
    private Integer enabled;

    /** 最大可重试次数。 */
    private Integer maxAttempts;

    /** 恢复时间（秒）：失败计数场景=锁定时长；请求计数场景=窗口长度。 */
    private Integer recoverySeconds;

    /** 是否按账号/邮箱维度限制 (0/1)。 */
    private Integer limitByAccount;

    /** 是否按 IP 维度限制 (0/1)。 */
    private Integer limitByIp;

    public String getSceneKey() {
        return sceneKey;
    }

    public void setSceneKey(String sceneKey) {
        this.sceneKey = sceneKey;
    }

    public Integer getEnabled() {
        return enabled;
    }

    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
    }

    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(Integer maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public Integer getRecoverySeconds() {
        return recoverySeconds;
    }

    public void setRecoverySeconds(Integer recoverySeconds) {
        this.recoverySeconds = recoverySeconds;
    }

    public Integer getLimitByAccount() {
        return limitByAccount;
    }

    public void setLimitByAccount(Integer limitByAccount) {
        this.limitByAccount = limitByAccount;
    }

    public Integer getLimitByIp() {
        return limitByIp;
    }

    public void setLimitByIp(Integer limitByIp) {
        this.limitByIp = limitByIp;
    }
}
