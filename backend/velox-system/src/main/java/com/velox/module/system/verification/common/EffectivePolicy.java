package com.velox.module.system.verification.common;

/**
 * 某场景的生效策略快照（供限制器热路径只读使用）。
 *
 * <p>recoverySeconds 同时充当：失败计数场景的锁定时长 + 请求计数场景的窗口长度。
 */
public record EffectivePolicy(
        boolean enabled,
        int maxAttempts,
        int recoverySeconds,
        boolean limitByAccount,
        boolean limitByIp
) {
}
