package com.velox.module.system.auth.config;

import com.velox.framework.redis.common.prefix.RedisPropertyPrefixes;
import com.velox.framework.security.properties.SecurityProperties;
import com.velox.module.system.auth.status.ActiveUserStatusService;
import com.velox.module.system.auth.status.InMemoryActiveUserStatusService;
import com.velox.module.system.auth.status.RedisActiveUserStatusService;
import com.velox.module.system.auth.store.InMemoryVerificationCodeStore;
import com.velox.module.system.auth.store.RedisVerificationCodeStore;
import com.velox.module.system.auth.store.VerificationCodeStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class AuthStoreConfiguration {

    @Bean
    @ConditionalOnMissingBean(VerificationCodeStore.class)
    @ConditionalOnProperty(prefix = RedisPropertyPrefixes.REDIS, name = "enabled", havingValue = "true", matchIfMissing = true)
    public VerificationCodeStore redisVerificationCodeStore(StringRedisTemplate stringRedisTemplate,
                                                            SecurityProperties securityProperties) {
        return new RedisVerificationCodeStore(stringRedisTemplate, securityProperties);
    }

    @Bean
    @ConditionalOnMissingBean(VerificationCodeStore.class)
    @ConditionalOnProperty(prefix = RedisPropertyPrefixes.REDIS, name = "enabled", havingValue = "false")
    public VerificationCodeStore inMemoryVerificationCodeStore(SecurityProperties securityProperties) {
        return new InMemoryVerificationCodeStore(securityProperties);
    }

    @Bean
    @ConditionalOnMissingBean(ActiveUserStatusService.class)
    @ConditionalOnProperty(prefix = RedisPropertyPrefixes.REDIS, name = "enabled", havingValue = "true", matchIfMissing = true)
    public ActiveUserStatusService redisActiveUserStatusService(StringRedisTemplate stringRedisTemplate,
                                                                SecurityProperties securityProperties) {
        return new RedisActiveUserStatusService(stringRedisTemplate, securityProperties);
    }

    @Bean
    @ConditionalOnMissingBean(ActiveUserStatusService.class)
    @ConditionalOnProperty(prefix = RedisPropertyPrefixes.REDIS, name = "enabled", havingValue = "false")
    public ActiveUserStatusService inMemoryActiveUserStatusService(SecurityProperties securityProperties) {
        return new InMemoryActiveUserStatusService(securityProperties);
    }
}
