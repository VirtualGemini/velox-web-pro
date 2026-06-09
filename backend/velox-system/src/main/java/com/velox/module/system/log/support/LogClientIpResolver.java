package com.velox.module.system.log.support;

import com.velox.module.system.log.config.SystemLogProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class LogClientIpResolver {

    private static final String[] HEADERS = {
            "X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP"
    };

    private final SystemLogProperties properties;

    public LogClientIpResolver(SystemLogProperties properties) {
        this.properties = properties;
    }

    public String resolve(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        if (properties.isTrustProxyHeaders()) {
            for (String header : HEADERS) {
                String value = request.getHeader(header);
                if (StringUtils.hasText(value) && !"unknown".equalsIgnoreCase(value)) {
                    return value.split(",")[0].trim();
                }
            }
        }
        return request.getRemoteAddr();
    }
}
