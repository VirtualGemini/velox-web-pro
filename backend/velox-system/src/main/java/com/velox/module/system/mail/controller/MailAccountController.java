package com.velox.module.system.mail.controller;

import com.velox.common.result.PageResult;
import com.velox.common.result.Result;
import com.velox.framework.security.api.annotation.RequirePermission;
import com.velox.module.system.id.frontend.SystemFrontendIdCodecSupport;
import com.velox.module.system.mail.service.MailAccountService;
import com.velox.module.system.mail.vo.MailAccountPageReqVO;
import com.velox.module.system.mail.vo.MailAccountRespVO;
import com.velox.module.system.mail.vo.MailAccountSaveReqVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "openapi.system.mail_account.tag.name", description = "openapi.system.mail_account.tag.description")
@RestController
@RequestMapping("/mail-account")
@Validated
public class MailAccountController {

    private final MailAccountService mailAccountService;
    private final SystemFrontendIdCodecSupport frontendIdCodecSupport;

    public MailAccountController(MailAccountService mailAccountService,
                                 SystemFrontendIdCodecSupport frontendIdCodecSupport) {
        this.mailAccountService = mailAccountService;
        this.frontendIdCodecSupport = frontendIdCodecSupport;
    }

    @PostMapping("/create")
    @Operation(summary = "openapi.system.mail_account.create.summary")
    @RequirePermission("system:mail-account:create")
    public Result<String> createMailAccount(@Valid @RequestBody MailAccountSaveReqVO createReqVO) {
        return Result.ok(frontendIdCodecSupport.encodeIdentifier(mailAccountService.createMailAccount(createReqVO)));
    }

    @PutMapping("/update")
    @Operation(summary = "openapi.system.mail_account.update.summary")
    @RequirePermission("system:mail-account:update")
    public Result<Boolean> updateMailAccount(@Valid @RequestBody MailAccountSaveReqVO updateReqVO) {
        mailAccountService.updateMailAccount(updateReqVO);
        return Result.ok(true);
    }

    @PutMapping("/update-enabled")
    @Operation(summary = "openapi.system.mail_account.update_enabled.summary")
    @Parameter(name = "id", description = "openapi.common.id", required = true)
    @Parameter(name = "enabled", description = "openapi.common.enabled", required = true)
    @RequirePermission("system:mail-account:update")
    public Result<Boolean> updateMailAccountEnabled(@RequestParam("id") String id,
                                                    @RequestParam("enabled") Integer enabled) {
        mailAccountService.updateMailAccountEnabled(id, enabled);
        return Result.ok(true);
    }

    @PutMapping("/recover")
    @Operation(summary = "openapi.system.mail_account.recover.summary")
    @Parameter(name = "id", description = "openapi.common.id", required = true)
    @RequirePermission("system:mail-account:update")
    public Result<Boolean> recoverMailAccount(@RequestParam("id") String id) {
        mailAccountService.recoverMailAccount(id);
        return Result.ok(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "openapi.system.mail_account.delete.summary")
    @Parameter(name = "id", description = "openapi.common.id", required = true)
    @RequirePermission("system:mail-account:delete")
    public Result<Boolean> deleteMailAccount(@RequestParam("id") String id) {
        mailAccountService.deleteMailAccount(id);
        return Result.ok(true);
    }

    @DeleteMapping("/delete-batch")
    @Operation(summary = "openapi.system.mail_account.delete_batch.summary")
    @RequirePermission("system:mail-account:delete")
    public Result<Boolean> deleteMailAccountList(@RequestParam("ids") List<String> ids) {
        mailAccountService.deleteMailAccountList(ids);
        return Result.ok(true);
    }

    @GetMapping("/get")
    @Operation(summary = "openapi.system.mail_account.get.summary")
    @Parameter(name = "id", description = "openapi.common.id", required = true)
    @RequirePermission("system:mail-account:query")
    public Result<MailAccountRespVO> getMailAccount(@RequestParam("id") String id) {
        return Result.ok(mailAccountService.getMailAccount(id));
    }

    @GetMapping("/page")
    @Operation(summary = "openapi.system.mail_account.page.summary")
    @RequirePermission("system:mail-account:query")
    public Result<PageResult<MailAccountRespVO>> getMailAccountPage(MailAccountPageReqVO pageReqVO) {
        return Result.ok(mailAccountService.getMailAccountPage(pageReqVO));
    }

    @PostMapping("/test")
    @Operation(summary = "openapi.system.mail_account.test.summary")
    @Parameter(name = "id", description = "openapi.common.id", required = true)
    @Parameter(name = "toEmail", description = "openapi.system.mail_account.test.to_email", required = true)
    @RequirePermission("system:mail-account:query")
    public Result<Boolean> testMailAccount(@RequestParam("id") String id,
                                           @RequestParam("toEmail") String toEmail) {
        mailAccountService.testMailAccount(id, toEmail);
        return Result.ok(true);
    }
}
