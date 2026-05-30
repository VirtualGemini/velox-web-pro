package com.velox.module.system.account.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.velox.common.exception.ApiException;
import com.velox.common.exception.BusinessErrorCode;
import com.velox.framework.security.api.session.SecuritySessionService;
import com.velox.module.system.account.admin.service.AdminAccountSecurityService;
import com.velox.module.system.account.dto.AdminLoginMethodsCommand;
import com.velox.module.system.account.dto.AdminMfaEmailCommand;
import com.velox.module.system.account.dto.AdminOauthChannelCommand;
import com.velox.module.system.account.dto.AdminPasswordResetCommand;
import com.velox.module.system.account.dto.AdminSecurityEmailCommand;
import com.velox.module.system.auth.properties.SystemAccountSecurityProperties;
import com.velox.module.system.auth.service.PasswordCipherService;
import com.velox.module.system.domain.model.Account;
import com.velox.module.system.domain.model.AccountSecurity;
import com.velox.module.system.id.generator.SystemEntityIdGenerator;
import com.velox.module.system.permission.service.PermissionService;
import com.velox.module.system.persistence.AccountMapper;
import com.velox.module.system.persistence.AccountSecurityMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class AdminAccountSecurityServiceImpl implements AdminAccountSecurityService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final AccountMapper userMapper;
    private final AccountSecurityMapper accountSecurityMapper;
    private final PasswordCipherService passwordCipherService;
    private final SystemAccountSecurityProperties accountSecurityProperties;
    private final SecuritySessionService securitySessionService;
    private final PermissionService permissionService;
    private final SystemEntityIdGenerator entityIdGenerator;

    public AdminAccountSecurityServiceImpl(AccountMapper userMapper,
                                           AccountSecurityMapper accountSecurityMapper,
                                           PasswordCipherService passwordCipherService,
                                           SystemAccountSecurityProperties accountSecurityProperties,
                                           SecuritySessionService securitySessionService,
                                           PermissionService permissionService,
                                           SystemEntityIdGenerator entityIdGenerator) {
        this.userMapper = userMapper;
        this.accountSecurityMapper = accountSecurityMapper;
        this.passwordCipherService = passwordCipherService;
        this.accountSecurityProperties = accountSecurityProperties;
        this.securitySessionService = securitySessionService;
        this.permissionService = permissionService;
        this.entityIdGenerator = entityIdGenerator;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean resetPassword(String accountId, AdminPasswordResetCommand command) {
        Account user = getActiveUser(accountId);
        ensureCanUpdateUser(user.getId());
        String operator = currentOperator();

        user.setPassword(passwordCipherService.encode(command.getNewPassword().trim()));
        user.setLoginFailCount(0);
        user.setLoginFailTime(null);
        user.setUpdateBy(operator);
        userMapper.updateById(user);

        AccountSecurity security = getOrInitSecurity(user);
        security.setLastPasswordChangeAt(LocalDateTime.now(ZoneOffset.UTC));
        security.setUpdateBy(operator);
        saveSecurity(security);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateSecurityEmail(String accountId, AdminSecurityEmailCommand command) {
        Account user = getActiveUser(accountId);
        ensureCanUpdateUser(user.getId());
        String operator = currentOperator();

        AccountSecurity security = getOrInitSecurity(user);
        String email = normalizeEmail(command.getEmail());
        if (email == null) {
            // 清除安全邮箱：同步关闭依赖邮箱的二次验证
            security.setEmail(null);
            security.setEmailVerifiedAt(null);
            security.setMfaEmailEnabled(0);
        } else {
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                throw new ApiException(BusinessErrorCode.EMAIL_REQUIRED);
            }
            AccountSecurity existing = accountSecurityMapper.selectOne(new LambdaQueryWrapper<AccountSecurity>()
                    .eq(AccountSecurity::getDeleted, 0)
                    .eq(AccountSecurity::getEmail, email)
                    .last("limit 1"));
            if (existing != null && !user.getId().equals(existing.getAccountId())) {
                throw new ApiException(BusinessErrorCode.EMAIL_ALREADY_IN_USE);
            }
            security.setEmail(email);
            security.setEmailVerifiedAt(LocalDateTime.now(ZoneOffset.UTC));
        }
        security.setUpdateBy(operator);
        saveSecurity(security);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateMfaEmail(String accountId, AdminMfaEmailCommand command) {
        if (command.getEnabled() == null) {
            throw new ApiException(BusinessErrorCode.OPERATION_FAILED);
        }
        Account user = getActiveUser(accountId);
        ensureCanUpdateUser(user.getId());
        AccountSecurity security = getOrInitSecurity(user);

        if (command.getEnabled()) {
            if (Integer.valueOf(1).equals(security.getMfaTotpEnabled())) {
                throw new ApiException(BusinessErrorCode.MFA_ALREADY_ENABLED);
            }
            String email = normalizeEmail(security.getEmail());
            if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
                throw new ApiException(BusinessErrorCode.EMAIL_NOT_BOUND_TO_USER);
            }
            security.setMfaEmailEnabled(1);
        } else {
            security.setMfaEmailEnabled(0);
        }
        security.setUpdateBy(currentOperator());
        saveSecurity(security);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean disableTotp(String accountId) {
        Account user = getActiveUser(accountId);
        ensureCanUpdateUser(user.getId());
        AccountSecurity security = getOrInitSecurity(user);
        security.setMfaTotpEnabled(0);
        security.setMfaTotpSecret(null);
        security.setMfaTotpRecoveryCodes(null);
        security.setUpdateBy(currentOperator());
        saveSecurity(security);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateLoginMethods(String accountId, AdminLoginMethodsCommand command) {
        Account user = getActiveUser(accountId);
        ensureCanUpdateUser(user.getId());

        List<String> globalEnabled = accountSecurityProperties.getLoginMethods().getEnabled();
        Set<String> enabled = normalizeMethods(command.getEnabledMethods());
        Set<String> disabled = normalizeMethods(command.getDisabledMethods());

        if (enabled.isEmpty()) {
            throw new ApiException(BusinessErrorCode.LOGIN_METHOD_EMPTY);
        }
        for (String method : enabled) {
            if (!globalEnabled.contains(method)) {
                throw new ApiException(BusinessErrorCode.LOGIN_METHOD_NOT_ALLOWED);
            }
            if (disabled.contains(method)) {
                // 同一方式不能既启用又禁用
                throw new ApiException(BusinessErrorCode.LOGIN_METHOD_NOT_ALLOWED);
            }
        }

        AccountSecurity security = getOrInitSecurity(user);
        security.setLoginMethods(String.join(",", enabled));
        security.setDisabledLoginMethods(disabled.isEmpty() ? null : String.join(",", disabled));
        security.setUpdateBy(currentOperator());
        saveSecurity(security);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean toggleOauthChannel(String accountId, String channel, AdminOauthChannelCommand command) {
        String key = channel == null ? "" : channel.trim().toLowerCase();
        if (!StringUtils.hasText(key)) {
            throw new ApiException(BusinessErrorCode.OPERATION_FAILED);
        }
        boolean disabled = Boolean.TRUE.equals(command.getDisabled());
        Account user = getActiveUser(accountId);
        ensureCanUpdateUser(user.getId());

        AccountSecurity security = getOrInitSecurity(user);
        Set<String> channels = new LinkedHashSet<>(parseCsv(security.getDisabledOauthChannels()));
        if (disabled) {
            channels.add(key);
        } else {
            channels.remove(key);
        }
        security.setDisabledOauthChannels(channels.isEmpty() ? null : String.join(",", channels));
        security.setUpdateBy(currentOperator());
        saveSecurity(security);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean unbindOauthChannel(String accountId, String channel) {
        Account user = getActiveUser(accountId);
        ensureCanUpdateUser(user.getId());
        // 第三方登录绑定能力尚未实现，没有真实绑定可解除；此处为占位返回成功，待 OAuth 绑定落地后补全。
        return true;
    }

    private void ensureCanUpdateUser(String userId) {
        String currentUserId = securitySessionService.currentLoginIdOrNull();
        if (!StringUtils.hasText(currentUserId)) {
            throw new ApiException(BusinessErrorCode.USER_UPDATE_FORBIDDEN);
        }
        if (currentUserId.equals(userId)) {
            return;
        }
        int operatorLevel = permissionService.getAccountHighestRoleLevel(currentUserId);
        int targetLevel = permissionService.getAccountHighestRoleLevel(userId);
        if (operatorLevel <= targetLevel) {
            throw new ApiException(BusinessErrorCode.USER_UPDATE_FORBIDDEN);
        }
    }

    private Account getActiveUser(String userId) {
        Account user = userMapper.selectOne(new LambdaQueryWrapper<Account>()
                .eq(Account::getId, userId)
                .eq(Account::getDeleted, 0)
                .last("limit 1"));
        if (user == null) {
            throw new ApiException(BusinessErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    private AccountSecurity getOrInitSecurity(Account user) {
        AccountSecurity security = accountSecurityMapper.selectOne(new LambdaQueryWrapper<AccountSecurity>()
                .eq(AccountSecurity::getAccountId, user.getId())
                .eq(AccountSecurity::getDeleted, 0)
                .last("limit 1"));
        if (security != null) {
            return security;
        }
        AccountSecurity created = new AccountSecurity();
        created.setId(entityIdGenerator.nextId(AccountSecurity.class));
        created.setAccountId(user.getId());
        created.setLoginMethods(String.join(",", accountSecurityProperties.getLoginMethods().getDefaults()));
        created.setMfaEmailEnabled(0);
        created.setMfaTotpEnabled(0);
        created.setDeleted(0);
        return created;
    }

    private void saveSecurity(AccountSecurity security) {
        if (security.getId() != null && accountSecurityMapper.selectById(security.getId()) != null) {
            accountSecurityMapper.updateById(security);
        } else {
            accountSecurityMapper.insert(security);
        }
        // 安全设置变更同样属于对账号的编辑：刷新账号更新时间，使列表“编辑时间”反映最新操作。
        touchAccountUpdateTime(security.getAccountId());
    }

    /**
     * 刷新账号更新时间/更新人。安全相关改动只落在 sys_account_security，
     * 这里同步触达 sys_account，避免“编辑了账号但编辑时间不变”。
     */
    private void touchAccountUpdateTime(String accountId) {
        if (!StringUtils.hasText(accountId)) {
            return;
        }
        userMapper.update(null, new LambdaUpdateWrapper<Account>()
                .eq(Account::getId, accountId)
                .eq(Account::getDeleted, 0)
                .set(Account::getUpdateBy, currentOperator())
                .set(Account::getUpdateTime, LocalDateTime.now(ZoneOffset.UTC)));
    }

    private Set<String> normalizeMethods(List<String> methods) {
        Set<String> result = new LinkedHashSet<>();
        if (methods == null) {
            return result;
        }
        for (String method : methods) {
            if (StringUtils.hasText(method)) {
                result.add(method.trim().toLowerCase());
            }
        }
        return result;
    }

    private List<String> parseCsv(String value) {
        if (!StringUtils.hasText(value)) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(String::toLowerCase)
                .distinct()
                .toList();
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim().toLowerCase();
    }

    private String currentOperator() {
        String loginId = securitySessionService.currentLoginIdOrNull();
        return StringUtils.hasText(loginId) ? loginId : "system";
    }
}
