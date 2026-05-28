package com.velox.module.system.account.config;

import com.velox.framework.redis.common.prefix.RedisPropertyPrefixes;
import com.velox.module.system.account.store.InMemoryAccountLanguageStore;
import com.velox.module.system.account.store.RedisAccountLanguageStore;
import com.velox.module.system.account.store.AccountLanguageStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class AccountLanguageStoreConfiguration {

    @Bean
    @ConditionalOnMissingBean(AccountLanguageStore.class)
    @ConditionalOnProperty(prefix = RedisPropertyPrefixes.REDIS, name = "enabled", havingValue = "true", matchIfMissing = true)
    public AccountLanguageStore redisAccountLanguageStore(StringRedisTemplate stringRedisTemplate) {
        return new RedisAccountLanguageStore(stringRedisTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(AccountLanguageStore.class)
    @ConditionalOnProperty(prefix = RedisPropertyPrefixes.REDIS, name = "enabled", havingValue = "false")
    public AccountLanguageStore inMemoryAccountLanguageStore() {
        return new InMemoryAccountLanguageStore();
    }
}
