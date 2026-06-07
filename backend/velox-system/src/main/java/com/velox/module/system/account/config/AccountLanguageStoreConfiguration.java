package com.velox.module.system.account.config;

import com.velox.module.system.account.store.RedisAccountLanguageStore;
import com.velox.module.system.account.store.AccountLanguageStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class AccountLanguageStoreConfiguration {

    @Bean
    @ConditionalOnMissingBean(AccountLanguageStore.class)
    public AccountLanguageStore redisAccountLanguageStore(StringRedisTemplate stringRedisTemplate) {
        return new RedisAccountLanguageStore(stringRedisTemplate);
    }
}
