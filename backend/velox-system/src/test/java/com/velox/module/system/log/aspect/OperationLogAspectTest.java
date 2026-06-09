package com.velox.module.system.log.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.velox.module.system.log.annotation.OperationLog;
import com.velox.module.system.log.annotation.OperationType;
import com.velox.module.system.log.config.SystemLogProperties;
import com.velox.module.system.log.support.LogPayloadSanitizer;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class OperationLogAspectTest {

    private final OperationLogAspect aspect = aspect();

    @Test
    void shouldRequireExplicitSearchMarkerForQueryLog() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/account/list");
        request.addParameter("current", "1");
        request.addParameter("size", "20");
        request.addParameter("username", "admin");

        assertThat(isExplicitQuery(request)).isFalse();
    }

    @Test
    void shouldSkipExplicitQueryWithoutMeaningfulSearchValue() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/account/list");
        request.addParameter("__operationLogQuery", "true");
        request.addParameter("current", "1");
        request.addParameter("size", "20");
        request.addParameter("username", " ");

        assertThat(isExplicitQuery(request)).isTrue();
        assertThat(sanitizedQueryRequestParams(request)).isNull();
    }

    @Test
    void shouldKeepOnlyWhitelistedSearchValues() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/account/list");
        request.addParameter("__operationLogQuery", "true");
        request.addParameter("current", "1");
        request.addParameter("size", "20");
        request.addParameter("username", "admin");

        String params = sanitizedQueryRequestParams(request);

        assertThat(params).contains("\"username\":\"admin\"");
        assertThat(params).doesNotContain("__operationLogQuery", "current", "size");
    }

    private boolean isExplicitQuery(MockHttpServletRequest request) throws Exception {
        Method method = OperationLogAspect.class.getDeclaredMethod("isExplicitQuery", jakarta.servlet.http.HttpServletRequest.class);
        method.setAccessible(true);
        return (Boolean) method.invoke(aspect, request);
    }

    private String sanitizedQueryRequestParams(MockHttpServletRequest request) throws Exception {
        Method method = OperationLogAspect.class.getDeclaredMethod(
                "sanitizedQueryRequestParams",
                jakarta.servlet.http.HttpServletRequest.class,
                OperationLog.class
        );
        method.setAccessible(true);
        return (String) method.invoke(aspect, request, operationLog());
    }

    private OperationLog operationLog() throws NoSuchMethodException {
        return OperationLogAspectTest.class
                .getDeclaredMethod("queryMethod")
                .getAnnotation(OperationLog.class);
    }

    private OperationLogAspect aspect() {
        try {
            Constructor<OperationLogAspect> constructor = OperationLogAspect.class.getConstructor(
                    org.springframework.context.ApplicationEventPublisher.class,
                    com.velox.framework.security.api.session.SecuritySessionService.class,
                    com.velox.module.system.persistence.AccountMapper.class,
                    LogPayloadSanitizer.class,
                    com.velox.module.system.log.support.LogRecordEnricher.class,
                    com.velox.module.system.id.frontend.SystemFrontendIdCodecSupport.class
            );
            return constructor.newInstance(
                    null,
                    null,
                    null,
                    new LogPayloadSanitizer(new ObjectMapper(), new SystemLogProperties()),
                    null,
                    null
            );
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    @OperationLog(
            module = "test",
            action = "query",
            type = OperationType.QUERY,
            queryParamNames = {"username"}
    )
    private void queryMethod() {
    }
}
