package com.velox.framework.redis.core.health;

import com.velox.framework.redis.exception.VeloxRedisException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * 启动期 Redis 连通性自检。
 *
 * <p>Redis 是本系统的硬依赖（验证码、登录态、账号语言等均依赖 Redis 共享存储）。
 * 若连接不可用，在启动阶段直接 fail-fast，避免“伪可用”掩盖配置错误，或拖到首个业务请求才暴露故障。
 */
public class RedisConnectivityValidator implements InitializingBean {

    private final RedisConnectionFactory redisConnectionFactory;

    public RedisConnectivityValidator(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
    }

    @Override
    public void afterPropertiesSet() {
        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
            connection.ping();
        } catch (RuntimeException ex) {
            throw new VeloxRedisException(
                    "Redis is required but unreachable at startup. "
                            + "Verify spring.data.redis.* and that the Redis server is running.",
                    ex);
        }
    }
}
