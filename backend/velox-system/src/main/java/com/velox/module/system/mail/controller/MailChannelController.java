package com.velox.module.system.mail.controller;

import com.velox.common.result.Result;
import com.velox.framework.security.api.annotation.RequirePermission;
import com.velox.module.system.mail.service.MailChannelService;
import com.velox.module.system.mail.vo.MailChannelActiveReqVO;
import com.velox.module.system.mail.vo.MailChannelRespVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "openapi.system.mail_channel.tag.name", description = "openapi.system.mail_channel.tag.description")
@RestController
@RequestMapping("/mail-channel")
@Validated
public class MailChannelController {

    private final MailChannelService mailChannelService;

    public MailChannelController(MailChannelService mailChannelService) {
        this.mailChannelService = mailChannelService;
    }

    @GetMapping("/list")
    @Operation(summary = "openapi.system.mail_channel.list.summary")
    @RequirePermission("system:mail-account:query")
    public Result<List<MailChannelRespVO>> getMailChannelList() {
        return Result.ok(mailChannelService.getMailChannelList());
    }

    @PutMapping("/update-active")
    @Operation(summary = "openapi.system.mail_channel.update_active.summary")
    @RequirePermission("system:mail-account:channel")
    public Result<Boolean> updateChannelActive(@Valid @RequestBody MailChannelActiveReqVO reqVO) {
        mailChannelService.updateChannelActive(reqVO);
        return Result.ok(true);
    }
}
