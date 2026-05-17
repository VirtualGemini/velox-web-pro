package com.velox.framework.id.support.codec;

import com.velox.framework.id.api.codec.IdCodec;
import com.velox.framework.id.common.message.IdGeneratorCommonMessages;
import com.velox.framework.id.common.type.IdCodecNames;
import com.velox.framework.id.exception.VeloxIdGeneratorException;
import org.springframework.util.StringUtils;

public class IdentityIdCodec implements IdCodec {

    @Override
    public String getName() {
        return IdCodecNames.IDENTITY;
    }

    @Override
    public String encode(String sourceId) {
        return normalize(sourceId, IdGeneratorCommonMessages.SOURCE_ID_MUST_NOT_BE_BLANK);
    }

    @Override
    public String decode(String encodedId) {
        return normalize(encodedId, IdGeneratorCommonMessages.ENCODED_ID_MUST_NOT_BE_BLANK);
    }

    private String normalize(String value, String blankMessage) {
        if (!StringUtils.hasText(value)) {
            throw new VeloxIdGeneratorException(blankMessage);
        }
        return value.trim();
    }
}
