package com.velox.module.system.verification.service;

import com.velox.module.system.verification.common.EffectivePolicy;
import com.velox.module.system.verification.dto.VerificationPolicyUpdateCommand;
import com.velox.module.system.verification.vo.VerificationPolicyRespVO;

import java.util.List;

public interface VerificationPolicyService {

    /** 列出所有场景策略（管理端）。 */
    List<VerificationPolicyRespVO> listScenes();

    /** 更新某场景策略（管理端）。 */
    void updateScene(String sceneKey, VerificationPolicyUpdateCommand command);

    /** 取某场景生效策略（热路径，带缓存）。未知场景返回 disabled 的 no-op 策略。 */
    EffectivePolicy getEffectivePolicy(String sceneKey);
}
