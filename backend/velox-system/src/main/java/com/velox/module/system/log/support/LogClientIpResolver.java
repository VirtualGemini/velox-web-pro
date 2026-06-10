package com.velox.module.system.log.support;

import com.velox.module.system.log.config.SystemLogProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

@Component
public class LogClientIpResolver {

    private static final String X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String[] SINGLE_IP_HEADERS = {
            "X-Forwarded-For", "CF-Connecting-IP", "True-Client-IP", "X-Real-IP",
            "Client-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP"
    };

    private final SystemLogProperties properties;

    public LogClientIpResolver(SystemLogProperties properties) {
        this.properties = properties;
    }

    public String resolve(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        if (properties.getTrustedProxy().isEnabled() || properties.isTrustProxyHeaders()) {
            String forwarded = request.getHeader(X_FORWARDED_FOR);
            if (StringUtils.hasText(forwarded)) {
                String clientIp = firstValid(forwarded);
                if (StringUtils.hasText(clientIp)) {
                    return normalize(clientIp);
                }
            }
            String singleHeaderIp = resolveSingleIpHeader(request);
            if (StringUtils.hasText(singleHeaderIp)) {
                return normalize(singleHeaderIp);
            }
        }
        return normalize(request.getRemoteAddr());
    }

    private String resolveSingleIpHeader(HttpServletRequest request) {
        for (String header : SINGLE_IP_HEADERS) {
            String ip = firstValid(request.getHeader(header));
            if (StringUtils.hasText(ip)) {
                return ip;
            }
        }
        return null;
    }

    private String resolveForwardedFor(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String[] hops = value.split(",");
        int trustedHops = Math.max(1, properties.getTrustedProxy().getTrustedHops());
        int clientIndex = hops.length - trustedHops;
        if (clientIndex >= 0 && clientIndex < hops.length) {
            return firstValid(hops[clientIndex]);
        }
        return firstValid(hops[0]);
    }

    private String firstValid(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        for (String item : value.split(",")) {
            String ip = item.trim();
            if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
                return ip;
            }
        }
        return null;
    }

    private String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String ip = value.trim();
        if (ip.startsWith("[") && ip.contains("]")) {
            ip = ip.substring(1, ip.indexOf(']'));
        } else if (ip.contains(":") && ip.indexOf(':') == ip.lastIndexOf(':')) {
            ip = ip.substring(0, ip.indexOf(':'));
        }
        try {
            InetAddress address = InetAddress.getByName(ip);
            if (address.isLoopbackAddress() && address instanceof Inet6Address) {
                return "::1";
            }
            if (address instanceof Inet4Address) {
                return address.getHostAddress();
            }
            return ip;
        } catch (Exception ignored) {
            return ip;
        }
    }
}
