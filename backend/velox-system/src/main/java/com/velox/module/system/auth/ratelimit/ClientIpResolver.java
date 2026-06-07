package com.velox.module.system.auth.ratelimit;

import com.velox.module.system.auth.properties.SystemAuthProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

/**
 * 客户端 IP 解析。
 *
 * <p>安全要点：默认只取 {@code request.getRemoteAddr()}。`X-Forwarded-For` 由客户端可写，
 * 直接信任会让攻击者每请求伪造一个 IP 从而绕过限流——因此仅当显式开启可信代理时，
 * 才从 XFF 右端按信任跳数取真实客户端 IP（而非最左侧可控值）。
 */
public class ClientIpResolver {

    private static final String XFF_HEADER = "X-Forwarded-For";

    private final SystemAuthProperties.RateLimit.TrustedProxy trustedProxy;

    public ClientIpResolver(SystemAuthProperties.RateLimit.TrustedProxy trustedProxy) {
        this.trustedProxy = trustedProxy;
    }

    public String resolve(HttpServletRequest request) {
        if (!trustedProxy.isEnabled()) {
            return request.getRemoteAddr();
        }
        String forwarded = request.getHeader(XFF_HEADER);
        if (!StringUtils.hasText(forwarded)) {
            return request.getRemoteAddr();
        }
        String[] hops = forwarded.split(",");
        int hops0 = trustedProxy.getTrustedHops() <= 0 ? 1 : trustedProxy.getTrustedHops();
        int index = hops.length - hops0;
        if (index < 0) {
            index = 0;
        }
        String ip = hops[index].trim();
        return StringUtils.hasText(ip) ? ip : request.getRemoteAddr();
    }
}
