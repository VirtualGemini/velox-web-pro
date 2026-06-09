package com.velox.framework.web.core.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.velox.framework.web.RequestDateTimeFormatter;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * 将以 UTC 存储的 {@link LocalDateTime} 转换到当前请求时区，并输出为 {@code yyyy-MM-dd HH:mm:ss}，
 * 与系统管理各接口手动调用 {@link RequestDateTimeFormatter#format} 的行为保持一致。
 */
public class RequestZoneLocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {

    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        gen.writeString(RequestDateTimeFormatter.format(value));
    }
}
