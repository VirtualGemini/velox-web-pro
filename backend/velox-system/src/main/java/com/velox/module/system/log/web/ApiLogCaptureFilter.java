package com.velox.module.system.log.web;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.velox.common.exception.ApiException;
import com.velox.framework.security.api.session.SecuritySessionService;
import com.velox.module.system.domain.model.Account;
import com.velox.module.system.log.config.SystemLogProperties;
import com.velox.module.system.log.domain.model.ApiLogRecord;
import com.velox.module.system.log.event.ApiLogEvent;
import com.velox.module.system.log.support.LogPayloadSanitizer;
import com.velox.module.system.log.support.LogRecordEnricher;
import com.velox.module.system.persistence.AccountMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@Order(Ordered.LOWEST_PRECEDENCE - 20)
public class ApiLogCaptureFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ApiLogCaptureFilter.class);
    private static final Set<String> EXCLUDED_PREFIXES = Set.of("/log/", "/actuator", "/swagger", "/v3/api-docs", "/webjars", "/favicon.ico", "/assets", "/static");
    private static final Set<String> CAPTURED_HEADERS = Set.of(
            "Content-Type", "Accept", "Authorization", "User-Agent", "X-Forwarded-For",
            "X-Real-IP", "X-Request-Id", "X-Trace-Id", "X-Caller-App"
    );

    private final ApplicationEventPublisher eventPublisher;
    private final SecuritySessionService securitySessionService;
    private final AccountMapper accountMapper;
    private final LogPayloadSanitizer sanitizer;
    private final LogRecordEnricher enricher;
    private final ObjectMapper objectMapper;
    private final SystemLogProperties properties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public ApiLogCaptureFilter(ApplicationEventPublisher eventPublisher,
                               SecuritySessionService securitySessionService,
                               AccountMapper accountMapper,
                               LogPayloadSanitizer sanitizer,
                               LogRecordEnricher enricher,
                               ObjectMapper objectMapper,
                               SystemLogProperties properties) {
        this.eventPublisher = eventPublisher;
        this.securitySessionService = securitySessionService;
        this.accountMapper = accountMapper;
        this.sanitizer = sanitizer;
        this.enricher = enricher;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;
        if (uri.contains("/log/")) return true;
        for (String prefix : EXCLUDED_PREFIXES) {
            if (uri.equals(prefix) || uri.startsWith(prefix)) return true;
        }
        if (matchesAny(uri, properties.getPayload().getExcludedPaths())) return true;
        return isMultipart(request) || looksLikeDownload(uri);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        LocalDateTime requestTime = LocalDateTime.now(ZoneOffset.UTC);
        boolean captureRequestBody = shouldCaptureRequestBody(request);
        boolean captureResponseBody = shouldCaptureResponseBody(request);
        Throwable failure = null;
        if (!captureRequestBody && !captureResponseBody) {
            try {
                filterChain.doFilter(request, response);
            } catch (Throwable throwable) {
                failure = throwable;
                throw throwable;
            } finally {
                publish(request, response, failure, requestTime, LocalDateTime.now(ZoneOffset.UTC), System.currentTimeMillis() - start, null, null, null, requestSize(request), responseSize(response));
            }
            return;
        }

        HttpServletRequest requestToUse = captureRequestBody
                ? new LimitedContentCachingRequestWrapper(request, maxBodyBytes())
                : request;
        HttpServletResponse responseToUse = captureResponseBody
                ? new LimitedContentCachingResponseWrapper(response, maxBodyBytes())
                : response;
        try {
            filterChain.doFilter(requestToUse, responseToUse);
        } catch (Throwable throwable) {
            failure = throwable;
            throw throwable;
        } finally {
            String requestBody = null;
            String responseBody = null;
            String rawResponseBody = null;
            Long currentRequestSize = requestSize(request);
            Long currentResponseSize = responseSize(response);
            if (requestToUse instanceof LimitedContentCachingRequestWrapper wrappedRequest) {
                requestBody = readRequestBody(wrappedRequest);
                currentRequestSize = wrappedRequest.isOverflow()
                        ? requestSize(request)
                        : (long) wrappedRequest.getContentAsByteArray().length;
            }
            if (responseToUse instanceof LimitedContentCachingResponseWrapper wrappedResponse) {
                wrappedResponse.flushCapture();
                responseBody = readResponseBody(wrappedResponse);
                rawResponseBody = readRawResponseBody(wrappedResponse);
                currentResponseSize = wrappedResponse.getContentLength();
            }
            publish(requestToUse, responseToUse, failure, requestTime, LocalDateTime.now(ZoneOffset.UTC), System.currentTimeMillis() - start, requestBody, responseBody, rawResponseBody, currentRequestSize, currentResponseSize);
        }
    }

    private void publish(HttpServletRequest request,
                         HttpServletResponse response,
                         Throwable failure,
                         LocalDateTime requestTime,
                         LocalDateTime responseTime,
                         long cost,
                         String requestBody,
                         String responseBody,
                         String rawResponseBody,
                         Long requestSize,
                         Long responseSize) {
        try {
            String accountId = securitySessionService.currentLoginIdOrNull();
            Account account = account(accountId);
            ApiLogRecord record = new ApiLogRecord();
            record.setAccountId(accountId);
            record.setUsername(account != null ? account.getUsername() : null);
            record.setCallerApp(callerApp(request));
            record.setRequestUrl(fullRequestUrl(request));
            record.setRequestMethod(request.getMethod());
            record.setRequestUri(request.getRequestURI());
            Object pattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
            record.setMatchedPattern(pattern != null ? String.valueOf(pattern) : null);
            Object handler = request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE);
            if (handler instanceof HandlerMethod method) {
                record.setJavaMethod(method.getBeanType().getName() + "." + method.getMethod().getName());
            }
            record.setHttpStatus(response.getStatus());
            record.setResult(failure == null && response.getStatus() < 500 ? 1 : 0);
            record.setRequestQuery(sanitizer.sanitizeRequestParameters(request, properties.getPayload().getRequestParamsMaxChars()));
            record.setRequestHeaders(requestHeaders(request));
            record.setRequestBody(requestBody);
            record.setResponseBody(responseBody);
            record.setRequestSize(requestSize);
            record.setResponseSize(responseSize);
            applyBusinessResult(record, rawResponseBody);
            if (failure instanceof ApiException apiException) {
                record.setErrorCode(String.valueOf(apiException.getErrorCode().code()));
                record.setErrorMessage(apiException.getRawMessage());
            } else if (failure != null) {
                record.setErrorCode(failure.getClass().getSimpleName());
                record.setErrorMessage(sanitizer.sanitizeError(failure));
            }
            if (failure != null) {
                record.setExceptionStack(exceptionStack(failure));
            }
            record.setServerIp(serverIp());
            record.setServerNode(serverNode());
            record.setRequestTime(requestTime);
            record.setResponseTime(responseTime);
            record.setCostTimeMs(cost);
            record.setApiTime(requestTime);
            enricher.enrich(record, request);
            eventPublisher.publishEvent(new ApiLogEvent(record));
        } catch (RuntimeException exception) {
            log.warn("Failed to publish api log", exception);
        }
    }

    private void applyBusinessResult(ApiLogRecord record, String responseBody) {
        if (!StringUtils.hasText(responseBody)) return;
        try {
            JsonNode node = objectMapper.readTree(responseBody);
            if (node.has("code")) record.setBusinessCode(node.get("code").asText());
            if (node.has("msg")) record.setBusinessMessage(sanitizer.truncate(node.get("msg").asText(), 255));
            if (node.has("message") && !StringUtils.hasText(record.getBusinessMessage())) record.setBusinessMessage(sanitizer.truncate(node.get("message").asText(), 255));
        } catch (Exception ignored) {
            log.debug("Failed to parse api business response");
        }
    }

    private String readRequestBody(LimitedContentCachingRequestWrapper request) {
        if (request.isOverflow()) {
            return null;
        }
        return readText(
                request.getContentAsByteArray(),
                request.getContentType(),
                request.getCharacterEncoding(),
                properties.getPayload().getRequestParamsMaxChars(),
                true
        );
    }

    private String readResponseBody(LimitedContentCachingResponseWrapper response) {
        if (response.isOverflow()) {
            return null;
        }
        return readText(
                response.getContentAsByteArray(),
                response.getContentType(),
                response.getCharacterEncoding(),
                properties.getPayload().getResponseBodyMaxChars(),
                true
        );
    }

    private String readRawResponseBody(LimitedContentCachingResponseWrapper response) {
        if (response.isOverflow()) {
            return null;
        }
        return readText(
                response.getContentAsByteArray(),
                response.getContentType(),
                response.getCharacterEncoding(),
                properties.getPayload().getResponseBodyMaxChars(),
                false
        );
    }

    private String readText(byte[] bytes, String contentType, String encoding, int maxChars, boolean sanitize) {
        if (bytes.length == 0 || bytes.length > maxBodyBytes() || !isAllowedContentType(contentType)) {
            return null;
        }
        String value = new String(bytes, charset(encoding));
        return sanitize ? sanitizer.sanitizeJsonOrText(value, maxChars) : sanitizer.truncate(value, maxChars);
    }

    private String fullRequestUrl(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        StringBuffer url = request.getRequestURL();
        String query = request.getQueryString();
        return StringUtils.hasText(query) ? url.append('?').append(query).toString() : url.toString();
    }

    private String callerApp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String caller = request.getHeader("X-Caller-App");
        return StringUtils.hasText(caller) ? sanitizer.truncate(caller.trim(), 128) : null;
    }

    private String requestHeaders(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Map<String, String> headers = new LinkedHashMap<>();
        for (String header : CAPTURED_HEADERS) {
            String value = request.getHeader(header);
            if (StringUtils.hasText(value)) {
                headers.put(header, value);
            }
        }
        return headers.isEmpty()
                ? null
                : sanitizer.sanitizeObject(headers, properties.getPayload().getRequestParamsMaxChars());
    }

    private String exceptionStack(Throwable throwable) {
        StringWriter writer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(writer));
        return sanitizer.sanitizeJsonOrText(writer.toString(), properties.getPayload().getExceptionStackMaxChars());
    }

    private String serverIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception ignored) {
            return null;
        }
    }

    private String serverNode() {
        String podName = System.getenv("POD_NAME");
        if (StringUtils.hasText(podName)) {
            return podName;
        }
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception ignored) {
            return null;
        }
    }

    private Long requestSize(HttpServletRequest request) {
        long contentLength = request.getContentLengthLong();
        return contentLength >= 0 ? contentLength : null;
    }

    private Long responseSize(HttpServletResponse response) {
        return parseContentLength(response.getHeader("Content-Length"));
    }

    private Long parseContentLength(String contentLength) {
        if (!StringUtils.hasText(contentLength)) {
            return null;
        }
        try {
            return Long.parseLong(contentLength.trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private Charset charset(String encoding) {
        try {
            return StringUtils.hasText(encoding) ? Charset.forName(encoding) : StandardCharsets.UTF_8;
        } catch (Exception exception) {
            return StandardCharsets.UTF_8;
        }
    }

    private boolean isMultipart(HttpServletRequest request) {
        String contentType = request.getContentType();
        return StringUtils.hasText(contentType) && contentType.toLowerCase().startsWith("multipart/");
    }

    private boolean looksLikeDownload(String uri) {
        return pathMatcher.match("/**/download/**", uri) || pathMatcher.match("/**/db/**", uri);
    }

    private boolean shouldCaptureRequestBody(HttpServletRequest request) {
        if (!properties.getPayload().isCaptureRequestBody()) {
            return false;
        }
        if (!isBodyPathIncluded(request.getRequestURI())) {
            return false;
        }
        if (!isAllowedContentType(request.getContentType())) {
            return false;
        }
        Long requestSize = requestSize(request);
        return requestSize == null || requestSize <= maxBodyBytes();
    }

    private boolean shouldCaptureResponseBody(HttpServletRequest request) {
        return properties.getPayload().isCaptureResponseBody()
                && isBodyPathIncluded(request.getRequestURI());
    }

    private boolean isBodyPathIncluded(String uri) {
        return matchesAny(uri, properties.getPayload().getBodyIncludedPaths());
    }

    private boolean matchesAny(String uri, List<String> patterns) {
        if (!StringUtils.hasText(uri) || patterns == null || patterns.isEmpty()) {
            return false;
        }
        for (String pattern : patterns) {
            if (StringUtils.hasText(pattern) && pathMatcher.match(pattern.trim(), uri)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAllowedContentType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return false;
        }
        List<String> allowedContentTypes = properties.getPayload().getAllowedContentTypes();
        if (allowedContentTypes == null || allowedContentTypes.isEmpty()) {
            return false;
        }
        String lower = contentType.toLowerCase();
        for (String allowedContentType : allowedContentTypes) {
            if (StringUtils.hasText(allowedContentType)
                    && lower.contains(allowedContentType.trim().toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private int maxBodyBytes() {
        return Math.max(0, properties.getPayload().getMaxBodyBytes());
    }

    private Account account(String accountId) {
        if (!StringUtils.hasText(accountId)) return null;
        if (accountMapper == null) return null;
        return accountMapper.selectOne(new LambdaQueryWrapper<Account>()
                .eq(Account::getId, accountId)
                .eq(Account::getDeleted, 0)
                .last("limit 1"));
    }

    private static class LimitedContentCachingRequestWrapper extends ContentCachingRequestWrapper {
        private boolean overflow;

        LimitedContentCachingRequestWrapper(HttpServletRequest request, int contentCacheLimit) {
            super(request, contentCacheLimit);
        }

        @Override
        protected void handleContentOverflow(int contentCacheLimit) {
            overflow = true;
        }

        boolean isOverflow() {
            return overflow;
        }
    }

    private static class LimitedContentCachingResponseWrapper extends HttpServletResponseWrapper {
        private final int limit;
        private final ByteArrayOutputStream content = new ByteArrayOutputStream();
        private ServletOutputStream outputStream;
        private PrintWriter writer;
        private long contentLength;
        private boolean overflow;

        LimitedContentCachingResponseWrapper(HttpServletResponse response, int limit) {
            super(response);
            this.limit = Math.max(0, limit);
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            if (writer != null) {
                throw new IllegalStateException("getWriter() has already been called");
            }
            if (outputStream == null) {
                outputStream = new LimitedContentCachingOutputStream(getResponse().getOutputStream());
            }
            return outputStream;
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            if (writer != null) {
                return writer;
            }
            if (outputStream != null) {
                throw new IllegalStateException("getOutputStream() has already been called");
            }
            outputStream = new LimitedContentCachingOutputStream(getResponse().getOutputStream());
            writer = new PrintWriter(new OutputStreamWriter(outputStream, getCharacterEncoding()));
            return writer;
        }

        @Override
        public void flushBuffer() throws IOException {
            if (writer != null) {
                writer.flush();
            }
            if (outputStream != null) {
                outputStream.flush();
            }
            super.flushBuffer();
        }

        byte[] getContentAsByteArray() {
            return content.toByteArray();
        }

        long getContentLength() {
            return contentLength;
        }

        boolean isOverflow() {
            return overflow;
        }

        void flushCapture() throws IOException {
            if (writer != null) {
                writer.flush();
            }
            if (outputStream != null) {
                outputStream.flush();
            }
        }

        private void cache(byte[] bytes, int offset, int length) {
            contentLength += length;
            if (length <= 0 || limit <= 0) {
                overflow = overflow || length > 0;
                return;
            }
            int writable = limit - content.size();
            if (writable <= 0) {
                overflow = true;
                return;
            }
            int size = Math.min(writable, length);
            content.write(bytes, offset, size);
            overflow = overflow || size < length;
        }

        private class LimitedContentCachingOutputStream extends ServletOutputStream {
            private final ServletOutputStream delegate;

            LimitedContentCachingOutputStream(ServletOutputStream delegate) {
                this.delegate = delegate;
            }

            @Override
            public boolean isReady() {
                return delegate.isReady();
            }

            @Override
            public void setWriteListener(WriteListener listener) {
                delegate.setWriteListener(listener);
            }

            @Override
            public void write(int value) throws IOException {
                delegate.write(value);
                cache(new byte[]{(byte) value}, 0, 1);
            }

            @Override
            public void write(byte[] bytes, int offset, int length) throws IOException {
                delegate.write(bytes, offset, length);
                cache(bytes, offset, length);
            }

            @Override
            public void flush() throws IOException {
                delegate.flush();
            }

            @Override
            public void close() throws IOException {
                delegate.close();
            }
        }
    }
}
