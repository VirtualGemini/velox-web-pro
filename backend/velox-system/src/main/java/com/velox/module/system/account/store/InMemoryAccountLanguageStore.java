package com.velox.module.system.account.store;

import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryAccountLanguageStore implements AccountLanguageStore {

    private final Map<String, String> store = new ConcurrentHashMap<>();

    @Override
    public void save(String userId, String language) {
        if (!StringUtils.hasText(userId) || !StringUtils.hasText(language)) {
            return;
        }
        store.put(userId, language);
    }

    @Override
    public Optional<String> find(String userId) {
        if (!StringUtils.hasText(userId)) {
            return Optional.empty();
        }
        return Optional.ofNullable(store.get(userId));
    }
}
