package com.velox.module.system.mail.controller;

import com.velox.common.result.Result;
import com.velox.framework.security.api.annotation.RequirePermission;
import com.velox.module.system.id.frontend.SystemFrontendIdCodecSupport;
import com.velox.module.system.mail.service.MailGroupService;
import com.velox.module.system.mail.vo.MailGroupRespVO;
import com.velox.module.system.mail.vo.MailGroupSaveReqVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "openapi.system.mail_group.tag.name", description = "openapi.system.mail_group.tag.description")
@RestController
@RequestMapping("/mail-group")
@Validated
public class MailGroupController {

    private final MailGroupService mailGroupService;
    private final SystemFrontendIdCodecSupport frontendIdCodecSupport;

    public MailGroupController(MailGroupService mailGroupService,
                               SystemFrontendIdCodecSupport frontendIdCodecSupport) {
        this.mailGroupService = mailGroupService;
        this.frontendIdCodecSupport = frontendIdCodecSupport;
    }

    @GetMapping("/list")
    @Operation(summary = "openapi.system.mail_group.list.summary")
    @RequirePermission("system:mail-account:query")
    public Result<List<MailGroupRespVO>> getMailGroupList() {
        return Result.ok(mailGroupService.getMailGroupList());
    }

    @PostMapping("/create")
    @Operation(summary = "openapi.system.mail_group.create.summary")
    @RequirePermission("system:mail-account:group")
    public Result<String> createMailGroup(@Valid @RequestBody MailGroupSaveReqVO createReqVO) {
        return Result.ok(frontendIdCodecSupport.encodeIdentifier(mailGroupService.createMailGroup(createReqVO)));
    }

    @PutMapping("/update")
    @Operation(summary = "openapi.system.mail_group.update.summary")
    @RequirePermission("system:mail-account:group")
    public Result<Boolean> updateMailGroup(@Valid @RequestBody MailGroupSaveReqVO updateReqVO) {
        mailGroupService.updateMailGroup(updateReqVO);
        return Result.ok(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "openapi.system.mail_group.delete.summary")
    @Parameter(name = "id", description = "openapi.common.id", required = true)
    @RequirePermission("system:mail-account:group")
    public Result<Boolean> deleteMailGroup(@RequestParam("id") String id) {
        mailGroupService.deleteMailGroup(id);
        return Result.ok(true);
    }
}
