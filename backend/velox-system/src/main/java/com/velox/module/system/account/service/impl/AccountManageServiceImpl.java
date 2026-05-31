package com.velox.module.system.account.service.impl;

import com.velox.common.exception.ApiException;
import com.velox.common.exception.BusinessErrorCode;
import com.velox.common.result.PageResult;
import com.velox.framework.security.api.session.SecuritySessionService;
import com.velox.module.system.account.dto.AccountDetailCardDTO;
import com.velox.module.system.domain.model.Profile;
import com.velox.module.system.domain.model.Role;
import com.velox.module.system.domain.model.Account;
import com.velox.module.system.domain.model.AccountRole;
import com.velox.module.system.domain.model.AccountSecurity;
import com.velox.module.system.persistence.ProfileMapper;
import com.velox.module.system.persistence.RoleMapper;
import com.velox.module.system.persistence.AccountMapper;
import com.velox.module.system.persistence.AccountRoleMapper;
import com.velox.module.system.persistence.AccountSecurityMapper;
import com.velox.module.system.auth.service.PasswordCipherService;
import com.velox.module.system.auth.status.ActiveUserStatusService;
import com.velox.module.system.auth.properties.SystemAccountSecurityProperties;
import com.velox.module.system.common.enums.AccountStatusEnum;
import com.velox.module.system.id.generator.SystemEntityIdGenerator;
import com.velox.module.system.id.frontend.SystemFrontendIdCodecSupport;
import com.velox.module.system.permission.service.PermissionService;
import com.velox.framework.web.RequestDateTimeFormatter;
import com.velox.module.system.account.dto.AccountListItemDTO;
import com.velox.module.system.account.dto.AccountQuery;
import com.velox.module.system.account.dto.AccountSaveCommand;
import com.velox.module.system.account.dto.AdminProfileUpdateCommand;
import com.velox.module.system.account.service.AccountManageService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AccountManageServiceImpl implements AccountManageService {

    private final AccountMapper userMapper;
    private final ProfileMapper profileMapper;
    private final RoleMapper roleMapper;
    private final AccountRoleMapper userRoleMapper;
    private final AccountSecurityMapper accountSecurityMapper;
    private final PasswordCipherService passwordCipherService;
    private final PermissionService permissionService;
    private final SystemEntityIdGenerator entityIdGenerator;
    private final ActiveUserStatusService activeUserStatusService;
    private final SecuritySessionService securitySessionService;
    private final SystemAccountSecurityProperties accountSecurityProperties;
    private final ObjectMapper objectMapper;
    private final SystemFrontendIdCodecSupport frontendIdCodecSupport;

    public AccountManageServiceImpl(AccountMapper userMapper,
                                 ProfileMapper profileMapper,
                                 RoleMapper roleMapper,
                                 AccountRoleMapper userRoleMapper,
                                 AccountSecurityMapper accountSecurityMapper,
                                 PasswordCipherService passwordCipherService,
                                 PermissionService permissionService,
                                 SystemEntityIdGenerator entityIdGenerator,
                                 ActiveUserStatusService activeUserStatusService,
                                 SecuritySessionService securitySessionService,
                                 SystemAccountSecurityProperties accountSecurityProperties,
                                 ObjectMapper objectMapper,
                                 SystemFrontendIdCodecSupport frontendIdCodecSupport) {
        this.userMapper = userMapper;
        this.profileMapper = profileMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.accountSecurityMapper = accountSecurityMapper;
        this.passwordCipherService = passwordCipherService;
        this.permissionService = permissionService;
        this.entityIdGenerator = entityIdGenerator;
        this.activeUserStatusService = activeUserStatusService;
        this.securitySessionService = securitySessionService;
        this.accountSecurityProperties = accountSecurityProperties;
        this.objectMapper = objectMapper;
        this.frontendIdCodecSupport = frontendIdCodecSupport;
    }

    @Override
    public PageResult<AccountListItemDTO> list(AccountQuery query) {
        LambdaQueryWrapper<Account> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Account::getDeleted, 0);
        // 数据权限：仅可查看自身最高角色等级及以下的账号（同级及以下）。账号等级取其所有角色的最高 role_level；
        // 排除「拥有任一更高等级角色」的账号。用关联子查询在 SQL 层过滤，保证分页 total 正确
        // （sys_account 主表无别名，子查询可直接以表名关联其主键）。
        String currentUserId = securitySessionService.currentLoginIdOrNull();
        int operatorLevel = StringUtils.hasText(currentUserId)
                ? permissionService.getAccountHighestRoleLevel(currentUserId)
                : 0;
        wrapper.apply(
                "NOT EXISTS (SELECT 1 FROM sys_account_role ar JOIN sys_role r ON ar.role_id = r.id "
                        + "WHERE ar.account_id = sys_account.id AND ar.deleted = 0 AND r.deleted = 0 "
                        + "AND r.role_level > {0})",
                operatorLevel
        );
        if (StringUtils.hasText(query.getStatus())) {
            // status 现表示账号状态(1-启用 2-禁用 3-异常 4-注销)，在线/离线属于独立的活跃状态。
            try {
                int statusCode = Integer.parseInt(query.getStatus().trim());
                if (AccountStatusEnum.isValid(statusCode)) {
                    wrapper.eq(Account::getStatus, statusCode);
                }
            } catch (NumberFormatException ignored) {
                // 非法状态码忽略，等价于不按状态过滤
            }
        }
        if (StringUtils.hasText(query.getUsername())) {
            wrapper.like(Account::getUsername, query.getUsername().trim());
        }
        if (StringUtils.hasText(query.getRemark())) {
            wrapper.like(Account::getRemark, query.getRemark().trim());
        }
        if (StringUtils.hasText(query.getCreateTimeStart())) {
            wrapper.ge(Account::getCreateTime, RequestDateTimeFormatter.parseToUtc(query.getCreateTimeStart()));
        }
        if (StringUtils.hasText(query.getCreateTimeEnd())) {
            wrapper.le(Account::getCreateTime, RequestDateTimeFormatter.parseToUtc(query.getCreateTimeEnd()));
        }
        if (StringUtils.hasText(query.getUpdateTimeStart())) {
            wrapper.ge(Account::getUpdateTime, RequestDateTimeFormatter.parseToUtc(query.getUpdateTimeStart()));
        }
        if (StringUtils.hasText(query.getUpdateTimeEnd())) {
            wrapper.le(Account::getUpdateTime, RequestDateTimeFormatter.parseToUtc(query.getUpdateTimeEnd()));
        }
        if (StringUtils.hasText(query.getEmail())) {
            List<String> accountIds = accountSecurityMapper.selectList(new LambdaQueryWrapper<AccountSecurity>()
                            .eq(AccountSecurity::getDeleted, 0)
                            .like(AccountSecurity::getEmail, query.getEmail().trim().toLowerCase()))
                    .stream()
                    .map(AccountSecurity::getAccountId)
                    .filter(StringUtils::hasText)
                    .distinct()
                    .collect(Collectors.toList());
            if (accountIds.isEmpty()) {
                return new PageResult<>(0, query.getCurrent(), query.getSize(), new ArrayList<>());
            }
            wrapper.in(Account::getId, accountIds);
        }
        wrapper.orderByDesc(Account::getCreateTime)
                .orderByDesc(Account::getUpdateTime);

        String activeStatusFilter = normalizeActiveStatus(query.getActiveStatus());
        if (activeStatusFilter != null) {
            // 活跃状态(在线/离线)是运行期数据、无对应 DB 列，无法在 SQL 层过滤：
            // 取出全部命中记录后按活跃状态过滤再做内存分页，保证总数与分页正确。
            return listFilteredByActiveStatus(query, wrapper, activeStatusFilter);
        }

        Page<Account> page = new Page<>(query.getCurrent(), query.getSize());
        Page<Account> result = userMapper.selectPage(page, wrapper);
        List<Account> users = result.getRecords();
        long total = result.getTotal();

        List<String> accountIds = users.stream().map(Account::getId).collect(Collectors.toList());
        Map<String, Profile> profileMap = getActiveProfileMap(accountIds);
        Map<String, AccountSecurity> securityMap = getActiveSecurityMap(accountIds);
        Map<String, String> activeStatusMap = activeUserStatusService.resolveStatuses(accountIds);

        List<AccountListItemDTO> list = users.stream()
                .map(account -> toAccountListItem(
                        account,
                        profileMap.get(account.getId()),
                        securityMap.get(account.getId()),
                        resolveAccountStatus(account),
                        resolveActiveStatus(account, activeStatusMap)))
                .collect(Collectors.toList());
        return new PageResult<>(total, query.getCurrent(), query.getSize(), list);
    }

    private PageResult<AccountListItemDTO> listFilteredByActiveStatus(
            AccountQuery query, LambdaQueryWrapper<Account> wrapper, String activeStatusFilter) {
        List<Account> candidates = userMapper.selectList(wrapper);
        Map<String, String> activeStatusMap = activeUserStatusService.resolveStatuses(
                candidates.stream().map(Account::getId).collect(Collectors.toList()));
        List<Account> matched = candidates.stream()
                .filter(account -> activeStatusFilter.equals(resolveActiveStatus(account, activeStatusMap)))
                .collect(Collectors.toList());

        long total = matched.size();
        long current = query.getCurrent();
        long size = query.getSize();
        int from = (int) Math.min((long) matched.size(), Math.max(0L, (current - 1) * size));
        int to = (int) Math.min((long) matched.size(), (long) from + size);
        List<Account> pageRows = matched.subList(from, to);

        List<String> accountIds = pageRows.stream().map(Account::getId).collect(Collectors.toList());
        Map<String, Profile> profileMap = getActiveProfileMap(accountIds);
        Map<String, AccountSecurity> securityMap = getActiveSecurityMap(accountIds);

        List<AccountListItemDTO> list = pageRows.stream()
                .map(account -> toAccountListItem(
                        account,
                        profileMap.get(account.getId()),
                        securityMap.get(account.getId()),
                        resolveAccountStatus(account),
                        resolveActiveStatus(account, activeStatusMap)))
                .collect(Collectors.toList());
        return new PageResult<>(total, current, size, list);
    }

    private String normalizeActiveStatus(String activeStatus) {
        if (!StringUtils.hasText(activeStatus)) {
            return null;
        }
        String normalized = activeStatus.trim();
        if (ActiveUserStatusService.STATUS_ONLINE.equals(normalized)
                || ActiveUserStatusService.STATUS_OFFLINE.equals(normalized)) {
            return normalized;
        }
        return null;
    }

    @Override
    public AccountDetailCardDTO getDetailCard(String accountId) {
        String decodedAccountId = frontendIdCodecSupport.decodeIdentifier(accountId);
        Account account = getActiveUser(decodedAccountId);
        Profile profile = getActiveProfileMap(List.of(decodedAccountId)).get(decodedAccountId);
        AccountSecurity security = getActiveSecurityMap(List.of(decodedAccountId)).get(decodedAccountId);
        String accountStatus = resolveAccountStatus(account);
        String activeStatus = resolveActiveStatus(
                account,
                activeUserStatusService.resolveStatuses(List.of(decodedAccountId))
        );
        List<String> roleCodes = permissionService.getAccountRoleCodes(decodedAccountId);

        AccountDetailCardDTO dto = new AccountDetailCardDTO();
        dto.setHeader(buildHeader(account, profile, accountStatus, activeStatus, roleCodes));
        dto.setProfile(buildProfileSection(profile));
        dto.setAccount(buildAccountSection(account, accountStatus, activeStatus, roleCodes));
        dto.setSecurity(buildSecuritySection(security));
        dto.setThirdPartyProviders(buildThirdPartyProviders(security));
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String create(AccountSaveCommand command) {
        validateCreateCommand(command);
        ensureUsernameUnique(command.getUsername(), null);

        List<Role> roles = resolveRoles(command.getRoleCodes());
        String operator = currentOperator();

        Account account = new Account();
        account.setId(entityIdGenerator.nextId(Account.class));
        account.setUsername(command.getUsername().trim());
        account.setPassword(passwordCipherService.encode(command.getPassword().trim()));
        account.setRemark(normalizeNullable(command.getRemark()));
        account.setStatus(AccountStatusEnum.ENABLED.getCode());
        account.setLoginFailCount(0);
        account.setDeleted(0);
        account.setCreateBy(operator);
        account.setUpdateBy(operator);
        userMapper.insert(account);

        ensureProfile(account, operator);
        assignUserRoles(account.getId(), roles);
        return account.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(String userId, AccountSaveCommand command) {
        String decodedUserId = frontendIdCodecSupport.decodeIdentifier(userId);
        Account user = getActiveUser(decodedUserId);
        ensureCanUpdateUser(user.getId());
        ensureUsernameUnique(command.getUsername(), decodedUserId);

        List<Role> roles = resolveRoles(command.getRoleCodes());
        String operator = currentOperator();

        user.setUsername(command.getUsername().trim());
        user.setRemark(normalizeNullable(command.getRemark()));
        user.setUpdateBy(operator);
        if (StringUtils.hasText(command.getPassword())) {
            user.setPassword(passwordCipherService.encode(command.getPassword().trim()));
        }
        if (command.getAccountStatus() != null) {
            if (!AccountStatusEnum.isValid(command.getAccountStatus())) {
                throw new ApiException(BusinessErrorCode.OPERATION_FAILED);
            }
            user.setStatus(command.getAccountStatus());
        }
        userMapper.updateById(user);

        // 管理员手动置为”注销”视为即时硬注销：去掉自助注销的反悔期（冷静期）时间戳，
        // 否则详情页仍会显示待删除倒计时。注意 updateById 默认跳过 null，故用 Wrapper 显式置空。
        if (command.getAccountStatus() != null
                && AccountStatusEnum.CANCELLED.getCode() == command.getAccountStatus()) {
            userMapper.update(null, new LambdaUpdateWrapper<Account>()
                    .eq(Account::getId, decodedUserId)
                    .set(Account::getDeletionRequestedAt, null)
                    .set(Account::getDeletionExpiresAt, null)
                    .set(Account::getUpdateBy, operator)
                    .set(Account::getUpdateTime, LocalDateTime.now(ZoneOffset.UTC)));
        }

        ensureProfile(user, operator);
        assignUserRoles(decodedUserId, roles);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateProfile(String accountId, AdminProfileUpdateCommand command) {
        String decodedAccountId = frontendIdCodecSupport.decodeIdentifier(accountId);
        Account user = getActiveUser(decodedAccountId);
        ensureCanUpdateUser(user.getId());
        String operator = currentOperator();

        Profile profile = getOrInitProfile(user, operator);
        profile.setNickname(normalizeNullable(command.getNickname()));
        profile.setRealName(normalizeNullable(command.getRealName()));
        profile.setEmail(normalizeNullable(command.getEmail()));
        profile.setMobile(normalizeNullable(command.getPhone()));
        profile.setAddress(normalizeNullable(command.getAddress()));
        profile.setGender(normalizeGender(command.getGender()));
        profile.setIntroduction(normalizeNullable(command.getIntroduction()));
        profile.setSignature(normalizeNullable(command.getSignature()));
        profile.setPosition(normalizeNullable(command.getPosition()));
        profile.setCompany(normalizeNullable(command.getCompany()));
        profile.setTags(serializeTags(command.getTags()));
        if (StringUtils.hasText(command.getAvatar())) {
            profile.setAvatar(command.getAvatar().trim());
        }
        profile.setDeleted(0);
        profile.setUpdateBy(operator);

        if (profileMapper.selectById(profile.getId()) == null) {
            profileMapper.insert(profile);
        } else {
            profileMapper.updateById(profile);
        }
        // 资料属于账号信息的一部分：同步刷新账号更新时间，使列表”编辑时间”反映本次编辑。
        touchAccountUpdateTime(decodedAccountId, operator);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(String userId) {
        String decodedUserId = frontendIdCodecSupport.decodeIdentifier(userId);
        Account user = getActiveUser(decodedUserId);
        ensureCanDeleteUser(user.getId());
        String operator = currentOperator();

        userMapper.update(null, new LambdaUpdateWrapper<Account>()
                .eq(Account::getId, decodedUserId)
                .eq(Account::getDeleted, 0)
                .set(Account::getDeleted, 1)
                .set(Account::getUpdateTime, LocalDateTime.now(ZoneOffset.UTC))
                .set(Account::getUpdateBy, operator));

        profileMapper.update(null, new LambdaUpdateWrapper<Profile>()
                .eq(Profile::getAccountId, decodedUserId)
                .eq(Profile::getDeleted, 0)
                .set(Profile::getDeleted, 1)
                .set(Profile::getUpdateTime, LocalDateTime.now(ZoneOffset.UTC))
                .set(Profile::getUpdateBy, operator));

        List<String> activeRelationIds = userRoleMapper.selectAllByAccountId(decodedUserId).stream()
                .filter(relation -> Integer.valueOf(0).equals(relation.getDeleted()))
                .map(AccountRole::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (!activeRelationIds.isEmpty()) {
            userRoleMapper.logicalDeleteByIds(activeRelationIds, operator);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteBatch(List<String> accountIds) {
        if (accountIds == null || accountIds.isEmpty()) {
            return true;
        }
        accountIds.stream()
                .filter(StringUtils::hasText)
                .map(frontendIdCodecSupport::decodeIdentifier)
                .distinct()
                .forEach(this::delete);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelBatch(List<String> accountIds) {
        if (accountIds == null || accountIds.isEmpty()) {
            return true;
        }
        accountIds.stream()
                .filter(StringUtils::hasText)
                .map(frontendIdCodecSupport::decodeIdentifier)
                .distinct()
                .forEach(this::cancel);
        return true;
    }

    /**
     * 批量注销：将账号状态置为“注销(CANCELLED)”。注销不同于删除——账号不做逻辑删除，
     * 列表中仍会展示，但登录被拦截；同时清空自助注销的反悔期（冷静期）时间戳，
     * 与管理员在编辑抽屉手动置为“注销”的语义保持一致。
     */
    private void cancel(String userId) {
        Account user = getActiveUser(userId);
        ensureCanCancelUser(user.getId());
        String operator = currentOperator();
        userMapper.update(null, new LambdaUpdateWrapper<Account>()
                .eq(Account::getId, user.getId())
                .eq(Account::getDeleted, 0)
                .set(Account::getStatus, AccountStatusEnum.CANCELLED.getCode())
                .set(Account::getDeletionRequestedAt, null)
                .set(Account::getDeletionExpiresAt, null)
                .set(Account::getUpdateBy, operator)
                .set(Account::getUpdateTime, LocalDateTime.now(ZoneOffset.UTC)));
    }

    /**
     * 注销权限校验：禁止注销自己（避免把自己锁在门外），且只能操作角色层级更低的账号。
     * 语义与删除一致（禁止操作自身 + 需更高层级），但使用“更新”相关错误码（注销走 update 权限）。
     */
    private void ensureCanCancelUser(String userId) {
        String currentUserId = securitySessionService.currentLoginIdOrNull();
        if (!StringUtils.hasText(currentUserId)) {
            throw new ApiException(BusinessErrorCode.USER_UPDATE_FORBIDDEN);
        }
        if (currentUserId.equals(userId)) {
            throw new ApiException(BusinessErrorCode.USER_OPERATE_SELF_FORBIDDEN);
        }
        int operatorLevel = permissionService.getAccountHighestRoleLevel(currentUserId);
        int targetLevel = permissionService.getAccountHighestRoleLevel(userId);
        if (operatorLevel <= targetLevel) {
            throw new ApiException(BusinessErrorCode.USER_UPDATE_FORBIDDEN);
        }
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

    private void ensureCanDeleteUser(String userId) {
        String currentUserId = securitySessionService.currentLoginIdOrNull();
        if (!StringUtils.hasText(currentUserId)) {
            throw new ApiException(BusinessErrorCode.USER_DELETE_FORBIDDEN);
        }
        if (currentUserId.equals(userId)) {
            throw new ApiException(BusinessErrorCode.USER_OPERATE_SELF_FORBIDDEN);
        }
        int operatorLevel = permissionService.getAccountHighestRoleLevel(currentUserId);
        int targetLevel = permissionService.getAccountHighestRoleLevel(userId);
        if (operatorLevel <= targetLevel) {
            throw new ApiException(BusinessErrorCode.USER_DELETE_FORBIDDEN);
        }
    }

    private AccountListItemDTO toAccountListItem(
            Account user,
            Profile profile,
            AccountSecurity security,
            String accountStatus,
            String activeStatus
    ) {
        AccountListItemDTO dto = new AccountListItemDTO();
        dto.setAccountId(user.getId());
        dto.setAvatar(resolveAvatar(profile, user.getUsername()));
        dto.setStatus(accountStatus);
        dto.setActiveStatus(activeStatus);
        dto.setUsername(user.getUsername());
        dto.setEmail(security == null ? "" : defaultString(security.getEmail(), ""));
        dto.setRemark(defaultString(user.getRemark(), ""));
        dto.setCreateTime(RequestDateTimeFormatter.format(user.getCreateTime()));
        dto.setUpdateTime(RequestDateTimeFormatter.format(user.getUpdateTime()));
        return dto;
    }

    private AccountDetailCardDTO.Header buildHeader(
            Account account,
            Profile profile,
            String accountStatus,
            String activeStatus,
            List<String> roleCodes
    ) {
        AccountDetailCardDTO.Header header = new AccountDetailCardDTO.Header();
        header.setAvatar(resolveAvatar(profile, account.getUsername()));
        header.setUsername(account.getUsername());
        header.setNickname(profile == null ? null : profile.getNickname());
        header.setRealName(profile == null ? null : profile.getRealName());
        header.setStatus(accountStatus);
        header.setActiveStatus(activeStatus);
        header.setRemark(defaultString(account.getRemark(), ""));
        header.setRoleCodes(roleCodes);
        header.setCreateTime(RequestDateTimeFormatter.format(account.getCreateTime()));
        header.setUpdateTime(RequestDateTimeFormatter.format(account.getUpdateTime()));
        return header;
    }

    private AccountDetailCardDTO.ProfileSection buildProfileSection(Profile profile) {
        AccountDetailCardDTO.ProfileSection section = new AccountDetailCardDTO.ProfileSection();
        if (profile == null) {
            section.setTags(new ArrayList<>());
            return section;
        }
        section.setNickname(profile.getNickname());
        section.setGender(profile.getGender());
        section.setRealName(profile.getRealName());
        section.setDisplayEmail(profile.getEmail());
        section.setDisplayMobile(profile.getMobile());
        section.setAddress(profile.getAddress());
        section.setPosition(profile.getPosition());
        section.setCompany(profile.getCompany());
        section.setSignature(profile.getSignature());
        section.setIntroduction(profile.getIntroduction());
        section.setTags(parseTags(profile.getTags()));
        return section;
    }

    private AccountDetailCardDTO.AccountSection buildAccountSection(Account account, String accountStatus, String activeStatus, List<String> roleCodes) {
        AccountDetailCardDTO.AccountSection section = new AccountDetailCardDTO.AccountSection();
        section.setAccountId(account.getId());
        section.setUsername(account.getUsername());
        section.setRemark(account.getRemark());
        section.setStatus(accountStatus);
        section.setActiveStatus(activeStatus);
        section.setPendingDeletion(account.getDeletionExpiresAt() != null);
        section.setDeletionRequestedAt(RequestDateTimeFormatter.format(account.getDeletionRequestedAt()));
        section.setDeletionExpiresAt(RequestDateTimeFormatter.format(account.getDeletionExpiresAt()));
        section.setLoginFailCount(account.getLoginFailCount());
        section.setLoginFailTime(RequestDateTimeFormatter.format(account.getLoginFailTime()));
        section.setRoleCodes(roleCodes);
        return section;
    }

    private AccountDetailCardDTO.SecuritySection buildSecuritySection(AccountSecurity security) {
        AccountDetailCardDTO.SecuritySection section = new AccountDetailCardDTO.SecuritySection();
        List<String> allowed = new ArrayList<>(accountSecurityProperties.getLoginMethods().getEnabled());
        if (security == null) {
            section.setLoginMethods(new ArrayList<>());
            section.setDisabledLoginMethods(new ArrayList<>());
            section.setAllowedLoginMethods(allowed);
            section.setEmailMfaEnabled(false);
            section.setTotpMfaEnabled(false);
            return section;
        }
        section.setSecurityEmail(security.getEmail());
        section.setEmailMfaEnabled(Integer.valueOf(1).equals(security.getMfaEmailEnabled()));
        section.setTotpMfaEnabled(Integer.valueOf(1).equals(security.getMfaTotpEnabled()));
        section.setLoginMethods(parseLoginMethods(security.getLoginMethods()));
        section.setDisabledLoginMethods(parseLoginMethods(security.getDisabledLoginMethods()));
        section.setAllowedLoginMethods(allowed);
        section.setEmailVerifiedAt(RequestDateTimeFormatter.format(security.getEmailVerifiedAt()));
        section.setLastPasswordChangeAt(RequestDateTimeFormatter.format(security.getLastPasswordChangeAt()));
        return section;
    }

    private List<AccountDetailCardDTO.ThirdPartyProvider> buildThirdPartyProviders(AccountSecurity security) {
        List<String> disabledChannels = security == null
                ? new ArrayList<>()
                : parseLoginMethods(security.getDisabledOauthChannels());
        List<AccountDetailCardDTO.ThirdPartyProvider> providers = new ArrayList<>();
        providers.add(buildThirdPartyProvider("github", "GitHub", disabledChannels));
        providers.add(buildThirdPartyProvider("linuxdo", "LinuxDo", disabledChannels));
        return providers;
    }

    private AccountDetailCardDTO.ThirdPartyProvider buildThirdPartyProvider(String key, String name, List<String> disabledChannels) {
        AccountDetailCardDTO.ThirdPartyProvider provider = new AccountDetailCardDTO.ThirdPartyProvider();
        provider.setKey(key);
        provider.setName(name);
        provider.setBound(false);
        provider.setDisabled(disabledChannels.contains(key));
        return provider;
    }

    private String resolveAccountStatus(Account user) {
        return AccountStatusEnum.toStringCode(user.getStatus());
    }

    private String resolveActiveStatus(Account user, Map<String, String> activeStatusMap) {
        return activeStatusMap.getOrDefault(user.getId(), ActiveUserStatusService.STATUS_OFFLINE);
    }

    private void validateCreateCommand(AccountSaveCommand command) {
        if (!StringUtils.hasText(command.getPassword())) {
            throw new ApiException(BusinessErrorCode.USER_PASSWORD_REQUIRED_FOR_CREATE);
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

    private void ensureUsernameUnique(String username, String excludeUserId) {
        LambdaQueryWrapper<Account> wrapper = new LambdaQueryWrapper<Account>()
                .eq(Account::getDeleted, 0)
                .eq(Account::getUsername, username == null ? null : username.trim());
        if (excludeUserId != null) {
            wrapper.ne(Account::getId, excludeUserId);
        }
        if (userMapper.selectCount(wrapper) > 0) {
            throw new ApiException(BusinessErrorCode.USER_ALREADY_EXISTS);
        }
    }

    private List<Role> resolveRoles(Collection<String> roleCodes) {
        Set<String> distinctCodes = roleCodes == null ? Set.of() : roleCodes.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (distinctCodes.isEmpty()) {
            throw new ApiException(BusinessErrorCode.ROLE_REQUIRED);
        }

        List<Role> roles = roleMapper.selectList(new LambdaQueryWrapper<Role>()
                .eq(Role::getDeleted, 0)
                .in(Role::getRoleCode, distinctCodes));
        if (roles.size() != distinctCodes.size()) {
            throw new ApiException(BusinessErrorCode.ROLE_INVALID);
        }
        return roles;
    }

    private void assignUserRoles(String userId, List<Role> roles) {
        Set<String> requestedRoleIds = roles.stream()
                .map(Role::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        String operator = currentOperator();

        Map<String, AccountRole> activeRelationMap = new LinkedHashMap<>();
        Map<String, AccountRole> deletedRelationMap = new LinkedHashMap<>();
        for (AccountRole relation : userRoleMapper.selectAllByAccountId(userId)) {
            String roleId = relation.getRoleId();
            if (roleId == null) {
                continue;
            }
            if (Integer.valueOf(1).equals(relation.getDeleted())) {
                deletedRelationMap.merge(roleId, relation, this::preferLatestRelation);
                continue;
            }
            activeRelationMap.merge(roleId, relation, this::preferLatestRelation);
        }

        List<String> deleteIds = activeRelationMap.entrySet().stream()
                .filter(entry -> !requestedRoleIds.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .map(AccountRole::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (!deleteIds.isEmpty()) {
            userRoleMapper.logicalDeleteByIds(deleteIds, operator);
        }

        List<String> restoreIds = requestedRoleIds.stream()
                .filter(roleId -> !activeRelationMap.containsKey(roleId))
                .map(deletedRelationMap::get)
                .filter(Objects::nonNull)
                .map(AccountRole::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (!restoreIds.isEmpty()) {
            userRoleMapper.restoreByIds(restoreIds, operator);
        }

        List<AccountRole> entities = new ArrayList<>(roles.size());
        for (Role role : roles) {
            String roleId = role.getId();
            if (roleId == null || activeRelationMap.containsKey(roleId) || deletedRelationMap.containsKey(roleId)) {
                continue;
            }
            AccountRole userRole = new AccountRole();
            userRole.setId(entityIdGenerator.nextId(AccountRole.class));
            userRole.setAccountId(userId);
            userRole.setRoleId(roleId);
            userRole.setDeleted(0);
            userRole.setCreateBy(operator);
            userRole.setUpdateBy(operator);
            entities.add(userRole);
        }

        for (AccountRole entity : entities) {
            userRoleMapper.insert(entity);
        }
    }

    private Profile getOrInitProfile(Account user, String operator) {
        Profile profile = profileMapper.selectOne(new LambdaQueryWrapper<Profile>()
                .eq(Profile::getAccountId, user.getId())
                .last("limit 1"));
        if (profile != null) {
            if (!StringUtils.hasText(profile.getAvatar())) {
                profile.setAvatar(buildDefaultAvatar(user.getUsername()));
            }
            return profile;
        }

        Profile created = new Profile();
        created.setId(entityIdGenerator.nextId(Profile.class));
        created.setAccountId(user.getId());
        created.setAvatar(buildDefaultAvatar(user.getUsername()));
        created.setDeleted(0);
        created.setCreateBy(operator);
        created.setUpdateBy(operator);
        return created;
    }

    private void ensureProfile(Account user, String operator) {
        Profile profile = getOrInitProfile(user, operator);
        if (!StringUtils.hasText(profile.getNickname())) {
            profile.setNickname(user.getUsername());
        }
        profile.setDeleted(0);
        profile.setUpdateBy(operator);

        if (profileMapper.selectById(profile.getId()) == null) {
            profileMapper.insert(profile);
            return;
        }
        profileMapper.updateById(profile);
    }

    private Map<String, Profile> getActiveProfileMap(Collection<String> accountIds) {
        if (accountIds == null || accountIds.isEmpty()) {
            return Map.of();
        }
        return profileMapper.selectList(new LambdaQueryWrapper<Profile>()
                        .eq(Profile::getDeleted, 0)
                        .in(Profile::getAccountId, accountIds))
                .stream()
                .filter(profile -> StringUtils.hasText(profile.getAccountId()))
                .collect(Collectors.toMap(Profile::getAccountId, profile -> profile, (left, right) -> right, LinkedHashMap::new));
    }

    private Map<String, AccountSecurity> getActiveSecurityMap(Collection<String> accountIds) {
        if (accountIds == null || accountIds.isEmpty()) {
            return Map.of();
        }
        return accountSecurityMapper.selectList(new LambdaQueryWrapper<AccountSecurity>()
                        .eq(AccountSecurity::getDeleted, 0)
                        .in(AccountSecurity::getAccountId, accountIds))
                .stream()
                .filter(security -> StringUtils.hasText(security.getAccountId()))
                .collect(Collectors.toMap(AccountSecurity::getAccountId, security -> security, (left, right) -> right, LinkedHashMap::new));
    }

    private AccountRole preferLatestRelation(AccountRole left, AccountRole right) {
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }
        if (right.getUpdateTime() != null && (left.getUpdateTime() == null || right.getUpdateTime().isAfter(left.getUpdateTime()))) {
            return right;
        }
        if (left.getUpdateTime() != null && right.getUpdateTime() == null) {
            return left;
        }
        if (right.getCreateTime() != null && (left.getCreateTime() == null || right.getCreateTime().isAfter(left.getCreateTime()))) {
            return right;
        }
        return left;
    }

    private String normalizeNullable(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String defaultString(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    private List<String> parseTags(String tags) {
        if (!StringUtils.hasText(tags)) {
            return new ArrayList<>();
        }
        String normalized = tags.trim();
        if (normalized.startsWith("[") && normalized.endsWith("]")) {
            normalized = normalized.substring(1, normalized.length() - 1);
        }
        if (!StringUtils.hasText(normalized)) {
            return new ArrayList<>();
        }
        return Arrays.stream(normalized.split(","))
                .map(String::trim)
                .map(item -> item.replace("\"", ""))
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
    }

    private String serializeTags(List<String> tags) {
        if (tags == null) {
            return null;
        }
        List<String> normalized = tags.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .limit(10)
                .collect(Collectors.toList());
        if (normalized.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(normalized);
        } catch (Exception ex) {
            throw new ApiException(BusinessErrorCode.OPERATION_FAILED);
        }
    }

    private Integer normalizeGender(Integer gender) {
        if (gender == null) {
            return 0;
        }
        return switch (gender) {
            case 1, 2, 3 -> gender;
            default -> 0;
        };
    }

    private List<String> parseLoginMethods(String loginMethods) {
        if (!StringUtils.hasText(loginMethods)) {
            return new ArrayList<>();
        }
        return Arrays.stream(loginMethods.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
    }

    private String resolveAvatar(Profile profile, String username) {
        if (profile != null && StringUtils.hasText(profile.getAvatar())) {
            String avatar = profile.getAvatar();
            // 如果是完整 URL（http/https 开头）或已带路径前缀，直接返回
            if (avatar.startsWith("http://") || avatar.startsWith("https://") || avatar.startsWith("/")) {
                return avatar;
            }
            // 否则视为 DB 存储的文件 ID（如 F0000001），补全为可访问的 URL
            return "/api/file/db/" + avatar;
        }
        return buildDefaultAvatar(username);
    }

    private String buildDefaultAvatar(String username) {
        String seed = StringUtils.hasText(username) ? username.trim() : "user";
        return "https://api.dicebear.com/7.x/avataaars/svg?seed=" + seed;
    }

    private String currentOperator() {
        String loginId = securitySessionService.currentLoginIdOrNull();
        return StringUtils.hasText(loginId) ? loginId : "system";
    }

    /**
     * 刷新账号的更新时间/更新人。用于资料、安全等子表被修改时，让列表“编辑时间”同步反映本次编辑。
     * 走 Wrapper 显式置值（updateById 之外的无实体更新不触发审计填充）。
     */
    private void touchAccountUpdateTime(String accountId, String operator) {
        userMapper.update(null, new LambdaUpdateWrapper<Account>()
                .eq(Account::getId, accountId)
                .eq(Account::getDeleted, 0)
                .set(Account::getUpdateBy, operator)
                .set(Account::getUpdateTime, LocalDateTime.now(ZoneOffset.UTC)));
    }
}
