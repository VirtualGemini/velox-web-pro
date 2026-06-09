package com.velox.module.system.mail.controller;

import com.velox.common.result.PageResult;
import com.velox.common.result.Result;
import com.velox.framework.security.api.annotation.RequirePermission;
import com.velox.module.system.id.frontend.SystemFrontendIdCodecSupport;
import com.velox.module.system.log.annotation.OperationLog;
import com.velox.module.system.log.annotation.OperationType;
import com.velox.module.system.mail.service.MailTemplateService;
import com.velox.module.system.mail.template.MailTemplateKind;
import com.velox.module.system.mail.template.MailTemplateType;
import com.velox.module.system.mail.vo.MailTemplateMetadataRespVO;
import com.velox.module.system.mail.vo.MailTemplatePageReqVO;
import com.velox.module.system.mail.vo.MailTemplatePreviewSampleRespVO;
import com.velox.module.system.mail.vo.MailTemplateRespVO;
import com.velox.module.system.mail.vo.MailTemplateSaveReqVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Tag(name = "openapi.system.mail_template.tag.name", description = "openapi.system.mail_template.tag.description")
@RestController
@RequestMapping("/mail-template")
@Validated
public class MailTemplateController {

    private final MailTemplateService mailTemplateService;
    private final SystemFrontendIdCodecSupport frontendIdCodecSupport;

    public MailTemplateController(MailTemplateService mailTemplateService,
                                  SystemFrontendIdCodecSupport frontendIdCodecSupport) {
        this.mailTemplateService = mailTemplateService;
        this.frontendIdCodecSupport = frontendIdCodecSupport;
    }

    @PostMapping("/create")
    @Operation(summary = "openapi.system.mail_template.create.summary")
    @RequirePermission("system:mail-template:create")
    public Result<String> createMailTemplate(@Valid @RequestBody MailTemplateSaveReqVO createReqVO) {
        return Result.ok(frontendIdCodecSupport.encodeIdentifier(mailTemplateService.createMailTemplate(createReqVO)));
    }

    @PutMapping("/update")
    @Operation(summary = "openapi.system.mail_template.update.summary")
    @RequirePermission("system:mail-template:update")
    public Result<Boolean> updateMailTemplate(@Valid @RequestBody MailTemplateSaveReqVO updateReqVO) {
        mailTemplateService.updateMailTemplate(updateReqVO);
        return Result.ok(true);
    }

    @PutMapping("/update-enabled")
    @Operation(summary = "openapi.system.mail_template.update_enabled.summary")
    @Parameter(name = "id", description = "openapi.common.id", required = true)
    @Parameter(name = "enabled", description = "openapi.common.enabled", required = true)
    @RequirePermission("system:mail-template:update")
    public Result<Boolean> updateMailTemplateEnabled(@RequestParam("id") String id,
                                                     @RequestParam("enabled") Integer enabled) {
        mailTemplateService.updateMailTemplateEnabled(id, enabled);
        return Result.ok(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "openapi.system.mail_template.delete.summary")
    @Parameter(name = "id", description = "openapi.common.id", required = true)
    @RequirePermission("system:mail-template:delete")
    public Result<Boolean> deleteMailTemplate(@RequestParam("id") String id) {
        mailTemplateService.deleteMailTemplate(id);
        return Result.ok(true);
    }

    @DeleteMapping("/delete-batch")
    @Operation(summary = "openapi.system.mail_template.delete_batch.summary")
    @RequirePermission("system:mail-template:delete")
    public Result<Boolean> deleteMailTemplateList(@RequestParam("ids") List<String> ids) {
        mailTemplateService.deleteMailTemplateList(ids);
        return Result.ok(true);
    }

    @GetMapping("/get")
    @Operation(summary = "openapi.system.mail_template.get.summary")
    @Parameter(name = "id", description = "openapi.common.id", required = true)
    @RequirePermission("system:mail-template:query")
    public Result<MailTemplateRespVO> getMailTemplate(@RequestParam("id") String id) {
        return Result.ok(mailTemplateService.getMailTemplate(id));
    }

    @GetMapping("/page")
    @Operation(summary = "openapi.system.mail_template.page.summary")
    @RequirePermission("system:mail-template:query")
    @OperationLog(
            module = "system.mail_template",
            action = "query",
            type = OperationType.QUERY,
            queryParamNames = {
                    "name", "sendType", "templateType", "enabled", "remark",
                    "createTimeStart", "createTimeEnd", "updateTimeStart", "updateTimeEnd"
            }
    )
    public Result<PageResult<MailTemplateRespVO>> getMailTemplatePage(MailTemplatePageReqVO pageReqVO) {
        return Result.ok(mailTemplateService.getMailTemplatePage(pageReqVO));
    }

    @GetMapping("/metadata")
    @Operation(summary = "openapi.system.mail_template.metadata.summary")
    @RequirePermission("system:mail-template:query")
    public Result<MailTemplateMetadataRespVO> getMailTemplateMetadata() {
        MailTemplateMetadataRespVO respVO = new MailTemplateMetadataRespVO();
        respVO.setSendTypes(Arrays.stream(MailTemplateType.values()).map(MailTemplateType::code).toList());
        respVO.setTemplateTypes(Arrays.stream(MailTemplateKind.values()).map(MailTemplateKind::code).toList());
        respVO.setVariables(List.of("username", "code", "validityMinutes", "appName"));
        respVO.setValidityMinutes(Map.of(
                MailTemplateType.AUTH_RESET_PASSWORD_CODE.code(), 10,
                MailTemplateType.AUTH_LOGIN_CODE.code(), 10,
                MailTemplateType.AUTH_LOGIN_MFA_CODE.code(), 5,
                MailTemplateType.ACCOUNT_EMAIL_UNBIND_CODE.code(), 10,
                MailTemplateType.ACCOUNT_EMAIL_REBIND_PROOF_CODE.code(), 10,
                MailTemplateType.ACCOUNT_EMAIL_REBIND_CODE.code(), 10,
                MailTemplateType.ACCOUNT_MFA_CODE.code(), 5
        ));
        respVO.setPreviewSample(buildPreviewSample());
        return Result.ok(respVO);
    }

    private MailTemplatePreviewSampleRespVO buildPreviewSample() {
        MailTemplatePreviewSampleRespVO sample = new MailTemplatePreviewSampleRespVO();
        sample.setUsername("User");
        sample.setCode("123456");
        sample.setAppName("Velox");
        sample.setFromAddress("no-reply@example.com");
        sample.setToAddress("user@example.com");
        return sample;
    }
}
