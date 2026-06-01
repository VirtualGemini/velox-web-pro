package com.velox.module.system.account.security.controller;

import com.velox.common.result.Result;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "openapi.system.user.security.tag.name", description = "openapi.system.user.security.tag.description")
@RestController
@RequestMapping("/account/security")
public class AccountSecurityController {

    private final AccountSecurityService accountSecurityService;

    public AccountSecurityController(AccountSecurityService accountSecurityService) {
        this.accountSecurityService = accountSecurityService;
    }

    @Operation(summary = "openapi.system.user.security.get_status.summary")
    @GetMapping("/status")
    public Result<SecurityStatusDTO> getStatus() {
        return Result.ok(accountSecurityService.getStatus());
    }

    @Operation(summary = "openapi.system.user.security.send_email_unbind_code.summary")
    @PostMapping("/email/unbind/send-code")
    public Result<Void> sendEmailUnbindCode() {
        accountSecurityService.sendEmailUnbindCode();
        return Result.ok();
    }

    @Operation(summary = "openapi.system.user.security.send_email_rebind_proof_code.summary")
    @PostMapping("/email/rebind/proof/send-code")
    public Result<Void> sendEmailRebindProofCode() {
        accountSecurityService.sendEmailRebindProofCode();
        return Result.ok();
    }

    @Operation(summary = "openapi.system.user.security.verify_email_rebind_proof.summary")
    @PostMapping("/email/rebind/proof/verify")
    public Result<EmailRebindProofDTO> verifyEmailRebindProof(
            @Valid @RequestBody EmailRebindProofVerifyCommand command) {
        return Result.ok(accountSecurityService.verifyEmailRebindProof(command));
    }

    @Operation(summary = "openapi.system.user.security.send_email_rebind_code.summary")
    @PostMapping("/email/rebind/send-code")
    public Result<Void> sendEmailRebindCode(@Valid @RequestBody EmailRebindSendCodeCommand command) {
        accountSecurityService.sendEmailRebindCode(command);
        return Result.ok();
    }

    @Operation(summary = "openapi.system.user.security.rebind_email.summary")
    @PutMapping("/email/rebind")
    public Result<Boolean> rebindEmail(@Valid @RequestBody EmailRebindCommand command) {
        return Result.ok(accountSecurityService.rebindEmail(command));
    }

    @Operation(summary = "openapi.system.user.security.unbind_email.summary")
    @PutMapping("/email/unbind")
    public Result<Boolean> unbindEmail(@Valid @RequestBody EmailUnbindCommand command) {
        return Result.ok(accountSecurityService.unbindEmail(command));
    }

    @Operation(summary = "openapi.system.user.security.update_login_methods.summary")
    @PutMapping("/login-methods")
    public Result<Boolean> updateLoginMethods(@Valid @RequestBody LoginMethodsUpdateCommand command) {
        return Result.ok(accountSecurityService.updateLoginMethods(command));
    }

    @Operation(summary = "openapi.system.user.security.send_mfa_email_code.summary")
    @PostMapping("/mfa/email/send-code")
    public Result<Void> sendMfaEmailCode() {
        accountSecurityService.sendMfaEmailCode();
        return Result.ok();
    }

    @Operation(summary = "openapi.system.user.security.update_mfa_email.summary")
    @PutMapping("/mfa/email")
    public Result<Boolean> updateMfaEmail(@Valid @RequestBody MfaEmailUpdateCommand command) {
        return Result.ok(accountSecurityService.updateMfaEmail(command));
    }

    @Operation(summary = "openapi.system.user.security.provision_mfa_totp.summary")
    @PostMapping("/mfa/totp/provision")
    public Result<MfaTotpProvisionDTO> provisionMfaTotp() {
        return Result.ok(accountSecurityService.provisionMfaTotp());
    }

    @Operation(summary = "openapi.system.user.security.enable_mfa_totp.summary")
    @PutMapping("/mfa/totp/enable")
    public Result<MfaTotpEnableResultDTO> enableMfaTotp(@Valid @RequestBody MfaTotpEnableCommand command) {
        return Result.ok(accountSecurityService.enableMfaTotp(command));
    }

    @Operation(summary = "openapi.system.user.security.disable_mfa_totp.summary")
    @PutMapping("/mfa/totp/disable")
    public Result<Boolean> disableMfaTotp(@Valid @RequestBody MfaTotpDisableCommand command) {
        return Result.ok(accountSecurityService.disableMfaTotp(command));
    }
}
