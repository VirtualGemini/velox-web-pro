package com.velox.module.system.auth.ratelimit;

import com.velox.common.exception.ApiException;
import com.velox.common.exception.BusinessErrorCode;
import com.velox.module.system.auth.properties.SystemAuthProperties;
import com.velox.module.system.verification.common.EffectivePolicy;
import com.velox.module.system.verification.common.VerificationScene;
import com.velox.module.system.verification.enforce.ClientIpHolder;
import com.velox.module.system.verification.service.VerificationPolicyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 认证端点 IP 限流拦截器。
 *
 * <p>对受限端点集合按「端点 + 客户端 IP」限流；限额数值由 `captcha` 验证策略（IP 维度）动态驱动，
 * 策略关闭或 IP 维度关闭即不限流。同时把客户端 IP 写入 {@link ClientIpHolder} 供服务层按 IP 维度限制。
 * 跳过 OPTIONS 预检；限流总开关 {@code velox.system.auth.rate-limit.enabled} 可应急关闭。
 */
public class AuthRateLimitInterceptor implements HandlerInterceptor {

    private static final String AUTH_PREFIX = "/auth";

    private final SystemAuthProperties.RateLimit rateLimit;
    private final RedisRateLimiter rateLimiter;
    private final ClientIpResolver clientIpResolver;
    private final VerificationPolicyService policyService;
    private final String apiPrefix;

    public AuthRateLimitInterceptor(SystemAuthProperties.RateLimit rateLimit,
                                    RedisRateLimiter rateLimiter,
                                    ClientIpResolver clientIpResolver,
                                    VerificationPolicyService policyService,
                                    String apiPrefix) {
        this.rateLimit = rateLimit;
        this.rateLimiter = rateLimiter;
        this.clientIpResolver = clientIpResolver;
        this.policyService = policyService;
        this.apiPrefix = apiPrefix == null ? "" : apiPrefix;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        String suffix = resolveAuthSuffix(request.getRequestURI());
        if (suffix == null) {
            return true;
        }
        // 解析客户端 IP 并置入线程持有者，供服务层（LoginServiceImpl 等）按 IP 维度限制
        String ip = clientIpResolver.resolve(request);
        ClientIpHolder.set(ip);

        if (!rateLimit.isEnabled()) {
            return true;
        }
        // 仅对受限端点集合应用 IP 限流；限额数值由 captcha 策略驱动（可在管理端调整/关闭）
        if (!rateLimit.getRules().containsKey(suffix)) {
            return true;
        }
        EffectivePolicy captcha = policyService.getEffectivePolicy(VerificationScene.CAPTCHA);
        if (!captcha.enabled() || !captcha.limitByIp()) {
            return true;
        }
        String key = "rl:auth:" + suffix + ":" + ip;
        if (!rateLimiter.tryAcquire(key, captcha.maxAttempts(), captcha.recoverySeconds())) {
            throw new ApiException(BusinessErrorCode.TOO_MANY_REQUESTS);
        }
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Object handler,
                                Exception ex) {
        ClientIpHolder.clear();
    }

    private String resolveAuthSuffix(String requestUri) {
        String uri = requestUri;
        if (!apiPrefix.isEmpty() && uri.startsWith(apiPrefix)) {
            uri = uri.substring(apiPrefix.length());
        }
        if (!uri.startsWith(AUTH_PREFIX)) {
            return null;
        }
        return uri.substring(AUTH_PREFIX.length());
    }
}
