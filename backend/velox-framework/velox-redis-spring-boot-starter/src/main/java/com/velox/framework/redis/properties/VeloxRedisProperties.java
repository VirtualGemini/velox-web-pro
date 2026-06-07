package com.velox.framework.redis.properties;

import com.velox.framework.redis.autoconfigure.RedisAutoConfigurationConstants;
import com.velox.framework.redis.common.prefix.RedisPropertyPrefixes;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = RedisPropertyPrefixes.REDIS)
public class VeloxRedisProperties {

    private String templateType = RedisAutoConfigurationConstants.DEFAULT_REDIS_TEMPLATE_TYPE;

    public String getTemplateType() {
        return templateType;
    }

    public void setTemplateType(String templateType) {
        this.templateType = templateType;
    }
}
