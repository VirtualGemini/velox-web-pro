package com.velox.module.system.account.controller;

import com.velox.common.result.Result;
import com.velox.module.system.account.dto.AvatarUpdateCommand;
import com.velox.module.system.account.dto.AccountDeletionCommand;
import com.velox.module.system.account.dto.LanguageUpdateCommand;
import com.velox.module.system.account.dto.AccountTabInfoDTO;
import com.velox.module.system.account.dto.AccountInfoBasicDTO;
import com.velox.module.system.account.dto.AccountInfoDTO;
import com.velox.module.system.account.dto.AccountPasswordUpdateCommand;
import com.velox.module.system.account.dto.AccountProfileUpdateCommand;
import com.velox.module.system.account.dto.AccountRecoveryCommand;
import com.velox.module.system.account.dto.AccountUsernameUpdateCommand;
import com.velox.module.system.account.service.AccountInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "openapi.system.user.self.tag.name", description = "openapi.system.user.self.tag.description")
@RestController
@RequestMapping("/account")
public class AccountController {

    private final AccountInfoService accountInfoService;

    public AccountController(AccountInfoService accountInfoService) {
        this.accountInfoService = accountInfoService;
    }

    @Operation(summary = "openapi.system.user.self.get_info.summary")
    @GetMapping("/info")
    public Result<AccountInfoBasicDTO> getAccountInfo() {
        return Result.ok(accountInfoService.getAccountInfoBasicDTO());
    }

    @Operation(summary = "openapi.system.user.self.get_detail.summary")
    @GetMapping("/detail")
    public Result<AccountInfoDTO> getAccountDetail() {
        return Result.ok(accountInfoService.getAccountInfoDTO());
    }

    @Operation(summary = "openapi.system.user.self.get_detail.summary")
    @GetMapping("/tab")
    public Result<AccountTabInfoDTO> getAccountTabInfo() {
        return Result.ok(accountInfoService.getCurrentAccountTabInfo());
    }

    @Operation(summary = "openapi.system.user.self.update_profile.summary")
    @PutMapping("/profile")
    public Result<Boolean> updateProfile(@Valid @RequestBody AccountProfileUpdateCommand command) {
        return Result.ok(accountInfoService.updateCurrentUserProfile(command));
    }

    @Operation(summary = "openapi.system.user.self.update_profile.summary")
    @PutMapping("/username")
    public Result<Boolean> updateUsername(@Valid @RequestBody AccountUsernameUpdateCommand command) {
        return Result.ok(accountInfoService.updateCurrentUsername(command));
    }

    @Operation(summary = "openapi.system.user.self.update_password.summary")
    @PutMapping("/password")
    public Result<Boolean> updatePassword(@Valid @RequestBody AccountPasswordUpdateCommand command) {
        return Result.ok(accountInfoService.updateCurrentUserPassword(command));
    }

    @Operation(summary = "openapi.system.user.self.update_avatar.summary")
    @PutMapping("/avatar")
    public Result<AccountInfoBasicDTO> updateAvatar(@RequestBody AvatarUpdateCommand command) {
        accountInfoService.updateCurrentUserAvatar(command.getAvatarUrl());
        return Result.ok(accountInfoService.getAccountInfoBasicDTO());
    }

    @Operation(summary = "openapi.system.user.self.update_language.summary")
    @PutMapping("/language")
    public Result<Boolean> updateLanguage(@Valid @RequestBody LanguageUpdateCommand command) {
        return Result.ok(accountInfoService.updateCurrentUserLanguage(command.getLanguage()));
    }

    @Operation(summary = "openapi.system.user.self.update_profile.summary")
    @PostMapping("/deletion")
    public Result<Boolean> requestDeletion(@RequestBody AccountDeletionCommand command) {
        return Result.ok(accountInfoService.requestAccountDeletion(command));
    }

    @Operation(summary = "openapi.system.user.self.update_profile.summary")
    @PostMapping("/recovery")
    public Result<Boolean> recoverAccount(@RequestBody AccountRecoveryCommand command) {
        return Result.ok(accountInfoService.recoverCurrentAccount(command));
    }
}
