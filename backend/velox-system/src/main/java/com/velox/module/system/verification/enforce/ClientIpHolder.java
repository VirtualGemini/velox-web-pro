package com.velox.module.system.verification.enforce;

/**
 * 请求线程内的客户端 IP 持有者（仿 TraceIdAccessor 的 MDC/ThreadLocal 约定）。
 *
 * <p>由 AuthRateLimitInterceptor 在 /auth/** 的 preHandle 写入、afterCompletion 清理，
 * 使服务层（无 HttpServletRequest 的 LoginServiceImpl 等）也能拿到 IP 做按 IP 维度限制。
 * 取不到时（非 web 线程/未覆盖路径）调用方应优雅降级为仅账号维度。
 */
public final class ClientIpHolder {

    private static final ThreadLocal<String> HOLDER = new ThreadLocal<>();

    private ClientIpHolder() {
    }

    public static void set(String ip) {
        HOLDER.set(ip);
    }

    public static String get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
