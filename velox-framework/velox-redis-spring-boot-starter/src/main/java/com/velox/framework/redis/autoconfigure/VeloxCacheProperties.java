package com.velox.framework.redis.autoconfigure;

import com.velox.framework.redis.common.type.RedisDisabledStrategyType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(VeloxCacheProperties.PREFIX)
public class VeloxCacheProperties {

    public static final String PREFIX = "velox.redis.cache";
    public static final String ENABLED_KEY = "enabled";
    public static final String ENABLED_TRUE = "true";
    public static final String ENABLED_FALSE = "false";

    private boolean enabled = true;
    private Integer redisScanBatchSize = 30;
    private String cacheManagerType = RedisAutoConfigurationConstants.DEFAULT_REDIS_CACHE_MANAGER_TYPE;
    private RedisDisabledStrategyType disabledStrategy = RedisDisabledStrategyType.FAIL_FAST;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getRedisScanBatchSize() {
        return redisScanBatchSize;
    }

    public void setRedisScanBatchSize(Integer redisScanBatchSize) {
        this.redisScanBatchSize = redisScanBatchSize;
    }

    public String getCacheManagerType() {
        return cacheManagerType;
    }

    public void setCacheManagerType(String cacheManagerType) {
        this.cacheManagerType = cacheManagerType;
    }

    public RedisDisabledStrategyType getDisabledStrategy() {
        return disabledStrategy;
    }

    public void setDisabledStrategy(RedisDisabledStrategyType disabledStrategy) {
        this.disabledStrategy = disabledStrategy;
    }
}
