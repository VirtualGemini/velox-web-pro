package com.velox.module.system.account.store;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;

import java.util.Optional;

public class RedisAccountLanguageStore implements AccountLanguageStore {

    private static final String KEY_PREFIX = "user:lang:";

    private final StringRedisTemplate stringRedisTemplate;

    public RedisAccountLanguageStore(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void save(String userId, String language) {
        if (!StringUtils.hasText(userId) || !StringUtils.hasText(language)) {
            return;
        }
        stringRedisTemplate.opsForValue().set(KEY_PREFIX + userId, language);
    }

    @Override
    public Optional<String> find(String userId) {
        if (!StringUtils.hasText(userId)) {
            return Optional.empty();
        }
        String value = stringRedisTemplate.opsForValue().get(KEY_PREFIX + userId);
        return Optional.ofNullable(value);
    }
}
