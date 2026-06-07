package com.velox.module.system.auth.ratelimit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.List;

/**
 * Redis 固定窗口限流器。
 *
 * <p>原子 Lua：首次 {@code INCR} 后设置窗口 {@code EXPIRE}，窗口内累加，过期自动重置。
 * <p>**强可用：Redis 异常时 fail-open**（放行 + 告警日志）——可用性优先，绝不让限流器自身把登录打挂。
 */
public class RedisRateLimiter {

    private static final Logger log = LoggerFactory.getLogger(RedisRateLimiter.class);

    private static final DefaultRedisScript<Long> INCR_WITH_TTL = new DefaultRedisScript<>(
            """
                    local current = redis.call('INCR', KEYS[1])
                    if current == 1 then
                        redis.call('EXPIRE', KEYS[1], ARGV[1])
                    end
                    return current
                    """,
            Long.class
    );

    private final StringRedisTemplate stringRedisTemplate;

    public RedisRateLimiter(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * @return true 放行；false 超阈拒绝。Redis 异常时返回 true（fail-open）。
     */
    public boolean tryAcquire(String key, int limit, int windowSeconds) {
        try {
            Long current = stringRedisTemplate.execute(
                    INCR_WITH_TTL, List.of(key), String.valueOf(windowSeconds));
            return current == null || current <= limit;
        } catch (RuntimeException ex) {
            log.warn("[RateLimit] Redis 异常，fail-open 放行 key={}: {}", key, ex.getMessage());
            return true;
        }
    }
}
