package com.velox.framework.redis.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = VeloxRedisProperties.PREFIX)
public class VeloxRedisProperties {

    public static final String PREFIX = "velox.redis";
    public static final String ENABLED_KEY = "enabled";
    public static final String ENABLED_TRUE = "true";
    public static final String ENABLED_FALSE = "false";

    private boolean enabled = true;
    private String templateType = RedisAutoConfigurationConstants.DEFAULT_REDIS_TEMPLATE_TYPE;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getTemplateType() {
        return templateType;
    }

    public void setTemplateType(String templateType) {
        this.templateType = templateType;
    }
}
