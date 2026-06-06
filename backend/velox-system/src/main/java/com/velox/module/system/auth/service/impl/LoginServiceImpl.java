package com.velox.module.system.auth.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.velox.common.exception.ApiException;
import com.velox.common.exception.BusinessErrorCode;
import com.velox.email.api.builder.EmailBuilder;
import com.velox.email.api.message.SendResponse;
import com.velox.email.common.error.EmailErrorCode;
import com.velox.framework.security.api.session.SecuritySessionService;
import com.velox.module.system.auth.dto.CaptchaDTO;
import com.velox.module.system.auth.dto.CodeLoginCommand;
import com.velox.module.system.auth.dto.ForgotPasswordCodeCommand;
import com.velox.module.system.auth.dto.LoginCodeSendCommand;
import com.velox.module.system.auth.dto.LoginCommand;
import com.velox.module.system.auth.dto.MfaChallengeSendCodeCommand;
import com.velox.module.system.auth.dto.MfaChallengeVerifyCommand;
import com.velox.module.system.auth.dto.RegisterCommand;
import com.velox.module.system.auth.dto.ResetPasswordCommand;
import com.velox.module.system.auth.dto.TokenDTO;
import com.velox.module.system.auth.properties.SystemAccountSecurityProperties;
import com.velox.module.system.auth.properties.SystemAuthProperties;
import com.velox.module.system.auth.service.LoginService;
import com.velox.module.system.auth.service.PasswordCipherService;
import com.velox.module.system.auth.status.ActiveUserStatusService;
import com.velox.module.system.auth.store.VerificationCodeStore;
import com.velox.module.system.accesscontrol.service.AccessControlService;
import com.velox.module.system.accesscontrol.vo.AccessControlRespVO;
import com.velox.module.system.common.enums.AccountStatusEnum;
import com.velox.module.system.domain.model.Profile;
import com.velox.module.system.domain.model.Role;
import com.velox.module.system.domain.model.Account;
import com.velox.module.system.domain.model.AccountRole;
import com.velox.module.system.domain.model.AccountSecurity;
import com.velox.module.system.domain.model.AccountSession;
import com.velox.module.system.id.generator.SystemEntityIdGenerator;
import com.velox.module.system.mail.service.MailTemplateRenderService;
import com.velox.module.system.mail.template.MailTemplateType;
import com.velox.module.system.mail.template.RecipientLanguageResolver;
import com.velox.module.system.mail.template.RenderedEmail;
import com.velox.module.system.persistence.ProfileMapper;
import com.velox.module.system.persistence.RoleMapper;
import com.velox.module.system.persistence.AccountMapper;
import com.velox.module.system.persistence.AccountRoleMapper;
import com.velox.module.system.persistence.AccountSecurityMapper;
import com.velox.framework.totp.api.model.TotpVerifyResult;
import com.velox.framework.totp.api.service.TotpService;
import com.wf.captcha.SpecCaptcha;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LoginServiceImpl implements LoginService {

    private static final java.util.regex.Pattern EMAIL_PATTERN = java.util.regex.Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final java.util.regex.Pattern PHONE_PATTERN = java.util.regex.Pattern.compile(
            "^1\\d{10}$");

    private final AccountMapper userMapper;
    private final ProfileMapper profileMapper;
    private final RoleMapper roleMapper;
    private final AccountRoleMapper userRoleMapper;
    private final AccountSecurityMapper userSecurityMapper;
    private final PasswordCipherService passwordCipherService;
    private final SystemAuthProperties authProperties;
    private final SystemAccountSecurityProperties accountSecurityProperties;
    private final SystemEntityIdGenerator entityIdGenerator;
    private final ObjectProvider<EmailBuilder> emailBuilderProvider;
    private final VerificationCodeStore verificationCodeStore;
    private final ActiveUserStatusService activeUserStatusService;
    private final SecuritySessionService securitySessionService;
    private final TotpService totpService;
    private final AccessControlService accessControlService;
    private final MailTemplateRenderService mailTemplateRenderService;
    private final RecipientLanguageResolver recipientLanguageResolver;

    public LoginServiceImpl(AccountMapper userMapper,
                            ProfileMapper profileMapper,
                            RoleMapper roleMapper,
                            AccountRoleMapper userRoleMapper,
                            AccountSecurityMapper userSecurityMapper,
                            PasswordCipherService passwordCipherService,
                            SystemAuthProperties authProperties,
                            SystemAccountSecurityProperties accountSecurityProperties,
                            SystemEntityIdGenerator entityIdGenerator,
                            ObjectProvider<EmailBuilder> emailBuilderProvider,
                            VerificationCodeStore verificationCodeStore,
                            ActiveUserStatusService activeUserStatusService,
                            SecuritySessionService securitySessionService,
                            TotpService totpService,
                            AccessControlService accessControlService,
                            MailTemplateRenderService mailTemplateRenderService,
                            RecipientLanguageResolver recipientLanguageResolver) {
        this.userMapper = userMapper;
        this.profileMapper = profileMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.userSecurityMapper = userSecurityMapper;
        this.passwordCipherService = passwordCipherService;
        this.authProperties = authProperties;
        this.accountSecurityProperties = accountSecurityProperties;
        this.entityIdGenerator = entityIdGenerator;
        this.emailBuilderProvider = emailBuilderProvider;
        this.verificationCodeStore = verificationCodeStore;
        this.activeUserStatusService = activeUserStatusService;
        this.securitySessionService = securitySessionService;
        this.totpService = totpService;
        this.accessControlService = accessControlService;
        this.mailTemplateRenderService = mailTemplateRenderService;
        this.recipientLanguageResolver = recipientLanguageResolver;
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
    public AccessControlRespVO getAccessConfig() {
        return accessControlService.getConfig();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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

        Account user = findUserByAccount(username);

        if (user == null) {
            throw new ApiException(BusinessErrorCode.LOGIN_FAILED);
        }

        checkLoginLock(user);

        if (!passwordCipherService.matches(password, user.getPassword())) {
            increaseLoginFailCount(user);
            throw new ApiException(BusinessErrorCode.LOGIN_FAILED);
        }

        if (isPendingDeletion(user)) {
            return issuePendingDeletionToken(user);
        }

        if (AccountStatusEnum.isLoginBlocked(user.getStatus())) {
            throw new ApiException(BusinessErrorCode.ACCOUNT_DISABLED);
        }

        AccountSecurity security = ensureUserSecurity(user);
        ensureLoginMethodAllowed(security, "password");

        resetLoginFailCount(user);
        upgradePasswordIfNeeded(user, password);

        String mfaType = resolveMfaType(security, "password");
        if (mfaType != null) {
            return issueMfaChallenge(user, mfaType);
        }

        return performLogin(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterCommand command) {
        if (!accessControlService.isGeneralRegisterEnabled()) {
            throw new ApiException(BusinessErrorCode.REGISTER_DISABLED);
        }
        if (!command.getPassword().equals(command.getConfirmPassword())) {
            throw new ApiException(BusinessErrorCode.PASSWORD_MISMATCH);
        }

        Account existUser = userMapper.selectOne(
            new LambdaQueryWrapper<Account>()
                .eq(Account::getDeleted, 0)
                .eq(Account::getUsername, command.getUsername())
        );

        if (existUser != null) {
            throw new ApiException(BusinessErrorCode.USER_ALREADY_EXISTS);
        }

        Account user = new Account();
        user.setId(entityIdGenerator.nextId(Account.class));
        user.setUsername(command.getUsername());
        user.setPassword(passwordCipherService.encode(command.getPassword()));
        user.setStatus(AccountStatusEnum.ENABLED.getCode());
        user.setLoginFailCount(0);
        user.setDeleted(0);

        userMapper.insert(user);

        Profile profile = new Profile();
        profile.setId(entityIdGenerator.nextId(Profile.class));
        profile.setAccountId(user.getId());
        profile.setNickname(command.getUsername());
        profile.setAvatar(buildDefaultAvatar(command.getUsername()));
        profile.setGender(0);
        profile.setDeleted(0);
        profileMapper.insert(profile);

        Role defaultRole = roleMapper.selectOne(new LambdaQueryWrapper<Role>()
                .eq(Role::getDeleted, 0)
                .eq(Role::getRoleCode, "R_USER")
                .last("limit 1"));
        if (defaultRole != null && defaultRole.getId() != null) {
            AccountRole userRole = new AccountRole();
            userRole.setId(entityIdGenerator.nextId(AccountRole.class));
            userRole.setAccountId(user.getId());
            userRole.setRoleId(defaultRole.getId());
            userRole.setDeleted(0);
            userRoleMapper.insert(userRole);
        }

        AccountSecurity security = new AccountSecurity();
        security.setId(entityIdGenerator.nextId(AccountSecurity.class));
        security.setAccountId(user.getId());
        security.setLoginMethods(String.join(",",
                accountSecurityProperties.getLoginMethods().getDefaults()));
        security.setMfaEmailEnabled(0);
        security.setMfaTotpEnabled(0);
        security.setDeleted(0);
        userSecurityMapper.insert(security);
    }

    @Override
    public void sendResetPasswordCode(ForgotPasswordCodeCommand command) {
        if (!accessControlService.isForgotPasswordEnabled()) {
            throw new ApiException(BusinessErrorCode.FORGOT_PASSWORD_DISABLED);
        }
        String email = normalizeEmail(command.getEmail());
        if (email == null) {
            throw new ApiException(BusinessErrorCode.EMAIL_REQUIRED);
        }

        Account user = findUserByEmail(email);
        if (user == null) {
            throw new ApiException(BusinessErrorCode.EMAIL_NOT_BOUND);
        }

        EmailBuilder emailBuilder = requireEmailBuilder();
        String code = RandomUtil.randomNumbers(6);
        if (!verificationCodeStore.trySaveResetCode(email, code)) {
            throw new ApiException(BusinessErrorCode.RESET_CODE_SEND_TOO_FREQUENT);
        }
        try {
            RenderedEmail mail = mailTemplateRenderService.render(
                    MailTemplateType.AUTH_RESET_PASSWORD_CODE,
                    recipientLanguageResolver.resolve(user.getId()),
                    Map.of("username", user.getUsername(), "code", code,
                            "validityMinutes", "10", "appName", "Velox"));
            SendResponse response = emailBuilder.to(email)
                    .subject(mail.subject())
                    .html(mail.html())
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
        if (!accessControlService.isForgotPasswordEnabled()) {
            throw new ApiException(BusinessErrorCode.FORGOT_PASSWORD_DISABLED);
        }
        String email = normalizeEmail(command.getEmail());
        if (email == null) {
            throw new ApiException(BusinessErrorCode.EMAIL_REQUIRED);
        }
        if (!command.getNewPassword().equals(command.getConfirmPassword())) {
            throw new ApiException(BusinessErrorCode.PASSWORD_MISMATCH);
        }

        Account user = findUserByEmail(email);
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
    @Transactional(rollbackFor = Exception.class)
    public void logout() {
        String userId = securitySessionService.currentLoginIdOrNull();
        String tokenValue = securitySessionService.currentTokenOrNull();
        securitySessionService.logout();
        activeUserStatusService.recordLogout(userId, tokenValue);
    }

    @Override
    public void sendLoginCode(LoginCodeSendCommand command) {
        String type = command.getType() == null ? "" : command.getType().trim().toLowerCase();
        if (!"email".equals(type)) {
            throw new ApiException(BusinessErrorCode.EMAIL_REQUIRED);
        }

        String email = normalizeEmail(command.getTarget());
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new ApiException(BusinessErrorCode.EMAIL_REQUIRED);
        }

        Account user = findUserByEmail(email);
        if (user == null) {
            throw new ApiException(BusinessErrorCode.EMAIL_NOT_BOUND);
        }

        EmailBuilder emailBuilder = requireEmailBuilder();
        String code = RandomUtil.randomNumbers(6);
        if (!verificationCodeStore.trySaveLoginCode(email, code)) {
            throw new ApiException(BusinessErrorCode.LOGIN_CODE_SEND_TOO_FREQUENT);
        }
        try {
            RenderedEmail mail = mailTemplateRenderService.render(
                    MailTemplateType.AUTH_LOGIN_CODE,
                    recipientLanguageResolver.resolve(user.getId()),
                    Map.of("username", user.getUsername(), "code", code,
                            "validityMinutes", "10", "appName", "Velox"));
            SendResponse response = emailBuilder.to(email)
                    .subject(mail.subject())
                    .html(mail.html())
                    .sendSync();
            if (!response.success()) {
                verificationCodeStore.invalidateLoginCode(email);
                if (response.errorCode() == EmailErrorCode.DISABLED.code()) {
                    throw new ApiException(BusinessErrorCode.EMAIL_SERVICE_DISABLED);
                }
                throw new ApiException(BusinessErrorCode.EMAIL_SEND_FAILED);
            }
        } catch (ApiException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            verificationCodeStore.invalidateLoginCode(email);
            throw new ApiException(exception, BusinessErrorCode.EMAIL_SEND_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TokenDTO loginByCode(CodeLoginCommand command) {
        String type = command.getType() == null ? "" : command.getType().trim().toLowerCase();
        if (!"email".equals(type)) {
            throw new ApiException(BusinessErrorCode.EMAIL_REQUIRED);
        }

        String email = normalizeEmail(command.getTarget());
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new ApiException(BusinessErrorCode.EMAIL_REQUIRED);
        }

        Account user = findUserByEmail(email);
        if (user == null) {
            throw new ApiException(BusinessErrorCode.EMAIL_NOT_BOUND);
        }

        checkLoginLock(user);

        VerificationCodeStore.VerificationResult verificationResult =
                verificationCodeStore.verifyLoginCode(email, command.getCode());
        if (verificationResult == VerificationCodeStore.VerificationResult.EXPIRED) {
            throw new ApiException(BusinessErrorCode.LOGIN_CODE_EXPIRED);
        }
        if (verificationResult == VerificationCodeStore.VerificationResult.INVALID) {
            increaseLoginFailCount(user);
            throw new ApiException(BusinessErrorCode.LOGIN_CODE_ERROR);
        }

        if (isPendingDeletion(user)) {
            return issuePendingDeletionToken(user);
        }

        if (AccountStatusEnum.isLoginBlocked(user.getStatus())) {
            throw new ApiException(BusinessErrorCode.ACCOUNT_DISABLED);
        }

        AccountSecurity security = ensureUserSecurity(user);
        ensureLoginMethodAllowed(security, "email_code");

        resetLoginFailCount(user);

        // 邮箱验证码登录天然完成了邮箱因素校验，因此跳过邮箱二次验证；
        // 但 TOTP 是独立因素，仍需要继续走虚拟 MFA 设备验证挑战。
        String mfaType = resolveMfaType(security, "email_code");
        if (mfaType != null) {
            return issueMfaChallenge(user, mfaType);
        }

        return performLogin(user);
    }

    @Override
    public void sendMfaChallengeCode(MfaChallengeSendCodeCommand command) {
        String userId = verificationCodeStore.peekMfaChallenge(command.getChallenge());
        if (!StringUtils.hasText(userId)) {
            throw new ApiException(BusinessErrorCode.MFA_CHALLENGE_INVALID);
        }
        Account user = userMapper.selectOne(new LambdaQueryWrapper<Account>()
                .eq(Account::getId, userId)
                .eq(Account::getDeleted, 0)
                .last("limit 1"));
        if (user == null) {
            throw new ApiException(BusinessErrorCode.USER_NOT_FOUND);
        }
        AccountSecurity security = ensureUserSecurity(user);
        // 仅当前挑战属于"邮箱二次验证"时才能下发；TOTP 由认证器生成，无需也不允许触发邮件。
        if (Integer.valueOf(1).equals(security.getMfaTotpEnabled())) {
            throw new ApiException(BusinessErrorCode.MFA_CHALLENGE_INVALID);
        }
        if (!Integer.valueOf(1).equals(security.getMfaEmailEnabled())) {
            throw new ApiException(BusinessErrorCode.MFA_NOT_ENABLED);
        }
        String email = normalizeEmail(security.getEmail());
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new ApiException(BusinessErrorCode.EMAIL_NOT_BOUND_TO_USER);
        }

        EmailBuilder emailBuilder = requireEmailBuilder();
        SystemAccountSecurityProperties.Mfa.Email mfaConfig = accountSecurityProperties.getMfa().getEmail();
        String code = RandomUtil.randomNumbers(6);
        if (!verificationCodeStore.trySaveMfaCode(userId, code,
                mfaConfig.getTtlSeconds(), mfaConfig.getResendIntervalSeconds())) {
            throw new ApiException(BusinessErrorCode.MFA_CODE_SEND_TOO_FREQUENT);
        }
        try {
            RenderedEmail mail = mailTemplateRenderService.render(
                    MailTemplateType.AUTH_LOGIN_MFA_CODE,
                    recipientLanguageResolver.resolve(user.getId()),
                    Map.of("username", user.getUsername(), "code", code,
                            "validityMinutes", "5", "appName", "Velox"));
            SendResponse response = emailBuilder.to(email)
                    .subject(mail.subject())
                    .html(mail.html())
                    .sendSync();
            if (!response.success()) {
                verificationCodeStore.invalidateMfaCode(userId);
                if (response.errorCode() == EmailErrorCode.DISABLED.code()) {
                    throw new ApiException(BusinessErrorCode.EMAIL_SERVICE_DISABLED);
                }
                throw new ApiException(BusinessErrorCode.EMAIL_SEND_FAILED);
            }
        } catch (ApiException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            verificationCodeStore.invalidateMfaCode(userId);
            throw new ApiException(exception, BusinessErrorCode.EMAIL_SEND_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TokenDTO verifyMfaChallenge(MfaChallengeVerifyCommand command) {
        String userId = verificationCodeStore.peekMfaChallenge(command.getChallenge());
        if (!StringUtils.hasText(userId)) {
            throw new ApiException(BusinessErrorCode.MFA_CHALLENGE_EXPIRED);
        }
        Account user = userMapper.selectOne(new LambdaQueryWrapper<Account>()
                .eq(Account::getId, userId)
                .eq(Account::getDeleted, 0)
                .last("limit 1"));
        if (user == null) {
            throw new ApiException(BusinessErrorCode.USER_NOT_FOUND);
        }

        AccountSecurity security = ensureUserSecurity(user);
        if (Integer.valueOf(1).equals(security.getMfaTotpEnabled())) {
            if (!StringUtils.hasText(security.getMfaTotpSecret())) {
                throw new ApiException(BusinessErrorCode.MFA_TOTP_NOT_PROVISIONED);
            }
            TotpVerifyResult totpResult = totpService.verify(security.getMfaTotpSecret(), command.getCode());
            if (!totpResult.matched()) {
                throw new ApiException(BusinessErrorCode.MFA_TOTP_CODE_ERROR);
            }
        } else if (Integer.valueOf(1).equals(security.getMfaEmailEnabled())) {
            VerificationCodeStore.VerificationResult result =
                    verificationCodeStore.verifyMfaCode(userId, command.getCode());
            if (result == VerificationCodeStore.VerificationResult.EXPIRED) {
                throw new ApiException(BusinessErrorCode.MFA_CODE_EXPIRED);
            }
            if (result == VerificationCodeStore.VerificationResult.INVALID) {
                throw new ApiException(BusinessErrorCode.MFA_CODE_ERROR);
            }
        } else {
            // 挑战已颁发但用户中途关闭了所有虚拟 MFA 设备验证方式 —— 让挑战失效以保持一致性。
            throw new ApiException(BusinessErrorCode.MFA_NOT_ENABLED);
        }

        verificationCodeStore.consumeMfaChallenge(command.getChallenge());

        return performLogin(user);
    }

    private TokenDTO performLogin(Account user) {
        String sessionId = entityIdGenerator.nextId(AccountSession.class);
        String token = securitySessionService.login(user.getId(), sessionId);
        try {
            activeUserStatusService.recordLogin(user.getId(), sessionId, token);
        } catch (RuntimeException exception) {
            try {
                securitySessionService.logout();
            } catch (RuntimeException ignored) {
                // 会话表写入失败时优先回滚当前 token，避免发出不可追踪的登录态。
            }
            throw exception;
        }
        return new TokenDTO(token, null);
    }

    private TokenDTO issuePendingDeletionToken(Account user) {
        String sessionId = entityIdGenerator.nextId(AccountSession.class);
        String token = securitySessionService.login(user.getId(), sessionId);
        activeUserStatusService.recordLogin(user.getId(), sessionId, token);
        Profile profile = profileMapper.selectOne(new LambdaQueryWrapper<Profile>()
                .eq(Profile::getAccountId, user.getId())
                .eq(Profile::getDeleted, 0)
                .last("limit 1"));
        AccountSecurity security = ensureUserSecurity(user);
        TokenDTO dto = new TokenDTO();
        dto.setToken(token);
        dto.setPendingDeletion(true);
        dto.setAccountId(user.getId());
        dto.setUserName(user.getUsername());
        dto.setAvatar(buildAvatar(profile, user.getUsername()));
        dto.setEmail(normalizeEmail(security.getEmail()));
        dto.setDeletionRequestedAt(formatTime(user.getDeletionRequestedAt()));
        dto.setDeletionExpiresAt(formatTime(user.getDeletionExpiresAt()));
        return dto;
    }

    /**
     * 解析当前登录方式下应该走的虚拟 MFA 设备验证类型：
     * - 优先 TOTP（独立因素，对所有登录方式生效）
     * - 其次邮箱二次验证，但仅对密码登录生效（邮箱验证码登录本身已校验邮箱）
     */
    private String resolveMfaType(AccountSecurity security, String loginMethod) {
        if (security == null) {
            return null;
        }
        if (Integer.valueOf(1).equals(security.getMfaTotpEnabled()) && totpService.isEnabled()) {
            return "totp";
        }
        SystemAccountSecurityProperties.Mfa mfaConfig = accountSecurityProperties.getMfa();
        if ("password".equals(loginMethod)
                && mfaConfig.getEmail().isEnabled()
                && Integer.valueOf(1).equals(security.getMfaEmailEnabled())) {
            return "email";
        }
        return null;
    }

    private TokenDTO issueMfaChallenge(Account user, String mfaType) {
        String challenge = IdUtil.simpleUUID();
        SystemAccountSecurityProperties.Mfa.Email mfaConfig = accountSecurityProperties.getMfa().getEmail();
        AccountSecurity security = ensureUserSecurity(user);
        verificationCodeStore.saveMfaChallenge(challenge, user.getId(), mfaConfig.getChallengeTtlSeconds());
        TokenDTO dto = new TokenDTO();
        dto.setMfaChallenge(challenge);
        dto.setMfaType(mfaType);
        if ("email".equals(mfaType)) {
            dto.setMfaEmailMasked(maskEmail(security.getEmail()));
        } else if ("totp".equals(mfaType)) {
            dto.setMfaTotpDigits(6);
        }
        return dto;
    }

    private String maskEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return "";
        }
        int at = email.indexOf('@');
        if (at <= 0) {
            return email;
        }
        String local = email.substring(0, at);
        String domain = email.substring(at);
        if (local.length() <= 2) {
            return local.charAt(0) + "***" + domain;
        }
        return local.charAt(0) + "***" + local.charAt(local.length() - 1) + domain;
    }

    private AccountSecurity ensureUserSecurity(Account user) {
        AccountSecurity security = userSecurityMapper.selectOne(new LambdaQueryWrapper<AccountSecurity>()
                .eq(AccountSecurity::getAccountId, user.getId())
                .eq(AccountSecurity::getDeleted, 0)
                .last("limit 1"));
        if (security != null) {
            return security;
        }
        AccountSecurity created = new AccountSecurity();
        created.setId(entityIdGenerator.nextId(AccountSecurity.class));
        created.setAccountId(user.getId());
        created.setLoginMethods(String.join(",",
                accountSecurityProperties.getLoginMethods().getDefaults()));
        created.setMfaEmailEnabled(0);
        created.setMfaTotpEnabled(0);
        created.setDeleted(0);
        userSecurityMapper.insert(created);
        return created;
    }

    private boolean isPendingDeletion(Account user) {
        if (user == null || !Integer.valueOf(AccountStatusEnum.CANCELLED.getCode()).equals(user.getStatus())) {
            return false;
        }
        LocalDateTime expiresAt = user.getDeletionExpiresAt();
        return expiresAt != null && expiresAt.isAfter(LocalDateTime.now(ZoneOffset.UTC));
    }

    private String formatTime(LocalDateTime value) {
        return value == null ? "" : value.toString();
    }

    private String buildAvatar(Profile profile, String username) {
        if (profile != null && StringUtils.hasText(profile.getAvatar())) {
            String avatar = profile.getAvatar();
            if (avatar.startsWith("http://") || avatar.startsWith("https://") || avatar.startsWith("/")) {
                return avatar;
            }
            return "/api/file/db/" + avatar;
        }
        String seed = StringUtils.hasText(username) ? username.trim() : "user";
        return "https://api.dicebear.com/7.x/avataaars/svg?seed=" + seed;
    }

    private void ensureLoginMethodAllowed(AccountSecurity security, String method) {
        // 全局启用的登录方式以访问控制配置为准（取代 yml 全局配置）。
        List<String> enabled = accessControlService.getEnabledLoginMethods();
        if (enabled == null || !enabled.contains(method)) {
            throw new ApiException(BusinessErrorCode.LOGIN_METHOD_DISABLED);
        }
        // 管理员针对该账号单独禁用：区别于“全局未开放”，返回专属错误码以提示“被管理员禁用，暂时无法使用”。
        List<String> disabled = parseLoginMethods(security.getDisabledLoginMethods());
        if (disabled.contains(method)) {
            throw new ApiException(BusinessErrorCode.LOGIN_METHOD_DISABLED_BY_ADMIN);
        }
        List<String> stored = parseLoginMethods(security.getLoginMethods());
        if (stored.isEmpty()) {
            stored = accountSecurityProperties.getLoginMethods().getDefaults();
        }
        if (!stored.contains(method)) {
            throw new ApiException(BusinessErrorCode.LOGIN_METHOD_DISABLED);
        }
    }

    private List<String> parseLoginMethods(String methods) {
        if (!StringUtils.hasText(methods)) {
            return new ArrayList<>();
        }
        return Arrays.stream(methods.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
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

    private void checkLoginLock(Account user) {
        if (user.getLoginFailTime() == null) {
            return;
        }
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (user.getLoginFailTime().isAfter(now)) {
            throw new ApiException(BusinessErrorCode.ACCOUNT_LOCKED);
        }
        user.setLoginFailCount(0);
        user.setLoginFailTime(null);
        userMapper.updateById(user);
    }

    private void increaseLoginFailCount(Account user) {
        int failCount = user.getLoginFailCount() == null ? 0 : user.getLoginFailCount();
        user.setLoginFailCount(failCount + 1);

        if (failCount + 1 >= authProperties.getLogin().getMaxFailCount()) {
            user.setLoginFailTime(LocalDateTime.now(ZoneOffset.UTC)
                    .plusMinutes(authProperties.getLogin().getLockMinutes()));
        }

        userMapper.updateById(user);
    }

    private void resetLoginFailCount(Account user) {
        if (user.getLoginFailCount() != null && user.getLoginFailCount() > 0) {
            user.setLoginFailCount(0);
            user.setLoginFailTime(null);
            userMapper.updateById(user);
        }
    }

    private void upgradePasswordIfNeeded(Account user, String rawPassword) {
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

    private Account findUserByEmail(String email) {
        AccountSecurity security = userSecurityMapper.selectOne(
                new LambdaQueryWrapper<AccountSecurity>()
                        .eq(AccountSecurity::getDeleted, 0)
                        .eq(AccountSecurity::getEmail, email)
                        .last("limit 1")
        );
        if (security == null || !StringUtils.hasText(security.getAccountId())) {
            return null;
        }
        return userMapper.selectOne(
                new LambdaQueryWrapper<Account>()
                        .eq(Account::getDeleted, 0)
                        .eq(Account::getId, security.getAccountId())
                        .last("limit 1")
        );
    }

    /**
     * 登录支持账号、邮箱二选一匹配。
     */
    private Account findUserByAccount(String account) {
        String trimmed = account.trim();

        if (PHONE_PATTERN.matcher(trimmed).matches()) {
            return null;
        }

        Account user = userMapper.selectOne(
                new LambdaQueryWrapper<Account>()
                        .eq(Account::getDeleted, 0)
                        .eq(Account::getUsername, trimmed)
                        .last("limit 1")
        );
        if (user != null) {
            return user;
        }

        String lower = trimmed.toLowerCase();
        if (EMAIL_PATTERN.matcher(lower).matches()) {
            user = findUserByEmail(lower);
        }
        return user;
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim().toLowerCase();
    }

    private EmailBuilder requireEmailBuilder() {
        EmailBuilder emailBuilder = emailBuilderProvider.getIfAvailable();
        if (emailBuilder == null) {
            throw new ApiException(BusinessErrorCode.EMAIL_SERVICE_DISABLED);
        }
        return emailBuilder;
    }
}
