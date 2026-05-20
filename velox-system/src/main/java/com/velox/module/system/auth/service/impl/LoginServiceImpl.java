package com.velox.module.system.auth.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.velox.module.system.common.enums.RoleTypeEnum;
import com.velox.common.exception.ApiException;
import com.velox.common.exception.BusinessErrorCode;
import com.velox.email.api.builder.EmailBuilder;
import com.velox.email.api.message.SendResponse;
import com.velox.email.common.error.EmailErrorCode;
import com.velox.module.system.domain.model.Profile;
import com.velox.module.system.domain.model.Role;
import com.velox.module.system.domain.model.User;
import com.velox.module.system.domain.model.UserRole;
import com.velox.framework.id.BusinessIdGenerator;
import com.velox.framework.security.api.session.SecuritySessionService;
import com.velox.framework.security.properties.SecurityProperties;
import com.velox.module.system.persistence.ProfileMapper;
import com.velox.module.system.persistence.RoleMapper;
import com.velox.module.system.persistence.UserRoleMapper;
import com.velox.module.system.persistence.UserMapper;
import com.velox.module.system.auth.dto.CaptchaDTO;
import com.velox.module.system.auth.dto.ForgotPasswordCodeCommand;
import com.velox.module.system.auth.dto.LoginCommand;
import com.velox.module.system.auth.dto.LoginRoleDTO;
import com.velox.module.system.auth.dto.RegisterCommand;
import com.velox.module.system.auth.dto.ResetPasswordCommand;
import com.velox.module.system.auth.dto.TokenDTO;
import com.velox.module.system.auth.service.LoginService;
import com.velox.module.system.auth.service.PasswordCipherService;
import com.velox.module.system.auth.status.ActiveUserStatusService;
import com.velox.module.system.auth.store.VerificationCodeStore;
import com.wf.captcha.SpecCaptcha;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LoginServiceImpl implements LoginService {

    private final UserMapper userMapper;
    private final ProfileMapper profileMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final PasswordCipherService passwordCipherService;
    private final SecurityProperties securityProperties;
    private final BusinessIdGenerator businessIdGenerator;
    private final ObjectProvider<EmailBuilder> emailBuilderProvider;
    private final VerificationCodeStore verificationCodeStore;
    private final ActiveUserStatusService activeUserStatusService;
    private final SecuritySessionService securitySessionService;

    public LoginServiceImpl(UserMapper userMapper,
                            ProfileMapper profileMapper,
                            RoleMapper roleMapper,
                            UserRoleMapper userRoleMapper,
                            PasswordCipherService passwordCipherService,
                            SecurityProperties securityProperties,
                            BusinessIdGenerator businessIdGenerator,
                            ObjectProvider<EmailBuilder> emailBuilderProvider,
                            VerificationCodeStore verificationCodeStore,
                            ActiveUserStatusService activeUserStatusService,
                            SecuritySessionService securitySessionService) {
        this.userMapper = userMapper;
        this.profileMapper = profileMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.passwordCipherService = passwordCipherService;
        this.securityProperties = securityProperties;
        this.businessIdGenerator = businessIdGenerator;
        this.emailBuilderProvider = emailBuilderProvider;
        this.verificationCodeStore = verificationCodeStore;
        this.activeUserStatusService = activeUserStatusService;
        this.securitySessionService = securitySessionService;
    }

    @Override
    public CaptchaDTO generateCaptcha() {
        CaptchaDTO dto = new CaptchaDTO();
        dto.setIsCaptchaOn(true);

        SpecCaptcha specCaptcha = new SpecCaptcha(120, 40, 4);
        String key = IdUtil.simpleUUID();
        verificationCodeStore.saveCaptcha(key, specCaptcha.text());

        dto.setCaptchaCodeKey(key);
        dto.setCaptchaCodeImg(specCaptcha.toBase64());

        return dto;
    }

    @Override
    public java.util.List<LoginRoleDTO> listLoginRoles() {
        return roleMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Role>()
                        .eq(Role::getDeleted, 0)
                        .eq(Role::getType, RoleTypeEnum.SYSTEM.getCode())
                        .eq(Role::getEnabled, 1)
                        .orderByDesc(Role::getRoleLevel)
                        .orderByAsc(Role::getId))
                .stream()
                .map(role -> {
                    LoginRoleDTO dto = new LoginRoleDTO();
                    dto.setRoleName(role.getRoleName());
                    dto.setRoleCode(role.getRoleCode());
                    return dto;
                })
                .toList();
    }

    @Override
    public TokenDTO login(LoginCommand command) {
        validateCaptchaIfPresent(command.getCaptchaCode(), command.getCaptchaCodeKey());

        String username = command.getUsername();
        String password = command.getPassword();

        if (username == null || username.isBlank()) {
            throw new ApiException(BusinessErrorCode.LOGIN_FAILED);
        }

        if (password == null || password.isBlank()) {
            throw new ApiException(BusinessErrorCode.LOGIN_FAILED);
        }

        User user = userMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                .eq(User::getDeleted, 0)
                .eq(User::getUsername, username)
        );

        if (user == null) {
            throw new ApiException(BusinessErrorCode.LOGIN_FAILED);
        }

        checkLoginLock(user);

        if (!passwordCipherService.matches(password, user.getPassword())) {
            increaseLoginFailCount(user);
            throw new ApiException(BusinessErrorCode.LOGIN_FAILED);
        }

        if (Integer.valueOf(4).equals(user.getStatus())) {
            throw new ApiException(BusinessErrorCode.ACCOUNT_DISABLED);
        }

        ensureUserHasLoginRole(user.getId(), command.getRoleCode());

        resetLoginFailCount(user);
        upgradePasswordIfNeeded(user, password);

        String token = securitySessionService.login(user.getId());
        activeUserStatusService.recordLogin(user.getId());

        return new TokenDTO(token, null);
    }

    private void ensureUserHasLoginRole(String userId, String roleCode) {
        if (roleCode == null || roleCode.isBlank()) {
            throw new ApiException(BusinessErrorCode.LOGIN_FAILED);
        }

        Set<String> roleIds = userRoleMapper.selectAllByUserId(userId).stream()
                .filter(relation -> Integer.valueOf(0).equals(relation.getDeleted()))
                .map(UserRole::getRoleId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (roleIds.isEmpty()) {
            throw new ApiException(BusinessErrorCode.LOGIN_FAILED);
        }

        long matchedCount = roleMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Role>()
                .eq(Role::getDeleted, 0)
                .eq(Role::getType, RoleTypeEnum.SYSTEM.getCode())
                .eq(Role::getEnabled, 1)
                .eq(Role::getRoleCode, roleCode.trim())
                .in(Role::getId, roleIds));
        if (matchedCount <= 0) {
            throw new ApiException(BusinessErrorCode.LOGIN_FAILED);
        }
    }

    @Override
    public void register(RegisterCommand command) {
        if (!command.getPassword().equals(command.getConfirmPassword())) {
            throw new ApiException(BusinessErrorCode.PASSWORD_MISMATCH);
        }

        User existUser = userMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                .eq(User::getDeleted, 0)
                .eq(User::getUsername, command.getUsername())
        );

        if (existUser != null) {
            throw new ApiException(BusinessErrorCode.USER_ALREADY_EXISTS);
        }

        User user = new User();
        user.setId(businessIdGenerator.nextUserId());
        user.setUsername(command.getUsername());
        user.setPassword(passwordCipherService.encode(command.getPassword()));
        user.setStatus(1);
        user.setLoginFailCount(0);
        user.setDeleted(0);

        userMapper.insert(user);

        Profile profile = new Profile();
        profile.setId(businessIdGenerator.nextProfileId());
        profile.setUserId(user.getId());
        profile.setNickname(command.getUsername());
        profile.setAvatar(buildDefaultAvatar(command.getUsername()));
        profile.setGender(0);
        profile.setDeleted(0);
        profileMapper.insert(profile);

        Role defaultRole = roleMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Role>()
                .eq(Role::getDeleted, 0)
                .eq(Role::getRoleCode, "R_USER")
                .last("limit 1"));
        if (defaultRole != null && defaultRole.getId() != null) {
            UserRole userRole = new UserRole();
            userRole.setId(businessIdGenerator.nextUserRoleId());
            userRole.setUserId(user.getId());
            userRole.setRoleId(defaultRole.getId());
            userRole.setDeleted(0);
            userRoleMapper.insert(userRole);
        }
    }

    @Override
    public void sendResetPasswordCode(ForgotPasswordCodeCommand command) {
        String email = normalizeEmail(command.getEmail());
        if (email == null) {
            throw new ApiException(BusinessErrorCode.EMAIL_REQUIRED);
        }

        User user = findUserByEmail(email);
        if (user == null) {
            throw new ApiException(BusinessErrorCode.EMAIL_NOT_BOUND);
        }

        EmailBuilder emailBuilder = requireEmailBuilder();
        String code = RandomUtil.randomNumbers(6);
        if (!verificationCodeStore.trySaveResetCode(email, code)) {
            throw new ApiException(BusinessErrorCode.RESET_CODE_SEND_TOO_FREQUENT);
        }
        try {
            SendResponse response = emailBuilder.to(email)
                    .subject("密码重置验证码")
                    .text(buildResetPasswordMailContent(user.getUsername(), code))
                    .sendSync();
            if (!response.success()) {
                verificationCodeStore.invalidateResetCode(email);
                if (response.errorCode() == EmailErrorCode.DISABLED.code()) {
                    throw new ApiException(BusinessErrorCode.EMAIL_SERVICE_DISABLED);
                }
                throw new ApiException(BusinessErrorCode.EMAIL_SEND_FAILED);
            }
        } catch (ApiException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            verificationCodeStore.invalidateResetCode(email);
            throw new ApiException(exception, BusinessErrorCode.EMAIL_SEND_FAILED);
        }
    }

    @Override
    public void resetPassword(ResetPasswordCommand command) {
        String email = normalizeEmail(command.getEmail());
        if (email == null) {
            throw new ApiException(BusinessErrorCode.EMAIL_REQUIRED);
        }
        if (!command.getNewPassword().equals(command.getConfirmPassword())) {
            throw new ApiException(BusinessErrorCode.PASSWORD_MISMATCH);
        }

        User user = findUserByEmail(email);
        if (user == null) {
            throw new ApiException(BusinessErrorCode.EMAIL_NOT_BOUND);
        }

        VerificationCodeStore.VerificationResult verificationResult =
                verificationCodeStore.verifyResetCode(email, command.getCode());
        if (verificationResult == VerificationCodeStore.VerificationResult.EXPIRED) {
            throw new ApiException(BusinessErrorCode.RESET_CODE_EXPIRED);
        }
        if (verificationResult == VerificationCodeStore.VerificationResult.INVALID) {
            throw new ApiException(BusinessErrorCode.RESET_CODE_ERROR);
        }

        if (passwordCipherService.matches(command.getNewPassword(), user.getPassword())) {
            throw new ApiException(BusinessErrorCode.PASSWORD_SAME_AS_OLD);
        }

        user.setPassword(passwordCipherService.encode(command.getNewPassword().trim()));
        user.setLoginFailCount(0);
        user.setLoginFailTime(null);
        userMapper.updateById(user);
    }

    @Override
    public void logout() {
        String userId = securitySessionService.currentLoginIdOrNull();
        securitySessionService.logout();
        activeUserStatusService.recordLogout(userId);
    }

    private void validateCaptchaIfPresent(String captchaCode, String key) {
        boolean captchaCodeBlank = captchaCode == null || captchaCode.isBlank();
        boolean keyBlank = key == null || key.isBlank();

        if (captchaCodeBlank && keyBlank) {
            return;
        }

        if (captchaCodeBlank || keyBlank) {
            throw new ApiException(BusinessErrorCode.CAPTCHA_ERROR);
        }

        VerificationCodeStore.VerificationResult captchaResult = verificationCodeStore.consumeCaptcha(key, captchaCode);
        if (captchaResult == VerificationCodeStore.VerificationResult.EXPIRED) {
            throw new ApiException(BusinessErrorCode.CAPTCHA_EXPIRED);
        }
        if (captchaResult == VerificationCodeStore.VerificationResult.INVALID) {
            throw new ApiException(BusinessErrorCode.CAPTCHA_ERROR);
        }
    }

    private void checkLoginLock(User user) {
        if (user.getLoginFailTime() == null) {
            return;
        }
        java.time.LocalDateTime now = java.time.LocalDateTime.now(java.time.ZoneOffset.UTC);
        if (user.getLoginFailTime().isAfter(now)) {
            throw new ApiException(BusinessErrorCode.ACCOUNT_LOCKED);
        }
        user.setLoginFailCount(0);
        user.setLoginFailTime(null);
        userMapper.updateById(user);
    }

    private void increaseLoginFailCount(User user) {
        int failCount = user.getLoginFailCount() == null ? 0 : user.getLoginFailCount();
        user.setLoginFailCount(failCount + 1);

        if (failCount + 1 >= securityProperties.getLogin().getMaxFailCount()) {
            user.setLoginFailTime(java.time.LocalDateTime.now(java.time.ZoneOffset.UTC)
                    .plusMinutes(securityProperties.getLogin().getLockMinutes()));
        }

        userMapper.updateById(user);
    }

    private void resetLoginFailCount(User user) {
        if (user.getLoginFailCount() != null && user.getLoginFailCount() > 0) {
            user.setLoginFailCount(0);
            user.setLoginFailTime(null);
            userMapper.updateById(user);
        }
    }

    private void upgradePasswordIfNeeded(User user, String rawPassword) {
        if (!passwordCipherService.needsUpgrade(user.getPassword())) {
            return;
        }
        user.setPassword(passwordCipherService.encode(rawPassword));
        userMapper.updateById(user);
    }

    private String buildDefaultAvatar(String username) {
        String seed = username == null || username.isBlank() ? "user" : username.trim();
        return "https://api.dicebear.com/7.x/avataaars/svg?seed=" + seed;
    }

    private User findUserByEmail(String email) {
        return userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getDeleted, 0)
                        .eq(User::getEmail, email)
                        .last("limit 1")
        );
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim().toLowerCase();
    }

    private String buildResetPasswordMailContent(String username, String code) {
        return "您好，" + username + "：\n\n"
                + "您正在执行忘记密码操作。\n"
                + "本次密码重置验证码为：" + code + "\n"
                + "验证码 10 分钟内有效，请勿泄露给他人。\n\n"
                + "如果这不是您的操作，请忽略本邮件。";
    }

    private EmailBuilder requireEmailBuilder() {
        EmailBuilder emailBuilder = emailBuilderProvider.getIfAvailable();
        if (emailBuilder == null) {
            throw new ApiException(BusinessErrorCode.EMAIL_SERVICE_DISABLED);
        }
        return emailBuilder;
    }
}
