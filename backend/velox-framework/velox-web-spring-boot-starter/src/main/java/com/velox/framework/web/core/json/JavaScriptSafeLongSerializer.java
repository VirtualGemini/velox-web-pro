package com.velox.framework.web.core.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class JavaScriptSafeLongSerializer extends JsonSerializer<Long> {

    private static final long MAX_SAFE_INTEGER = 9_007_199_254_740_991L;

    @Override
    public void serialize(Long value, JsonGenerator generator, SerializerProvider serializers) throws IOException {
        if (value > MAX_SAFE_INTEGER || value < -MAX_SAFE_INTEGER) {
            generator.writeString(value.toString());
            return;
        }
        generator.writeNumber(value);
    }
}
