package com.velox.framework.redis.exception;

public class VeloxRedisException extends RuntimeException {

    public VeloxRedisException(String message) {
        super(message);
    }

    public VeloxRedisException(String message, Throwable cause) {
        super(message, cause);
    }
}
