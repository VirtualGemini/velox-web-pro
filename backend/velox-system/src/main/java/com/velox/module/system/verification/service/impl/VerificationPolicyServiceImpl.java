package com.velox.module.system.verification.service.impl;

import com.velox.common.exception.ApiException;
import com.velox.common.exception.BusinessErrorCode;
import com.velox.module.system.auth.properties.SystemAuthProperties;
import com.velox.module.system.verification.common.EffectivePolicy;
import com.velox.module.system.verification.common.VerificationScene;
import com.velox.module.system.verification.domain.model.VerificationPolicy;
import com.velox.module.system.verification.dto.VerificationPolicyUpdateCommand;
import com.velox.module.system.verification.persistence.VerificationPolicyMapper;
import com.velox.module.system.verification.service.VerificationPolicyService;
import com.velox.module.system.verification.vo.VerificationPolicyRespVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 验证策略服务：多行配置（一行一场景），固定主键种子，热路径 30s 缓存。
 * <p>DB 为真相源；缺省值取自 {@link SystemAuthProperties}（仅在种子行缺失时兜底）。
 */
@Service
public class VerificationPolicyServiceImpl implements VerificationPolicyService {

    private static final Logger log = LoggerFactory.getLogger(VerificationPolicyServiceImpl.class);

    private static final long CACHE_TTL_MILLIS = 30_000L;

    private static final EffectivePolicy NOOP_POLICY = new EffectivePolicy(false, 0, 0, false, false);

    private static final List<String> KNOWN_SCENES = List.of(
            VerificationScene.LOGIN,
            VerificationScene.VERIFY_CODE,
            VerificationScene.CAPTCHA,
            VerificationScene.SEND_CODE,
            VerificationScene.MFA
    );

    /** 固定主键（与 init SQL 种子一致，避免运行期依赖 ID 生成）。 */
    private static final Map<String, String> SCENE_IDS = Map.of(
            VerificationScene.LOGIN, "1900000000000019001",
            VerificationScene.VERIFY_CODE, "1900000000000019002",
            VerificationScene.CAPTCHA, "1900000000000019003",
            VerificationScene.SEND_CODE, "1900000000000019004",
            VerificationScene.MFA, "1900000000000019005"
    );

    private final VerificationPolicyMapper mapper;
    private final SystemAuthProperties authProperties;

    private final Map<String, EffectivePolicy> cache = new ConcurrentHashMap<>();
    private volatile long cacheExpiresAt = 0L;

    public VerificationPolicyServiceImpl(VerificationPolicyMapper mapper, SystemAuthProperties authProperties) {
        this.mapper = mapper;
        this.authProperties = authProperties;
    }

