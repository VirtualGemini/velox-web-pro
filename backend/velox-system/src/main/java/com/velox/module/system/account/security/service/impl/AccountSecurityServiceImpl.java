package com.velox.module.system.account.security.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.velox.module.system.auth.support.SecureCodeGenerator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.velox.common.exception.ApiException;
import com.velox.common.exception.BusinessErrorCode;
import com.velox.email.api.builder.EmailBuilder;
import com.velox.email.api.message.SendResponse;
import com.velox.email.common.error.EmailErrorCode;
import com.velox.framework.security.api.session.SecuritySessionService;
import com.velox.module.system.accesscontrol.service.AccessControlService;
import com.velox.module.system.auth.properties.SystemAccountSecurityProperties;
import com.velox.module.system.auth.service.PasswordCipherService;
import com.velox.module.system.auth.store.VerificationCodeStore;
import com.velox.module.system.domain.model.Account;
import com.velox.module.system.domain.model.AccountSecurity;
import com.velox.module.system.id.generator.SystemEntityIdGenerator;
import com.velox.module.system.mail.service.MailTemplateRenderService;
import com.velox.module.system.mail.template.MailTemplateType;
import com.velox.module.system.mail.template.RecipientLanguageResolver;
import com.velox.module.system.mail.template.RenderedEmail;
import com.velox.module.system.persistence.AccountMapper;
import com.velox.module.system.persistence.AccountSecurityMapper;
import com.velox.module.system.account.security.dto.EmailRebindCommand;
import com.velox.module.system.account.security.dto.EmailRebindProofDTO;
import com.velox.module.system.account.security.dto.EmailRebindProofVerifyCommand;
import com.velox.module.system.account.security.dto.EmailRebindSendCodeCommand;
import com.velox.module.system.account.security.dto.EmailUnbindCommand;
import com.velox.module.system.account.security.dto.LoginMethodsUpdateCommand;
import com.velox.module.system.account.security.dto.MfaEmailUpdateCommand;
import com.velox.module.system.account.security.dto.MfaTotpDisableCommand;
import com.velox.module.system.account.security.dto.MfaTotpEnableCommand;
import com.velox.module.system.account.security.dto.MfaTotpEnableResultDTO;
import com.velox.module.system.account.security.dto.MfaTotpProvisionDTO;
import com.velox.module.system.account.security.dto.SecurityStatusDTO;
import com.velox.module.system.account.security.service.AccountSecurityService;
import com.velox.framework.totp.api.model.TotpProvisioning;
import com.velox.framework.totp.api.model.TotpVerifyResult;
import com.velox.framework.totp.api.service.TotpService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AccountSecurityServiceImpl implements AccountSecurityService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final String REBIND_SCENE = "email_rebind";
    private static final String REBIND_PROOF_SCOPE = "email-proof";
    private static final String REBIND_PROOF_TYPE_CURRENT_EMAIL_CODE = "current_email_code";
    private static final String REBIND_PROOF_TYPE_TOTP = "totp";
    private static final String REBIND_PROOF_TYPE_PASSWORD = "password";
    private static final int TOTP_RECOVERY_CODE_COUNT = 12;
    private static final Pattern TOTP_CODE_PATTERN = Pattern.compile("^\\d{6,8}$");
    private static final Pattern TOTP_RECOVERY_CODE_PATTERN = Pattern.compile("^[A-Z0-9]{4}-[A-Z0-9]{4}$");

    private final AccountMapper userMapper;
    private final AccountSecurityMapper userSecurityMapper;
    private final SecuritySessionService securitySessionService;
    private final SystemAccountSecurityProperties accountSecurityProperties;
    private final AccessControlService accessControlService;
    private final SystemEntityIdGenerator entityIdGenerator;
    private final VerificationCodeStore verificationCodeStore;
    private final ObjectProvider<EmailBuilder> emailBuilderProvider;
    private final PasswordCipherService passwordCipherService;
    private final TotpService totpService;
    private final MailTemplateRenderService mailTemplateRenderService;
    private final RecipientLanguageResolver recipientLanguageResolver;

    public AccountSecurityServiceImpl(
            AccountMapper userMapper,
            AccountSecurityMapper userSecurityMapper,
            SecuritySessionService securitySessionService,
            SystemAccountSecurityProperties accountSecurityProperties,
            AccessControlService accessControlService,
            SystemEntityIdGenerator entityIdGenerator,
            VerificationCodeStore verificationCodeStore,
            ObjectProvider<EmailBuilder> emailBuilderProvider,
            PasswordCipherService passwordCipherService,
            TotpService totpService,
            MailTemplateRenderService mailTemplateRenderService,
            RecipientLanguageResolver recipientLanguageResolver) {
        this.userMapper = userMapper;
        this.userSecurityMapper = userSecurityMapper;
        this.securitySessionService = securitySessionService;
        this.accountSecurityProperties = accountSecurityProperties;
        this.accessControlService = accessControlService;
        this.entityIdGenerator = entityIdGenerator;
        this.verificationCodeStore = verificationCodeStore;
        this.emailBuilderProvider = emailBuilderProvider;
        this.passwordCipherService = passwordCipherService;
        this.totpService = totpService;
        this.mailTemplateRenderService = mailTemplateRenderService;
        this.recipientLanguageResolver = recipientLanguageResolver;
    }

    @Override
    public SecurityStatusDTO getStatus() {
        String userId = securitySessionService.requireCurrentLoginId();
        Account user = requireUser(userId);
        AccountSecurity security = getOrInitSecurity(user);

        // “全局策略”一律以访问控制配置为准（与登录校验 LoginServiceImpl.ensureLoginMethodAllowed 一致），
        // allowedLoginMethods = 系统支持的方式 ∩ 访问控制全局启用的方式；只有这里才叫“全局”。
        List<String> supported = accountSecurityProperties.getLoginMethods().getEnabled();
        List<String> globalEnabled = accessControlService.getEnabledLoginMethods();
        List<String> allowed = supported.stream()
                .filter(globalEnabled::contains)
                .collect(Collectors.toList());

        // 管理员针对该账号单独禁用的方式（账号管理），与“全局未开放”是两回事，单独返回供前端区分提示。
        List<String> adminDisabled = parseLoginMethods(security.getDisabledLoginMethods());

        List<String> stored = parseLoginMethods(security.getLoginMethods());
        if (stored.isEmpty()) {
            stored = new ArrayList<>(accountSecurityProperties.getLoginMethods().getDefaults());
        }
        // 实际可用 = 用户已选 ∩ 全局允许 − 管理员禁用。
        List<String> effective = stored.stream()
                .filter(allowed::contains)
                .filter(method -> !adminDisabled.contains(method))
                .collect(Collectors.toList());

        SecurityStatusDTO dto = new SecurityStatusDTO();
        dto.setEmail(security.getEmail());
        dto.setEmailMasked(maskEmail(security.getEmail()));
        dto.setLoginMethods(stored);
        dto.setEffectiveLoginMethods(effective);
        dto.setAllowedLoginMethods(allowed);
        dto.setAdminDisabledLoginMethods(adminDisabled);
        dto.setPasswordRequired(false);

        SecurityStatusDTO.MfaStatus mfa = new SecurityStatusDTO.MfaStatus();
        mfa.setEmail(Integer.valueOf(1).equals(security.getMfaEmailEnabled()));
        mfa.setTotp(Integer.valueOf(1).equals(security.getMfaTotpEnabled()));
        dto.setMfa(mfa);

        dto.setEmailVerifiedAt(formatTime(security.getEmailVerifiedAt()));
        dto.setLastPasswordChangeAt(formatTime(security.getLastPasswordChangeAt()));
        return dto;
    }

    @Override
    public void sendEmailUnbindCode() {
        String userId = securitySessionService.requireCurrentLoginId();
        Account user = requireUser(userId);
        AccountSecurity security = getOrInitSecurity(user);
        String currentEmail = normalizeEmail(security.getEmail());
        if (currentEmail == null || !EMAIL_PATTERN.matcher(currentEmail).matches()) {
            throw new ApiException(BusinessErrorCode.EMAIL_NOT_BOUND_TO_USER);
        }

        EmailBuilder emailBuilder = requireEmailBuilder();
        SystemAccountSecurityProperties.Rebind.Email rebindConfig = accountSecurityProperties.getRebind().getEmail();
        String code = SecureCodeGenerator.numeric(6);
        if (!verificationCodeStore.trySaveRebindCode(
                REBIND_PROOF_SCOPE,
                currentEmail,
                code,
                rebindConfig.getCodeTtlSeconds(),
                rebindConfig.getResendIntervalSeconds())) {
            throw new ApiException(BusinessErrorCode.REBIND_CODE_SEND_TOO_FREQUENT);
        }
        try {
            RenderedEmail mail = mailTemplateRenderService.render(
                    MailTemplateType.ACCOUNT_EMAIL_UNBIND_CODE,
                    recipientLanguageResolver.resolve(userId),
                    Map.of("username", user.getUsername(), "code", code,
                            "validityMinutes", "10", "appName", "Velox"));
            SendResponse response = emailBuilder.to(currentEmail)
                    .subject(mail.subject())
                    .html(mail.html())
                    .sendSync();
            if (!response.success()) {
                verificationCodeStore.invalidateRebindCode(REBIND_PROOF_SCOPE, currentEmail);
                if (response.errorCode() == EmailErrorCode.DISABLED.code()) {
                    throw new ApiException(BusinessErrorCode.EMAIL_SERVICE_DISABLED);
                }
                throw new ApiException(BusinessErrorCode.EMAIL_SEND_FAILED);
            }
        } catch (ApiException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            verificationCodeStore.invalidateRebindCode(REBIND_PROOF_SCOPE, currentEmail);
            throw new ApiException(ex, BusinessErrorCode.EMAIL_SEND_FAILED);
        }
    }

    @Override
    public void sendEmailRebindProofCode() {
        String userId = securitySessionService.requireCurrentLoginId();
        Account user = requireUser(userId);
        AccountSecurity security = getOrInitSecurity(user);
        String currentEmail = normalizeEmail(security.getEmail());
        if (currentEmail == null || !EMAIL_PATTERN.matcher(currentEmail).matches()) {
            throw new ApiException(BusinessErrorCode.EMAIL_NOT_BOUND_TO_USER);
        }

        EmailBuilder emailBuilder = requireEmailBuilder();
        SystemAccountSecurityProperties.Rebind.Email rebindConfig = accountSecurityProperties.getRebind().getEmail();
        String code = SecureCodeGenerator.numeric(6);
        if (!verificationCodeStore.trySaveRebindCode(
                REBIND_PROOF_SCOPE,
                currentEmail,
                code,
                rebindConfig.getCodeTtlSeconds(),
                rebindConfig.getResendIntervalSeconds())) {
            throw new ApiException(BusinessErrorCode.REBIND_CODE_SEND_TOO_FREQUENT);
        }
        try {
            RenderedEmail mail = mailTemplateRenderService.render(
                    MailTemplateType.ACCOUNT_EMAIL_REBIND_PROOF_CODE,
                    recipientLanguageResolver.resolve(userId),
                    Map.of("username", user.getUsername(), "code", code,
                            "validityMinutes", "10", "appName", "Velox"));
            SendResponse response = emailBuilder.to(currentEmail)
                    .subject(mail.subject())
                    .html(mail.html())
                    .sendSync();
            if (!response.success()) {
                verificationCodeStore.invalidateRebindCode(REBIND_PROOF_SCOPE, currentEmail);
                if (response.errorCode() == EmailErrorCode.DISABLED.code()) {
                    throw new ApiException(BusinessErrorCode.EMAIL_SERVICE_DISABLED);
                }
                throw new ApiException(BusinessErrorCode.EMAIL_SEND_FAILED);
            }
        } catch (ApiException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            verificationCodeStore.invalidateRebindCode(REBIND_PROOF_SCOPE, currentEmail);
            throw new ApiException(ex, BusinessErrorCode.EMAIL_SEND_FAILED);
        }
    }

    @Override
    public EmailRebindProofDTO verifyEmailRebindProof(EmailRebindProofVerifyCommand command) {
        String userId = securitySessionService.requireCurrentLoginId();
        Account user = requireUser(userId);
        AccountSecurity security = getOrInitSecurity(user);
        String expectedProofType = resolveRebindProofType(user, security);
        String actualProofType = normalizeProofType(command.getProofType());
        if (!expectedProofType.equals(actualProofType)) {
            throw new ApiException(BusinessErrorCode.REBIND_PROOF_TYPE_MISMATCH);
        }

        switch (expectedProofType) {
            case REBIND_PROOF_TYPE_CURRENT_EMAIL_CODE -> verifyCurrentEmailProof(user, command.getCurrentEmailCode());
            case REBIND_PROOF_TYPE_TOTP -> verifyTotpProof(security, command.getTotpCode());
            case REBIND_PROOF_TYPE_PASSWORD -> verifyPasswordProof(user, command.getCurrentPassword());
            default -> throw new ApiException(BusinessErrorCode.REBIND_PROOF_TYPE_MISMATCH);
        }

        int proofTtlSeconds = accountSecurityProperties.getMfa().getEmail().getChallengeTtlSeconds();
        String proofTicket = UUID.randomUUID().toString().replace("-", "");
        verificationCodeStore.saveProofTicket(REBIND_SCENE, proofTicket, userId, proofTtlSeconds);

        EmailRebindProofDTO dto = new EmailRebindProofDTO();
        dto.setProofTicket(proofTicket);
        dto.setExpiresInSeconds(proofTtlSeconds);
        return dto;
    }

    @Override
    public void sendEmailRebindCode(EmailRebindSendCodeCommand command) {
        String userId = securitySessionService.requireCurrentLoginId();
        Account user = requireUser(userId);
        requireRebindProof(userId, command.getProofTicket());
        String newEmail = normalizeEmail(command.getNewEmail());
        if (newEmail == null || !EMAIL_PATTERN.matcher(newEmail).matches()) {
            throw new ApiException(BusinessErrorCode.EMAIL_REQUIRED);
        }
        AccountSecurity security = getOrInitSecurity(user);
        String currentEmail = normalizeEmail(security.getEmail());
        if (newEmail.equalsIgnoreCase(currentEmail)) {
            throw new ApiException(BusinessErrorCode.EMAIL_SAME_AS_CURRENT);
        }
        AccountSecurity existing = userSecurityMapper.selectOne(new LambdaQueryWrapper<AccountSecurity>()
                .eq(AccountSecurity::getDeleted, 0)
                .eq(AccountSecurity::getEmail, newEmail)
                .last("limit 1"));
        if (existing != null && !existing.getId().equals(user.getId())) {
            throw new ApiException(BusinessErrorCode.EMAIL_ALREADY_IN_USE);
        }

        EmailBuilder emailBuilder = requireEmailBuilder();
        SystemAccountSecurityProperties.Rebind.Email rebindConfig = accountSecurityProperties.getRebind().getEmail();
        String code = SecureCodeGenerator.numeric(6);
        if (!verificationCodeStore.trySaveRebindCode("email", newEmail, code,
                rebindConfig.getCodeTtlSeconds(), rebindConfig.getResendIntervalSeconds())) {
            throw new ApiException(BusinessErrorCode.REBIND_CODE_SEND_TOO_FREQUENT);
        }
        try {
            RenderedEmail mail = mailTemplateRenderService.render(
                    MailTemplateType.ACCOUNT_EMAIL_REBIND_CODE,
                    recipientLanguageResolver.resolve(userId),
                    Map.of("username", user.getUsername(), "code", code,
                            "validityMinutes", "10", "appName", "Velox"));
            SendResponse response = emailBuilder.to(newEmail)
                    .subject(mail.subject())
                    .html(mail.html())
                    .sendSync();
            if (!response.success()) {
                verificationCodeStore.invalidateRebindCode("email", newEmail);
                if (response.errorCode() == EmailErrorCode.DISABLED.code()) {
                    throw new ApiException(BusinessErrorCode.EMAIL_SERVICE_DISABLED);
                }
                throw new ApiException(BusinessErrorCode.EMAIL_SEND_FAILED);
            }
        } catch (ApiException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            verificationCodeStore.invalidateRebindCode("email", newEmail);
            throw new ApiException(ex, BusinessErrorCode.EMAIL_SEND_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean rebindEmail(EmailRebindCommand command) {
        String userId = securitySessionService.requireCurrentLoginId();
        Account user = requireUser(userId);
        requireRebindProof(userId, command.getProofTicket());
        String newEmail = normalizeEmail(command.getNewEmail());
        if (newEmail == null || !EMAIL_PATTERN.matcher(newEmail).matches()) {
            throw new ApiException(BusinessErrorCode.EMAIL_REQUIRED);
        }
        AccountSecurity security = getOrInitSecurity(user);
        String currentEmail = normalizeEmail(security.getEmail());
        if (newEmail.equalsIgnoreCase(currentEmail)) {
            throw new ApiException(BusinessErrorCode.EMAIL_SAME_AS_CURRENT);
        }
        AccountSecurity existing = userSecurityMapper.selectOne(new LambdaQueryWrapper<AccountSecurity>()
                .eq(AccountSecurity::getDeleted, 0)
                .eq(AccountSecurity::getEmail, newEmail)
                .last("limit 1"));
        if (existing != null && !existing.getId().equals(user.getId())) {
            throw new ApiException(BusinessErrorCode.EMAIL_ALREADY_IN_USE);
        }

        VerificationCodeStore.VerificationResult result =
                verificationCodeStore.verifyRebindCode("email", newEmail, command.getNewEmailCode());
        if (result == VerificationCodeStore.VerificationResult.EXPIRED) {
            throw new ApiException(BusinessErrorCode.REBIND_CODE_EXPIRED);
        }
        if (result == VerificationCodeStore.VerificationResult.TOO_MANY_ATTEMPTS) {
            throw new ApiException(BusinessErrorCode.VERIFY_CODE_TOO_MANY_ATTEMPTS);
        }
        if (result == VerificationCodeStore.VerificationResult.INVALID) {
            throw new ApiException(BusinessErrorCode.REBIND_CODE_ERROR);
        }
        consumeRebindProof(userId, command.getProofTicket());

        security.setEmail(newEmail);
        security.setEmailVerifiedAt(LocalDateTime.now(ZoneOffset.UTC));
        security.setUpdateBy(userId);
        saveSecurity(security);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean unbindEmail(EmailUnbindCommand command) {
        String userId = securitySessionService.requireCurrentLoginId();
        Account user = requireUser(userId);
        AccountSecurity security = getOrInitSecurity(user);
        String currentEmail = normalizeEmail(security.getEmail());
        if (currentEmail == null || !EMAIL_PATTERN.matcher(currentEmail).matches()) {
            throw new ApiException(BusinessErrorCode.EMAIL_NOT_BOUND_TO_USER);
        }

        VerificationCodeStore.VerificationResult emailCodeResult =
                verificationCodeStore.verifyRebindCode(REBIND_PROOF_SCOPE, currentEmail, command.getCurrentEmailCode());
        if (emailCodeResult == VerificationCodeStore.VerificationResult.EXPIRED) {
            throw new ApiException(BusinessErrorCode.REBIND_CURRENT_EMAIL_CODE_EXPIRED);
        }
        if (emailCodeResult == VerificationCodeStore.VerificationResult.TOO_MANY_ATTEMPTS) {
            throw new ApiException(BusinessErrorCode.VERIFY_CODE_TOO_MANY_ATTEMPTS);
        }
        if (emailCodeResult == VerificationCodeStore.VerificationResult.INVALID) {
            throw new ApiException(BusinessErrorCode.REBIND_CURRENT_EMAIL_CODE_ERROR);
        }

        if (Integer.valueOf(1).equals(security.getMfaTotpEnabled())) {
            if (!StringUtils.hasText(command.getTotpCode())) {
                throw new ApiException(BusinessErrorCode.EMAIL_UNBIND_TOTP_REQUIRED);
            }
            verifyTotpProof(security, command.getTotpCode());
        }

        security.setEmail(null);
        security.setEmailVerifiedAt(null);
        security.setMfaEmailEnabled(0);
        security.setUpdateBy(userId);
        saveSecurity(security);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateLoginMethods(LoginMethodsUpdateCommand command) {
        String userId = securitySessionService.requireCurrentLoginId();
        Account user = requireUser(userId);

        if (command.getMethods() == null || command.getMethods().isEmpty()) {
            throw new ApiException(BusinessErrorCode.LOGIN_METHOD_EMPTY);
        }
        Set<String> dedup = new LinkedHashSet<>();
        for (String method : command.getMethods()) {
            if (StringUtils.hasText(method)) {
                dedup.add(method.trim().toLowerCase());
            }
        }
        if (dedup.isEmpty()) {
            throw new ApiException(BusinessErrorCode.LOGIN_METHOD_EMPTY);
        }

        SystemAccountSecurityProperties.LoginMethods config = accountSecurityProperties.getLoginMethods();
        List<String> enabled = config.getEnabled();
        for (String method : dedup) {
            if (!enabled.contains(method)) {
                throw new ApiException(BusinessErrorCode.LOGIN_METHOD_NOT_ALLOWED);
            }
        }
        AccountSecurity security = getOrInitSecurity(user);
        List<String> disabled = parseLoginMethods(security.getDisabledLoginMethods());
        for (String method : dedup) {
            if (disabled.contains(method)) {
                throw new ApiException(BusinessErrorCode.LOGIN_METHOD_NOT_ALLOWED);
            }
        }
        security.setLoginMethods(String.join(",", dedup));
        security.setUpdateBy(userId);
        saveSecurity(security);
        return true;
    }

    @Override
    public void sendMfaEmailCode() {
        String userId = securitySessionService.requireCurrentLoginId();
        Account user = requireUser(userId);
        AccountSecurity security = getOrInitSecurity(user);
        String email = normalizeEmail(security.getEmail());
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new ApiException(BusinessErrorCode.EMAIL_NOT_BOUND_TO_USER);
        }

        EmailBuilder emailBuilder = requireEmailBuilder();
        SystemAccountSecurityProperties.Mfa.Email mfaConfig = accountSecurityProperties.getMfa().getEmail();
        String code = SecureCodeGenerator.numeric(6);
        if (!verificationCodeStore.trySaveMfaCode(userId, code,
                mfaConfig.getTtlSeconds(), mfaConfig.getResendIntervalSeconds())) {
            throw new ApiException(BusinessErrorCode.MFA_CODE_SEND_TOO_FREQUENT);
        }
        try {
            RenderedEmail mail = mailTemplateRenderService.render(
                    MailTemplateType.ACCOUNT_MFA_CODE,
                    recipientLanguageResolver.resolve(userId),
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
        } catch (ApiException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            verificationCodeStore.invalidateMfaCode(userId);
            throw new ApiException(ex, BusinessErrorCode.EMAIL_SEND_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateMfaEmail(MfaEmailUpdateCommand command) {
        String userId = securitySessionService.requireCurrentLoginId();
        Account user = requireUser(userId);
        Boolean enabled = command.getEnabled();
        if (enabled == null) {
            throw new ApiException(BusinessErrorCode.BUSINESS_ERROR);
        }

        AccountSecurity security = getOrInitSecurity(user);
        if (Boolean.TRUE.equals(enabled)) {
            if (Integer.valueOf(1).equals(security.getMfaTotpEnabled())) {
                throw new ApiException(BusinessErrorCode.MFA_ALREADY_ENABLED);
            }
            String email = normalizeEmail(security.getEmail());
            if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
                throw new ApiException(BusinessErrorCode.EMAIL_NOT_BOUND_TO_USER);
            }
            if (!StringUtils.hasText(command.getCode())) {
                throw new ApiException(BusinessErrorCode.MFA_CODE_ERROR);
            }
            VerificationCodeStore.VerificationResult result =
                    verificationCodeStore.verifyMfaCode(userId, command.getCode());
            if (result == VerificationCodeStore.VerificationResult.EXPIRED) {
                throw new ApiException(BusinessErrorCode.MFA_CODE_EXPIRED);
            }
            if (result == VerificationCodeStore.VerificationResult.TOO_MANY_ATTEMPTS) {
                throw new ApiException(BusinessErrorCode.VERIFY_CODE_TOO_MANY_ATTEMPTS);
            }
            if (result == VerificationCodeStore.VerificationResult.INVALID) {
                throw new ApiException(BusinessErrorCode.MFA_CODE_ERROR);
            }
            security.setMfaEmailEnabled(1);
        } else {
            security.setMfaEmailEnabled(0);
        }
        security.setUpdateBy(userId);
        saveSecurity(security);
        return true;
    }

    @Override
    public MfaTotpProvisionDTO provisionMfaTotp() {
        if (!totpService.isEnabled()) {
            throw new ApiException(BusinessErrorCode.MFA_TOTP_SERVICE_DISABLED);
        }
        String userId = securitySessionService.requireCurrentLoginId();
        Account user = requireUser(userId);
        AccountSecurity security = getOrInitSecurity(user);
        if (Integer.valueOf(1).equals(security.getMfaEmailEnabled())) {
            throw new ApiException(BusinessErrorCode.MFA_ALREADY_ENABLED);
        }

        String accountName = StringUtils.hasText(security.getEmail()) ? security.getEmail() : user.getUsername();
        TotpProvisioning provisioning = totpService.provision(accountName);
        MfaTotpProvisionDTO dto = new MfaTotpProvisionDTO();
        dto.setSecret(provisioning.secret().base32());
        dto.setOtpAuthUri(provisioning.otpAuthUri());
        dto.setIssuer(provisioning.issuer());
        dto.setAccountName(provisioning.accountName());
        dto.setDigits(provisioning.secret().digits());
        dto.setPeriodSeconds(provisioning.secret().periodSeconds());
        dto.setAlgorithm(provisioning.secret().algorithm().name());
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MfaTotpEnableResultDTO enableMfaTotp(MfaTotpEnableCommand command) {
        if (!totpService.isEnabled()) {
            throw new ApiException(BusinessErrorCode.MFA_TOTP_SERVICE_DISABLED);
        }
        String userId = securitySessionService.requireCurrentLoginId();
        Account user = requireUser(userId);
        AccountSecurity security = getOrInitSecurity(user);
        if (Integer.valueOf(1).equals(security.getMfaEmailEnabled())) {
            throw new ApiException(BusinessErrorCode.MFA_ALREADY_ENABLED);
        }

        TotpVerifyResult result = totpService.verify(command.getSecret(), command.getCode());
        if (!result.matched()) {
            throw new ApiException(BusinessErrorCode.MFA_TOTP_CODE_ERROR);
        }

        List<String> recoveryCodes = generateRecoveryCodes();
        security.setMfaTotpEnabled(1);
        security.setMfaTotpSecret(command.getSecret());
        security.setMfaTotpRecoveryCodes(serializeRecoveryCodeHashes(hashRecoveryCodes(recoveryCodes)));
        security.setUpdateBy(userId);
        saveSecurity(security);
        MfaTotpEnableResultDTO dto = new MfaTotpEnableResultDTO();
        dto.setRecoveryCodes(recoveryCodes);
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean disableMfaTotp(MfaTotpDisableCommand command) {
        String userId = securitySessionService.requireCurrentLoginId();
        Account user = requireUser(userId);
        AccountSecurity security = getOrInitSecurity(user);
        if (!Integer.valueOf(1).equals(security.getMfaTotpEnabled())) {
            throw new ApiException(BusinessErrorCode.MFA_TOTP_NOT_ENABLED);
        }
        if (!StringUtils.hasText(security.getMfaTotpSecret())) {
            throw new ApiException(BusinessErrorCode.MFA_TOTP_NOT_PROVISIONED);
        }
        String code = normalizeTotpCode(command.getCode());
        String recoveryCode = normalizeRecoveryCode(command.getRecoveryCode());
        if (!StringUtils.hasText(code) && !StringUtils.hasText(recoveryCode)) {
            throw new ApiException(BusinessErrorCode.MFA_PROOF_REQUIRED);
        }

        if (StringUtils.hasText(code)) {
            if (!totpService.isEnabled()) {
                throw new ApiException(BusinessErrorCode.MFA_TOTP_SERVICE_DISABLED);
            }
            TotpVerifyResult result = totpService.verify(security.getMfaTotpSecret(), code);
            if (!result.matched()) {
                throw new ApiException(BusinessErrorCode.MFA_TOTP_CODE_ERROR);
            }
        } else if (!consumeRecoveryCode(security, recoveryCode)) {
            throw new ApiException(BusinessErrorCode.MFA_TOTP_RECOVERY_CODE_ERROR);
        }

        security.setMfaTotpEnabled(0);
        security.setMfaTotpSecret(null);
        security.setMfaTotpRecoveryCodes(null);
        security.setUpdateBy(userId);
        saveSecurity(security);
        return true;
    }

    private Account requireUser(String userId) {
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
        AccountSecurity security = userSecurityMapper.selectOne(new LambdaQueryWrapper<AccountSecurity>()
                .eq(AccountSecurity::getAccountId, user.getId())
                .eq(AccountSecurity::getDeleted, 0)
                .last("limit 1"));
        if (security != null) {
            return security;
        }
        SystemAccountSecurityProperties.LoginMethods config = accountSecurityProperties.getLoginMethods();
        AccountSecurity created = new AccountSecurity();
        created.setId(entityIdGenerator.nextId(AccountSecurity.class));
        created.setAccountId(user.getId());
        created.setLoginMethods(String.join(",", config.getDefaults()));
        created.setMfaEmailEnabled(0);
        created.setMfaTotpEnabled(0);
        created.setMfaTotpRecoveryCodes(null);
        created.setDeleted(0);
        return created;
    }

    private List<String> generateRecoveryCodes() {
        List<String> recoveryCodes = new ArrayList<>(TOTP_RECOVERY_CODE_COUNT);
        for (int i = 0; i < TOTP_RECOVERY_CODE_COUNT; i++) {
            recoveryCodes.add(SecureCodeGenerator.upperAlphaNumeric(4) + "-" + SecureCodeGenerator.upperAlphaNumeric(4));
        }
        return recoveryCodes;
    }

    private List<String> hashRecoveryCodes(List<String> recoveryCodes) {
        return recoveryCodes.stream()
                .map(this::hashRecoveryCode)
                .collect(Collectors.toList());
    }

    private String hashRecoveryCode(String recoveryCode) {
        return DigestUtil.sha256Hex(recoveryCode);
    }

    private String serializeRecoveryCodeHashes(List<String> hashes) {
        return String.join(",", hashes);
    }

    private List<String> parseRecoveryCodeHashes(String raw) {
        if (!StringUtils.hasText(raw)) {
            return new ArrayList<>();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private boolean consumeRecoveryCode(AccountSecurity security, String recoveryCode) {
        if (!StringUtils.hasText(recoveryCode)) {
            return false;
        }
        String hashedCode = hashRecoveryCode(recoveryCode);
        List<String> hashes = parseRecoveryCodeHashes(security.getMfaTotpRecoveryCodes());
        boolean removed = hashes.remove(hashedCode);
        if (removed) {
            security.setMfaTotpRecoveryCodes(serializeRecoveryCodeHashes(hashes));
        }
        return removed;
    }

    private String normalizeTotpCode(String code) {
        if (!StringUtils.hasText(code)) {
            return null;
        }
        String normalized = code.trim();
        if (!TOTP_CODE_PATTERN.matcher(normalized).matches()) {
            throw new ApiException(BusinessErrorCode.MFA_TOTP_CODE_ERROR);
        }
        return normalized;
    }

    private String normalizeRecoveryCode(String recoveryCode) {
        if (!StringUtils.hasText(recoveryCode)) {
            return null;
        }
        String normalized = recoveryCode.trim().toUpperCase(Locale.ROOT);
        if (!TOTP_RECOVERY_CODE_PATTERN.matcher(normalized).matches()) {
            throw new ApiException(BusinessErrorCode.MFA_TOTP_RECOVERY_CODE_ERROR);
        }
        return normalized;
    }

    private void saveSecurity(AccountSecurity security) {
        security.setDeleted(0);
        if (userSecurityMapper.selectById(security.getId()) == null) {
            userSecurityMapper.insert(security);
            return;
        }
        userSecurityMapper.updateById(security);
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

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim().toLowerCase();
    }

    private String normalizeProofType(String proofType) {
        if (!StringUtils.hasText(proofType)) {
            return "";
        }
        return proofType.trim().toLowerCase(Locale.ROOT);
    }

    private String resolveRebindProofType(Account user, AccountSecurity security) {
        AccountSecurity currentSecurity = security == null ? getOrInitSecurity(user) : security;
        if (StringUtils.hasText(normalizeEmail(currentSecurity.getEmail()))) {
            return REBIND_PROOF_TYPE_CURRENT_EMAIL_CODE;
        }
        if (Integer.valueOf(1).equals(currentSecurity.getMfaTotpEnabled())) {
            return REBIND_PROOF_TYPE_TOTP;
        }
        return REBIND_PROOF_TYPE_PASSWORD;
    }

    private void verifyCurrentEmailProof(Account user, String currentEmailCode) {
        AccountSecurity security = getOrInitSecurity(user);
        String currentEmail = normalizeEmail(security.getEmail());
        if (currentEmail == null || !EMAIL_PATTERN.matcher(currentEmail).matches()) {
            throw new ApiException(BusinessErrorCode.EMAIL_NOT_BOUND_TO_USER);
        }
        VerificationCodeStore.VerificationResult result =
                verificationCodeStore.verifyRebindCode(REBIND_PROOF_SCOPE, currentEmail, currentEmailCode);
        if (result == VerificationCodeStore.VerificationResult.EXPIRED) {
            throw new ApiException(BusinessErrorCode.REBIND_CURRENT_EMAIL_CODE_EXPIRED);
        }
        if (result == VerificationCodeStore.VerificationResult.TOO_MANY_ATTEMPTS) {
            throw new ApiException(BusinessErrorCode.VERIFY_CODE_TOO_MANY_ATTEMPTS);
        }
        if (result == VerificationCodeStore.VerificationResult.INVALID) {
            throw new ApiException(BusinessErrorCode.REBIND_CURRENT_EMAIL_CODE_ERROR);
        }
    }

    private void verifyTotpProof(AccountSecurity security, String totpCode) {
        if (!Integer.valueOf(1).equals(security.getMfaTotpEnabled())) {
            throw new ApiException(BusinessErrorCode.MFA_TOTP_NOT_ENABLED);
        }
        if (!StringUtils.hasText(security.getMfaTotpSecret())) {
            throw new ApiException(BusinessErrorCode.MFA_TOTP_NOT_PROVISIONED);
        }
        if (!totpService.isEnabled()) {
            throw new ApiException(BusinessErrorCode.MFA_TOTP_SERVICE_DISABLED);
        }
        TotpVerifyResult result = totpService.verify(security.getMfaTotpSecret(), totpCode);
        if (!result.matched()) {
            throw new ApiException(BusinessErrorCode.MFA_TOTP_CODE_ERROR);
        }
    }

    private void verifyPasswordProof(Account user, String currentPassword) {
        if (!passwordCipherService.matches(currentPassword, user.getPassword())) {
            throw new ApiException(BusinessErrorCode.REBIND_PASSWORD_ERROR);
        }
    }

    private void requireRebindProof(String userId, String proofTicket) {
        if (!StringUtils.hasText(proofTicket)) {
            throw new ApiException(BusinessErrorCode.REBIND_PROOF_REQUIRED);
        }
        String storedUserId = verificationCodeStore.peekProofTicket(REBIND_SCENE, proofTicket.trim());
        if (!userId.equals(storedUserId)) {
            throw new ApiException(BusinessErrorCode.REBIND_PROOF_INVALID);
        }
    }

    private void consumeRebindProof(String userId, String proofTicket) {
        if (!StringUtils.hasText(proofTicket)) {
            throw new ApiException(BusinessErrorCode.REBIND_PROOF_REQUIRED);
        }
        String storedUserId = verificationCodeStore.consumeProofTicket(REBIND_SCENE, proofTicket.trim());
        if (!userId.equals(storedUserId)) {
            throw new ApiException(BusinessErrorCode.REBIND_PROOF_INVALID);
        }
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

    private String formatTime(LocalDateTime time) {
        return time == null ? null : time.format(ISO_FMT);
    }

    private EmailBuilder requireEmailBuilder() {
        EmailBuilder emailBuilder = emailBuilderProvider.getIfAvailable();
        if (emailBuilder == null) {
            throw new ApiException(BusinessErrorCode.EMAIL_SERVICE_DISABLED);
        }
        return emailBuilder;
    }
}
