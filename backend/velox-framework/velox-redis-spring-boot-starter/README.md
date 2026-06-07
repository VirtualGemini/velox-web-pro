<h2 align="center" id="top">Velox Redis Spring Boot Starter</h2>
<p align="center">A standalone, hot-pluggable Redis starter focused on replaceable RedisTemplate wiring, pluggable cache manager strategy, startup connectivity validation, and Spring Boot auto-configuration.</p>
<div align="center">English | <a href="./README.zh-CN.md">简体中文</a></div>

<br />

## Overview

`velox-redis-spring-boot-starter` is an independent Redis capability module for Spring Boot applications.
It is designed to keep standard Spring injection paths stable while allowing developers to replace template creation, cache manager creation, and serializers. Redis itself is a mandatory dependency: connectivity is validated at startup and the application fails fast if Redis is unreachable.

This starter focuses on five core goals:

- stable `RedisTemplate` and `RedisCacheManager` injection for business modules
- hot-pluggable SPI for Redis template and cache manager replacement
- mandatory Redis with startup connectivity validation (fail-fast)
- serializer customization with a starter-owned default bean
- full Spring Boot auto-configuration isolation without `system` or `infra` coupling

## Architecture

The module follows the same starter layering style as `email` and `file`:

- `api`: public manager contracts such as `VeloxRedisManager` and `VeloxRedisCacheManager`
- `spi`: supported extension seams such as `RedisTemplateCreator`, `RedisCacheManagerCreator`, and their registrations
- `core`: default capability managers and TTL-aware cache manager base
- `support`: built-in template creator and cache manager creator
- `noop`: disabled-cache-mode cache manager and cache writer (used when `velox.redis.cache.enabled=false`)
- `exception` and `common`: starter-owned errors, messages, and cache disabled-strategy enum
- `autoconfigure`: Spring Boot entry points and configuration switches

## Built-In Capabilities

The starter ships with one built-in Redis template strategy and one built-in cache manager strategy:

- template type: `default`
- cache manager type: `default`

Both are registered as built-in registrations and can be overridden by publishing custom beans with the same type name.

## Standard Bean Exposure

The starter keeps standard Spring bean paths available:

- `redisTemplate`
- `redisCacheManager`
- `cacheManager`
- `RedisCacheConfiguration`

This means business modules can continue to inject standard Spring Redis types, while platform teams can still swap underlying implementations through SPI.

## Configuration

Basic configuration:

```yaml
velox:
  redis:
    template-type: default
    cache:
      enabled: true
      cache-manager-type: default
      redis-scan-batch-size: 30
      disabled-strategy: FAIL_FAST
spring:
  data:
    redis:
      host: localhost
      port: 6379
  cache:
    redis:
      time-to-live: 30m
      key-prefix: velox:
```

### Redis Is Required

Redis is a mandatory dependency of this starter. There is no in-memory or disabled fallback for the connection itself: `RedisConnectivityValidator` issues a `PING` during startup and throws `VeloxRedisException` (fail-fast) if Redis cannot be reached. Configure the connection through `spring.data.redis.*`.

### Cache Capability Switch

```yaml
velox:
  redis:
    cache:
      enabled: false
```

When disabled:

- `redisCacheManager` and `cacheManager` still remain injectable
- cache metadata and `RedisCacheConfiguration` remain available
- runtime cache behavior follows `velox.redis.cache.disabled-strategy`

Available disabled strategies:

- `FAIL_FAST`: cache operations throw `VeloxRedisException`
- `NOOP`: cache operations log warnings and act as no-op carriers

## Serializer Strategy

The starter exposes a default bean named `veloxRedisValueSerializer`.

Behavior:

- if the application already provides an `ObjectMapper`, the starter copies it and builds a Redis-specific serializer from that copy
- if no `ObjectMapper` is present, the starter creates a standalone `GenericJackson2JsonRedisSerializer`
- applications can fully replace the serializer by overriding the `veloxRedisValueSerializer` bean

This keeps Redis serialization aligned with application Jackson configuration without mutating the application's primary `ObjectMapper`.

## Extension Example

Register a custom Redis template strategy:

```java
@Bean
RedisTemplateRegistration customRedisTemplateRegistration() {
    return new RedisTemplateRegistration(
            "custom",
            (connectionFactory, redisValueSerializer) -> {
                RedisTemplate<String, Object> template = new RedisTemplate<>();
                template.setConnectionFactory(connectionFactory);
                template.setKeySerializer(RedisSerializer.string());
                template.setValueSerializer(redisValueSerializer);
                template.afterPropertiesSet();
                return template;
            }
    );
}
```

Register a custom cache manager strategy:

```java
@Bean
RedisCacheManagerRegistration customRedisCacheManagerRegistration() {
    return new RedisCacheManagerRegistration(
            "custom",
            new RedisCacheManagerCreator() {
                @Override
                public VeloxRedisCacheManager create(
                        CacheProperties cacheProperties,
                        VeloxCacheProperties veloxCacheProperties,
                        RedisConnectionFactory redisConnectionFactory,
                        RedisSerializer<Object> redisValueSerializer) {
                    // build your own RedisCacheManager subclass here
                    return new CustomRedisCacheManager(redisConnectionFactory, redisValueSerializer);
                }

                @Override
                public RedisCacheConfiguration createConfiguration(
                        CacheProperties cacheProperties,
                        VeloxCacheProperties veloxCacheProperties,
                        RedisSerializer<Object> redisValueSerializer) {
                    return RedisCacheConfiguration.defaultCacheConfig();
                }
            }
    );
}
```

Custom registrations override built-in defaults for the same type name.

## Primary Public Types

- `com.velox.framework.redis.api.manager.VeloxRedisManager`
- `com.velox.framework.redis.api.manager.VeloxRedisCacheManager`
- `com.velox.framework.redis.spi.redis.RedisTemplateCreator`
- `com.velox.framework.redis.spi.redis.RedisTemplateRegistration`
- `com.velox.framework.redis.spi.cache.RedisCacheManagerCreator`
- `com.velox.framework.redis.spi.cache.RedisCacheManagerRegistration`

## Auto-Configuration Entry

- `com.velox.framework.redis.autoconfigure.VeloxRedisAutoConfiguration`
- `com.velox.framework.redis.autoconfigure.VeloxCacheAutoConfiguration`
