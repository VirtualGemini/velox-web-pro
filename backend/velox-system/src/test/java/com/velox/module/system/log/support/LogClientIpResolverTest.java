package com.velox.module.system.log.support;

import com.velox.module.system.log.config.SystemLogProperties;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class LogClientIpResolverTest {

    @Test
    void shouldUseRemoteAddressWhenTrustedProxyDisabled() {
        SystemLogProperties properties = new SystemLogProperties();
        LogClientIpResolver resolver = new LogClientIpResolver(properties);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("0:0:0:0:0:0:0:1");
        request.addHeader("X-Forwarded-For", "192.168.1.20");

        String ip = resolver.resolve(request);

        assertThat(ip).isEqualTo("::1");
    }

    @Test
    void shouldResolveClientIpFromForwardedForWhenTrustedProxyEnabled() {
        SystemLogProperties properties = new SystemLogProperties();
        properties.getTrustedProxy().setEnabled(true);
        LogClientIpResolver resolver = new LogClientIpResolver(properties);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("0:0:0:0:0:0:0:1");
        request.addHeader("X-Forwarded-For", "192.168.1.20");

        String ip = resolver.resolve(request);

        assertThat(ip).isEqualTo("192.168.1.20");
    }

    @Test
    void shouldKeepLegacyTrustProxyHeadersUsingFirstForwardedForAddress() {
        SystemLogProperties properties = new SystemLogProperties();
        properties.setTrustProxyHeaders(true);
        LogClientIpResolver resolver = new LogClientIpResolver(properties);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("0:0:0:0:0:0:0:1");
        request.addHeader("X-Forwarded-For", "192.168.1.20, 10.0.0.1");

        String ip = resolver.resolve(request);

        assertThat(ip).isEqualTo("192.168.1.20");
    }

    @Test
    void shouldResolveForwardedForByTrustedHopCountFromRight() {
        SystemLogProperties properties = new SystemLogProperties();
        properties.getTrustedProxy().setEnabled(true);
        properties.getTrustedProxy().setTrustedHops(2);
        LogClientIpResolver resolver = new LogClientIpResolver(properties);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.3");
        request.addHeader("X-Forwarded-For", "203.0.113.10, 10.0.0.1, 10.0.0.2");

        String ip = resolver.resolve(request);

        assertThat(ip).isEqualTo("10.0.0.1");
    }

    @Test
    void shouldFallbackToRealIpHeaderWhenForwardedForMissing() {
        SystemLogProperties properties = new SystemLogProperties();
        properties.getTrustedProxy().setEnabled(true);
        LogClientIpResolver resolver = new LogClientIpResolver(properties);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("0:0:0:0:0:0:0:1");
        request.addHeader("X-Real-IP", "192.168.1.21");

        String ip = resolver.resolve(request);

        assertThat(ip).isEqualTo("192.168.1.21");
    }
}
