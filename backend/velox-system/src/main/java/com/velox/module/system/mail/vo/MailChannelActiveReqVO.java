package com.velox.module.system.mail.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 切换生效发件渠道请求：activeChannelIds 中的渠道置为生效，其余置为不生效。
 */
@Schema(description = "openapi.system.mail_channel.active_req.schema")
public class MailChannelActiveReqVO {

    @Schema(description = "openapi.system.mail_channel.active_req.active_ids")
    private List<String> activeChannelIds;

    public List<String> getActiveChannelIds() {
        return activeChannelIds;
    }

    public void setActiveChannelIds(List<String> activeChannelIds) {
        this.activeChannelIds = activeChannelIds;
    }
}
