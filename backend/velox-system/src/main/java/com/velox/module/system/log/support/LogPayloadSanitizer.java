package com.velox.module.system.log.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.velox.module.system.log.config.SystemLogProperties;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class LogPayloadSanitizer {

    private static final String MASK = "******";
    private static final Set<String> SENSITIVE_KEYS = SensitiveLogField.normalizedKeys();
    private static final Pattern SENSITIVE_TEXT = Pattern.compile(
            "(?i)(" + SensitiveLogField.textPatternAlternation() + ")\\s*[:=]\\s*([^,}&\\s]+)"
    );

    private final ObjectMapper objectMapper;
    private final SystemLogProperties properties;

    public LogPayloadSanitizer(ObjectMapper objectMapper, SystemLogProperties properties) {
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    public String sanitizeObject(Object value, int maxChars) {
        if (value == null) {
            return null;
        }
        try {
            JsonNode node = objectMapper.valueToTree(value);
            sanitizeNode(node);
            return truncate(objectMapper.writeValueAsString(node), maxChars);
        } catch (Exception exception) {
            return sanitizeText(String.valueOf(value), maxChars);
        }
    }

    public String sanitizeJsonOrText(String value, int maxChars) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            JsonNode node = objectMapper.readTree(value);
            sanitizeNode(node);
            return truncate(objectMapper.writeValueAsString(node), maxChars);
        } catch (Exception ignored) {
            return sanitizeText(value, maxChars);
        }
    }

    public String sanitizeRequestParameters(HttpServletRequest request, int maxChars) {
        if (request == null || request.getParameterMap().isEmpty()) {
            return null;
        }
        Map<String, Object> params = new LinkedHashMap<>();
        request.getParameterMap().forEach((key, values) -> params.put(key, values == null || values.length != 1 ? values : values[0]));
        return sanitizeObject(params, maxChars);
    }

    public String sanitizeArgs(Object[] args, String[] excludeParamNames) {
        Map<String, Object> params = new LinkedHashMap<>();
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                if (arg == null || arg instanceof ServletRequest || arg instanceof ServletResponse) {
                    continue;
                }
                if (arg instanceof MultipartFile file) {
                    params.put("arg" + i, Map.of("filename", file.getOriginalFilename(), "size", file.getSize(), "contentType", file.getContentType()));
                    continue;
                }
                if (arg instanceof MultipartFile[] files) {
                    params.put("arg" + i, "multipart files: " + files.length);
                    continue;
                }
                params.put("arg" + i, arg);
            }
        }
        for (String name : excludeParamNames) {
            if (StringUtils.hasText(name)) {
                params.remove(name);
            }
        }
        return params.isEmpty() ? null : sanitizeObject(params, properties.getPayload().getRequestParamsMaxChars());
    }

    public int requestParamsMaxChars() {
        return properties.getPayload().getRequestParamsMaxChars();
    }

    public String responseSummary(Object value) {
        if (value == null || value instanceof HttpServletResponse) {
            return null;
        }
        try {
            JsonNode node = objectMapper.valueToTree(value);
            JsonNode data = node.has("data") ? node.get("data") : node;
            ObjectNode summary = objectMapper.createObjectNode();
            copyIfPresent(node, summary, "code");
            copyIfPresent(node, summary, "msg");
            copyIfPresent(node, summary, "message");
            copyIfPresent(node, summary, "success");
            if (summary.isEmpty() && data != node) {
                copyIfPresent(data, summary, "code");
                copyIfPresent(data, summary, "message");
                copyIfPresent(data, summary, "success");
            }
            JsonNode output = summary.isEmpty() ? node : summary;
            sanitizeNode(output);
            return truncate(objectMapper.writeValueAsString(output), properties.getPayload().getResponseBodyMaxChars());
        } catch (Exception exception) {
            return sanitizeText(String.valueOf(value), properties.getPayload().getResponseBodyMaxChars());
        }
    }

    public String sanitizeError(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        return sanitizeText(throwable.getMessage(), properties.getPayload().getErrorMessageMaxChars());
    }

    public String truncateUserAgent(String userAgent) {
        return truncate(userAgent, properties.getPayload().getUserAgentMaxChars());
    }

    private void sanitizeNode(JsonNode node) {
        if (node == null) {
            return;
        }
        if (node instanceof ObjectNode objectNode) {
            objectNode.fieldNames().forEachRemaining(field -> {
                JsonNode child = objectNode.get(field);
                if (isSensitive(field)) {
                    objectNode.put(field, MASK);
                } else {
                    sanitizeNode(child);
                }
            });
        } else if (node instanceof ArrayNode arrayNode) {
            arrayNode.forEach(this::sanitizeNode);
        }
    }

    private void copyIfPresent(JsonNode source, ObjectNode target, String field) {
        if (source != null && source.has(field)) {
            target.set(field, source.get(field));
        }
    }

    private boolean isSensitive(String key) {
        if (!StringUtils.hasText(key)) {
            return false;
        }
        return SENSITIVE_KEYS.contains(SensitiveLogField.normalize(key));
    }

    private String sanitizeText(String value, int maxChars) {
        if (value == null) {
            return null;
        }
        return truncate(SENSITIVE_TEXT.matcher(value).replaceAll("$1=" + MASK), maxChars);
    }

    public String truncate(String value, int maxChars) {
        if (value == null) {
            return null;
        }
        int limit = Math.max(0, maxChars);
        if (limit == 0 || value.length() <= limit) {
            return value;
        }
        return value.substring(0, limit) + "...";
    }
}
