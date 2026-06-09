package com.velox.module.system.log.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.velox.framework.security.api.session.SecuritySessionService;
import com.velox.module.system.log.config.SystemLogProperties;
import com.velox.module.system.log.domain.model.ApiLogRecord;
import com.velox.module.system.log.event.ApiLogEvent;
import com.velox.module.system.log.support.LogPayloadSanitizer;
import com.velox.module.system.log.support.LogRecordEnricher;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ApiLogCaptureFilterTest {

    @Test
    void shouldDropResponseBodyWhenCapturedBodyExceedsLimit() throws Exception {
        SystemLogProperties properties = properties();
        properties.getPayload().setMaxBodyBytes(8);
        CapturingPublisher publisher = new CapturingPublisher();
        ApiLogCaptureFilter filter = filter(properties, publisher);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/demo");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (servletRequest, servletResponse) -> {
            servletResponse.setContentType("application/json");
            servletResponse.getWriter().write("{\"message\":\"123456789\"}");
        };

        filter.doFilter(request, response, chain);

        ApiLogRecord record = publisher.apiLogRecord();
        assertThat(record.getResponseSize()).isGreaterThan(8);
        assertThat(record.getResponseBody()).isNull();
        assertThat(response.getContentAsString()).isEqualTo("{\"message\":\"123456789\"}");
    }

    @Test
    void shouldCaptureAndSanitizeSmallJsonResponseOnIncludedPath() throws Exception {
        SystemLogProperties properties = properties();
        properties.getPayload().setMaxBodyBytes(256);
        CapturingPublisher publisher = new CapturingPublisher();
        ApiLogCaptureFilter filter = filter(properties, publisher);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/demo");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (servletRequest, servletResponse) -> {
            servletResponse.setContentType("application/json");
            servletResponse.getWriter().write("{\"code\":0,\"message\":\"ok\",\"token\":\"secret-token\"}");
        };

        filter.doFilter(request, response, chain);

        ApiLogRecord record = publisher.apiLogRecord();
        assertThat(record.getBusinessCode()).isEqualTo("0");
        assertThat(record.getBusinessMessage()).isEqualTo("ok");
        assertThat(record.getResponseBody())
                .contains("\"token\":\"******\"")
                .doesNotContain("secret-token");
        assertThat(response.getContentAsString()).isEqualTo("{\"code\":0,\"message\":\"ok\",\"token\":\"secret-token\"}");
    }

    private SystemLogProperties properties() {
        SystemLogProperties properties = new SystemLogProperties();
        properties.getPayload().setCaptureResponseBody(true);
        properties.getPayload().setBodyIncludedPaths(List.of("/api/**"));
        properties.getPayload().setAllowedContentTypes(List.of("application/json"));
        return properties;
    }

    private ApiLogCaptureFilter filter(SystemLogProperties properties, CapturingPublisher publisher) {
        return new ApiLogCaptureFilter(
                publisher,
                new AnonymousSecuritySessionService(),
                null,
                new LogPayloadSanitizer(new ObjectMapper(), properties),
                new NoopLogRecordEnricher(),
                new ObjectMapper(),
                properties
        );
    }

    private static class CapturingPublisher implements ApplicationEventPublisher {
        private final List<Object> events = new ArrayList<>();

        @Override
        public void publishEvent(ApplicationEvent event) {
            events.add(event);
        }

        @Override
        public void publishEvent(Object event) {
            events.add(event);
        }

        ApiLogRecord apiLogRecord() {
            assertThat(events).hasSize(1);
            assertThat(events.get(0)).isInstanceOf(ApiLogEvent.class);
            return ((ApiLogEvent) events.get(0)).getRecord();
        }
    }

    private static class AnonymousSecuritySessionService implements SecuritySessionService {
        @Override
        public String login(String loginId) {
            return null;
        }

        @Override
        public void logout() {
        }

        @Override
        public boolean isAuthenticated() {
            return false;
        }

        @Override
        public String currentLoginIdOrNull() {
            return null;
        }

        @Override
        public String currentTokenOrNull() {
            return null;
        }
    }

    private static class NoopLogRecordEnricher extends LogRecordEnricher {
        NoopLogRecordEnricher() {
            super(null, null, null, null);
        }

        @Override
        public void enrich(ApiLogRecord record, HttpServletRequest request) {
        }
    }
}