    @Override
    public List<VerificationPolicyRespVO> listScenes() {
        List<VerificationPolicyRespVO> list = new ArrayList<>();
        for (String scene : KNOWN_SCENES) {
            list.add(toRespVO(getOrInit(scene)));
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateScene(String sceneKey, VerificationPolicyUpdateCommand command) {
        if (!SCENE_IDS.containsKey(sceneKey)) {
            throw new ApiException(BusinessErrorCode.DATA_NOT_FOUND);
        }
        VerificationPolicy policy = getOrInit(sceneKey);
        policy.setEnabled(Boolean.TRUE.equals(command.getEnabled()) ? 1 : 0);
        policy.setMaxAttempts(command.getMaxAttempts());
        policy.setRecoverySeconds(command.getRecoverySeconds());
        policy.setLimitByAccount(Boolean.TRUE.equals(command.getLimitByAccount()) ? 1 : 0);
        policy.setLimitByIp(Boolean.TRUE.equals(command.getLimitByIp()) ? 1 : 0);
        mapper.updateById(policy);
        invalidateCache();
    }

    @Override
    public EffectivePolicy getEffectivePolicy(String sceneKey) {
        if (System.currentTimeMillis() >= cacheExpiresAt) {
            try {
                reloadCache();
            } catch (RuntimeException ex) {
                // fail-open：策略不可读（DB 异常/表缺失）时绝不阻断热路径（登录/验证），
                // 沿用上次缓存或 no-op；并退避 TTL 避免每次请求都打库。
                cacheExpiresAt = System.currentTimeMillis() + CACHE_TTL_MILLIS;
                log.warn("[VerificationPolicy] 策略加载失败，fail-open 使用缓存/默认 scene={}: {}",
                        sceneKey, ex.getMessage());
            }
        }
        EffectivePolicy policy = cache.get(sceneKey);
        return policy != null ? policy : NOOP_POLICY;
    }

    private synchronized void reloadCache() {
        if (System.currentTimeMillis() < cacheExpiresAt) {
            return;
        }
        Map<String, EffectivePolicy> fresh = new ConcurrentHashMap<>();
        for (String scene : KNOWN_SCENES) {
            fresh.put(scene, toEffective(getOrInit(scene)));
        }
        cache.clear();
        cache.putAll(fresh);
        cacheExpiresAt = System.currentTimeMillis() + CACHE_TTL_MILLIS;
    }

    private void invalidateCache() {
        cacheExpiresAt = 0L;
    }

    private VerificationPolicy getOrInit(String scene) {
        VerificationPolicy existing = mapper.selectById(SCENE_IDS.get(scene));
        if (existing != null) {
            return existing;
        }
        VerificationPolicy created = buildDefault(scene);
        mapper.insert(created);
        return created;
    }

    private VerificationPolicy buildDefault(String scene) {
        SystemAuthProperties.Login login = authProperties.getLogin();
        SystemAuthProperties.Verification ver = authProperties.getVerification();
        VerificationPolicy p = new VerificationPolicy();
        p.setId(SCENE_IDS.get(scene));
        p.setSceneKey(scene);
        p.setEnabled(1);
        p.setDeleted(0);
        switch (scene) {
            case VerificationScene.LOGIN -> {
                p.setMaxAttempts(login.getMaxFailCount());
                p.setRecoverySeconds(login.getLockMinutes() * 60);
                p.setLimitByAccount(1);
                p.setLimitByIp(0);
            }
            case VerificationScene.VERIFY_CODE -> {
                p.setMaxAttempts(ver.getMaxVerifyAttempts());
                p.setRecoverySeconds(ver.getResetCodeTtlSeconds());
                p.setLimitByAccount(1);
                p.setLimitByIp(0);
            }
            case VerificationScene.CAPTCHA -> {
                p.setMaxAttempts(10);
                p.setRecoverySeconds(60);
                p.setLimitByAccount(0);
                p.setLimitByIp(1);
            }
            case VerificationScene.SEND_CODE -> {
                p.setMaxAttempts(1);
                p.setRecoverySeconds(ver.getResetCodeResendIntervalSeconds());
                p.setLimitByAccount(1);
                p.setLimitByIp(0);
            }
            case VerificationScene.MFA -> {
                p.setMaxAttempts(ver.getMaxVerifyAttempts());
                p.setRecoverySeconds(300);
                p.setLimitByAccount(1);
                p.setLimitByIp(0);
            }
            default -> {
                p.setMaxAttempts(5);
                p.setRecoverySeconds(300);
                p.setLimitByAccount(1);
                p.setLimitByIp(0);
            }
        }
        return p;
    }

    private EffectivePolicy toEffective(VerificationPolicy p) {
        return new EffectivePolicy(
                Integer.valueOf(1).equals(p.getEnabled()),
                p.getMaxAttempts() == null ? 0 : p.getMaxAttempts(),
                p.getRecoverySeconds() == null ? 0 : p.getRecoverySeconds(),
                Integer.valueOf(1).equals(p.getLimitByAccount()),
                Integer.valueOf(1).equals(p.getLimitByIp())
        );
    }

    private VerificationPolicyRespVO toRespVO(VerificationPolicy p) {
        VerificationPolicyRespVO vo = new VerificationPolicyRespVO();
        vo.setSceneKey(p.getSceneKey());
        vo.setEnabled(Integer.valueOf(1).equals(p.getEnabled()));
        vo.setMaxAttempts(p.getMaxAttempts());
        vo.setRecoverySeconds(p.getRecoverySeconds());
        vo.setLimitByAccount(Integer.valueOf(1).equals(p.getLimitByAccount()));
        vo.setLimitByIp(Integer.valueOf(1).equals(p.getLimitByIp()));
        return vo;
    }
}
