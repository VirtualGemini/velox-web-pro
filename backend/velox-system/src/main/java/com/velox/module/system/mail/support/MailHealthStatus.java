package com.velox.module.system.mail.support;

/**
 * 发件邮箱健康状态
 * <p>
 * NORMAL 正常可用；UNAVAILABLE 异常（连续失败达到阈值，过重试间隔后可再次试用）；
 * DEAD 死亡（累计异常达到上限，不可自动恢复，仅可由用户手动恢复）。
 */
public final class MailHealthStatus {

    /** 正常 */
    public static final int NORMAL = 0;

    /** 异常 */
    public static final int UNAVAILABLE = 1;

    /** 死亡 */
    public static final int DEAD = 2;

    private MailHealthStatus() {
    }
}
