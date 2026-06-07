package com.velox.framework.web.support.servlet;

import com.velox.framework.web.properties.VeloxWebProperties;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * 安全响应头过滤器。
 *
 * <p>为响应注入 {@code X-Content-Type-Options}、{@code X-Frame-Options}、{@code Referrer-Policy}，
 * 以及可选的 {@code Content-Security-Policy} 与 {@code Strict-Transport-Security}（仅 https 且配置非空时）。
 * 仅设置非空配置项；CSP/HSTS 默认空以避免打断 Swagger / 误锁 HTTPS，按需开启。
 */
public class SecurityHeadersFilter implements Filter {

    private final VeloxWebProperties.SecurityHeaders config;

    public SecurityHeadersFilter(VeloxWebProperties.SecurityHeaders config) {
        this.config = config;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (config.isEnabled() && response instanceof HttpServletResponse httpResponse) {
            setIfPresent(httpResponse, "X-Content-Type-Options", config.getContentTypeOptions());
            setIfPresent(httpResponse, "X-Frame-Options", config.getFrameOptions());
            setIfPresent(httpResponse, "Referrer-Policy", config.getReferrerPolicy());
            setIfPresent(httpResponse, "Content-Security-Policy", config.getContentSecurityPolicy());
            if (StringUtils.hasText(config.getStrictTransportSecurity()) && request.isSecure()) {
                httpResponse.setHeader("Strict-Transport-Security", config.getStrictTransportSecurity());
            }
        }
        chain.doFilter(request, response);
    }

    private void setIfPresent(HttpServletResponse response, String name, String value) {
        if (StringUtils.hasText(value)) {
            response.setHeader(name, value);
        }
    }
}
