package com.velox.module.system.accesscontrol.service.impl;

import com.velox.common.exception.ApiException;
import com.velox.common.exception.BusinessErrorCode;
import com.velox.module.system.accesscontrol.domain.model.AccessControlConfig;
import com.velox.module.system.accesscontrol.persistence.AccessControlMapper;
import com.velox.module.system.accesscontrol.service.AccessControlService;
import com.velox.module.system.accesscontrol.vo.AccessControlRespVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 访问控制全局配置服务实现。
 * <p>
 * 单例语义：固定主键 {@link #SINGLETON_ID}，{@link #getOrInitConfig()} 懒初始化。
 * 登录方式/渠道统一以 CSV 持久化，沿用 sys_account_security.login_methods 约定。
 */
@Service
public class AccessControlServiceImpl implements AccessControlService {

    /** 单例固定主键（与 init/migration SQL 中的默认行保持一致）。 */
    private static final String SINGLETON_ID = "1900000000000002010";

    /** 普通登录方式合法取值。 */
    private static final Set<String> SUPPORTED_LOGIN_METHODS = Set.of("password", "email_code");

    private static final String DEFAULT_LOGIN_METHODS = "password,email_code";
    private static final String DEFAULT_THIRD_PARTY_CHANNELS = "github,linuxdo";

    private final AccessControlMapper accessControlMapper;

    public AccessControlServiceImpl(AccessControlMapper accessControlMapper) {
        this.accessControlMapper = accessControlMapper;
    }

    @Override
    public AccessControlRespVO getConfig() {
        return toRespVO(getOrInitConfig());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGeneralRegister(boolean enabled) {
        AccessControlConfig config = getOrInitConfig();
        config.setGeneralRegisterEnabled(enabled ? 1 : 0);
        accessControlMapper.updateById(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateForgotPassword(boolean enabled) {
        AccessControlConfig config = getOrInitConfig();
        config.setForgotPasswordEnabled(enabled ? 1 : 0);
        accessControlMapper.updateById(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLoginMethods(List<String> methods) {
        List<String> normalized = normalize(methods).stream()
                .filter(SUPPORTED_LOGIN_METHODS::contains)
                .collect(Collectors.toList());
        if (normalized.isEmpty()) {
            throw new ApiException(BusinessErrorCode.LOGIN_METHOD_EMPTY);
        }
        AccessControlConfig config = getOrInitConfig();
        config.setLoginMethods(String.join(",", normalized));
        accessControlMapper.updateById(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateThirdPartyLogin(List<String> channels) {
        AccessControlConfig config = getOrInitConfig();
        config.setThirdPartyLoginChannels(String.join(",", normalize(channels)));
        accessControlMapper.updateById(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateThirdPartyRegister(List<String> channels) {
        AccessControlConfig config = getOrInitConfig();
        config.setThirdPartyRegisterChannels(String.join(",", normalize(channels)));
        accessControlMapper.updateById(config);
    }

    @Override
    public boolean isGeneralRegisterEnabled() {
        return Integer.valueOf(1).equals(getOrInitConfig().getGeneralRegisterEnabled());
    }

    @Override
    public boolean isForgotPasswordEnabled() {
        return Integer.valueOf(1).equals(getOrInitConfig().getForgotPasswordEnabled());
    }

    @Override
    public List<String> getEnabledLoginMethods() {
        return parseCsv(getOrInitConfig().getLoginMethods());
    }

    /**
     * 懒初始化单例：不存在时插入默认行（全部启用）。
     * 参考 AccountInfoServiceImpl.getOrInitSecurity 写法。
     */
    private AccessControlConfig getOrInitConfig() {
        AccessControlConfig config = accessControlMapper.selectById(SINGLETON_ID);
        if (config != null) {
            return config;
        }
        AccessControlConfig created = new AccessControlConfig();
        created.setId(SINGLETON_ID);
        created.setGeneralRegisterEnabled(1);
        created.setForgotPasswordEnabled(1);
        created.setLoginMethods(DEFAULT_LOGIN_METHODS);
        created.setThirdPartyLoginChannels(DEFAULT_THIRD_PARTY_CHANNELS);
        created.setThirdPartyRegisterChannels(DEFAULT_THIRD_PARTY_CHANNELS);
        created.setDeleted(0);
        accessControlMapper.insert(created);
        return created;
    }

    private AccessControlRespVO toRespVO(AccessControlConfig config) {
        AccessControlRespVO vo = new AccessControlRespVO();
        vo.setGeneralRegisterEnabled(Integer.valueOf(1).equals(config.getGeneralRegisterEnabled()));
        vo.setForgotPasswordEnabled(Integer.valueOf(1).equals(config.getForgotPasswordEnabled()));
        vo.setLoginMethods(parseCsv(config.getLoginMethods()));
        vo.setThirdPartyLoginChannels(parseCsv(config.getThirdPartyLoginChannels()));
        vo.setThirdPartyRegisterChannels(parseCsv(config.getThirdPartyRegisterChannels()));
        return vo;
    }

    private List<String> normalize(List<String> values) {
        if (values == null) {
            return new ArrayList<>();
        }
        return values.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(Collectors.toCollection(LinkedHashSet::new))
                .stream()
                .collect(Collectors.toList());
    }

    private List<String> parseCsv(String csv) {
        if (!StringUtils.hasText(csv)) {
            return new ArrayList<>();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
    }
}
