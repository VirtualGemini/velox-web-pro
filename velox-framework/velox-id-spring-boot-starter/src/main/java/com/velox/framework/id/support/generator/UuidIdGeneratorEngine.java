package com.velox.framework.id.support.generator;

import com.velox.framework.id.api.codec.IdCodec;
import com.velox.framework.id.api.model.GeneratedId;
import com.velox.framework.id.api.model.IdGeneratorMetadata;
import com.velox.framework.id.common.type.IdGeneratorStrategies;
import com.velox.framework.id.common.type.IdSourceModes;
import com.velox.framework.id.properties.VeloxIdProperties;
import com.velox.framework.id.spi.generator.AbstractIdGeneratorEngine;

import java.util.UUID;

public class UuidIdGeneratorEngine extends AbstractIdGeneratorEngine {

    private final IdCodec codec;
    private final IdGeneratorMetadata metadata;
    private static final String UUID_STRIP_REGEX = "-";
    private static final String UUID_REPLACE_STRING = "";

    public UuidIdGeneratorEngine(VeloxIdProperties properties, IdCodec codec) {
        this.codec = codec;
        this.metadata = new IdGeneratorMetadata(
                true,
                IdSourceModes.APPLICATION,
                IdGeneratorStrategies.UUID,
                codec.getName(),
                properties.getDatabase().getInitMode(),
                null,
                null,
                null,
                null,
                false
        );
    }

    @Override
    protected GeneratedId doNextId(String businessType) {
        String sourceValue = UUID.randomUUID().toString().replace(UUID_STRIP_REGEX, UUID_REPLACE_STRING);
        return new GeneratedId(businessType, sourceValue, codec.encode(sourceValue), metadata);
    }

    @Override
    public IdGeneratorMetadata metadata() {
        return metadata;
    }
}
