package com.velox.module.system.accesscontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

/**
 * 更新已启用登录方式/渠道的命令（普通登录方式 / 第三方登录 / 第三方注册共用）。
 * <p>
 * 普通登录方式不可清空到空集（由服务层校验）；第三方渠道允许全部关闭。
 */
@Schema(description = "openapi.settings.access-control.channels.schema")
public class AccessControlChannelsCommand {

    @Schema(description = "openapi.settings.access-control.channels.values")
    private List<String> values = new ArrayList<>();

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values == null ? new ArrayList<>() : values;
    }
}
