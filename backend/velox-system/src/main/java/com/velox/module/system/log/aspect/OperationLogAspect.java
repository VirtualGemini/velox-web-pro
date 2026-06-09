package com.velox.module.system.log.aspect;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.velox.common.exception.ApiException;
import com.velox.framework.security.api.session.SecuritySessionService;
import com.velox.module.system.domain.model.Account;
import com.velox.module.system.id.frontend.SystemFrontendIdCodecSupport;
import com.velox.module.system.log.annotation.OperationLog;
import com.velox.module.system.log.annotation.OperationType;
import com.velox.module.system.log.domain.model.OperationLogRecord;
import com.velox.module.system.log.event.OperationLogEvent;
import com.velox.module.system.log.support.LogPayloadSanitizer;
import com.velox.module.system.log.support.LogRecordEnricher;
import com.velox.module.system.persistence.AccountMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Aspect
@Component
public class OperationLogAspect {

    private static final Logger log = LoggerFactory.getLogger(OperationLogAspect.class);
    private static final String EXPLICIT_QUERY_PARAM = "__operationLogQuery";

    private final ApplicationEventPublisher eventPublisher;
    private final SecuritySessionService securitySessionService;
    private final AccountMapper accountMapper;
    private final LogPayloadSanitizer sanitizer;
    private final LogRecordEnricher enricher;
    private final SystemFrontendIdCodecSupport frontendIdCodecSupport;

    public OperationLogAspect(ApplicationEventPublisher eventPublisher,
                              SecuritySessionService securitySessionService,
                              AccountMapper accountMapper,
                              LogPayloadSanitizer sanitizer,
                              LogRecordEnricher enricher,
                              SystemFrontendIdCodecSupport frontendIdCodecSupport) {
        this.eventPublisher = eventPublisher;
        this.securitySessionService = securitySessionService;
        this.accountMapper = accountMapper;
        this.sanitizer = sanitizer;
        this.enricher = enricher;
        this.frontendIdCodecSupport = frontendIdCodecSupport;
    }

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        long start = System.currentTimeMillis();
        Object response = null;
        Throwable failure = null;
        try {
            response = joinPoint.proceed();
            return response;
        } catch (Throwable throwable) {
            failure = throwable;
            throw throwable;
        } finally {
            publish(joinPoint, operationLog, response, failure, System.currentTimeMillis() - start);
        }
    }

    private void publish(ProceedingJoinPoint joinPoint, OperationLog operationLog, Object response, Throwable failure, long cost) {
        try {
            HttpServletRequest request = currentRequest();
            String queryRequestParams = null;
            if (operationLog.type() == OperationType.QUERY) {
                if (!isExplicitQuery(request)) {
                    return;
                }
                queryRequestParams = sanitizedQueryRequestParams(request, operationLog);
                if (!StringUtils.hasText(queryRequestParams)) {
                    return;
                }
            }
            String accountId = securitySessionService.currentLoginIdOrNull();
            Account account = account(accountId);
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            OperationLogRecord record = new OperationLogRecord();
            record.setModuleName(operationLog.module());
            record.setActionName(operationLog.action());
            record.setOperationType(operationLog.type().name());
            record.setTargetType(operationLog.targetType());
            record.setTargetId(resolveTargetId(operationLog.targetIdExpression(), joinPoint.getArgs()));
            record.setAccountId(accountId);
            record.setUsername(account != null ? account.getUsername() : null);
            record.setOperatorType("ADMIN");
            record.setRequestMethod(request != null ? request.getMethod() : null);
            record.setRequestUri(request != null ? request.getRequestURI() : null);
            record.setJavaMethod(signature.getDeclaringTypeName() + "." + signature.getName());
            if (operationLog.saveRequest()) {
                record.setRequestParams(sanitizeRequestParams(request, joinPoint, operationLog, queryRequestParams));
            }
            if (operationLog.saveResponse()) {
                record.setResponseSummary(sanitizer.responseSummary(response));
            }
            record.setResult(failure == null ? 1 : 0);
            if (failure instanceof ApiException apiException) {
                record.setErrorCode(String.valueOf(apiException.getErrorCode().code()));
                record.setErrorMessage(apiException.getRawMessage());
            } else if (failure != null) {
                record.setErrorCode(failure.getClass().getSimpleName());
                record.setErrorMessage(sanitizer.sanitizeError(failure));
            }
            record.setCostTimeMs(cost);
            record.setOperationTime(LocalDateTime.now(ZoneOffset.UTC));
            enricher.enrich(record, request);
            eventPublisher.publishEvent(new OperationLogEvent(record));
        } catch (RuntimeException exception) {
            log.warn("Failed to publish operation log", exception);
        }
    }

    private String resolveTargetId(String expression, Object[] args) {
        if (!StringUtils.hasText(expression)) {
            return null;
        }
        String trimmed = expression.trim();
        if (trimmed.startsWith("#arg") || trimmed.startsWith("#p")) {
            String number = trimmed.replace("#arg", "").replace("#p", "");
            try {
                int index = Integer.parseInt(number);
                String value = args != null && args.length > index && args[index] != null ? String.valueOf(args[index]) : null;
                return frontendIdCodecSupport.decodeIdentifier(value);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return trimmed;
    }

    private Account account(String accountId) {
        if (!StringUtils.hasText(accountId)) return null;
        return accountMapper.selectOne(new LambdaQueryWrapper<Account>().eq(Account::getId, accountId).eq(Account::getDeleted, 0).last("limit 1"));
    }

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }

    private String sanitizeRequestParams(HttpServletRequest request,
                                         ProceedingJoinPoint joinPoint,
                                         OperationLog operationLog,
                                         String queryRequestParams) {
        if (request != null && "GET".equalsIgnoreCase(request.getMethod())) {
            return queryRequestParams != null
                    ? queryRequestParams
                    : sanitizer.sanitizeRequestParameters(request, sanitizer.requestParamsMaxChars());
        }
        return sanitizer.sanitizeArgs(joinPoint.getArgs(), operationLog.excludeParamNames());
    }

    private boolean isExplicitQuery(HttpServletRequest request) {
        return request != null && "true".equalsIgnoreCase(request.getParameter(EXPLICIT_QUERY_PARAM));
    }

    private String sanitizedQueryRequestParams(HttpServletRequest request, OperationLog operationLog) {
        String[] queryParamNames = operationLog.queryParamNames();
        if (request == null || queryParamNames.length == 0) {
            return null;
        }
        Map<String, Object> params = new LinkedHashMap<>();
        for (String key : queryParamNames) {
            if (!StringUtils.hasText(key)) {
                continue;
            }
            String[] values = request.getParameterValues(key);
            Object value = meaningfulValue(values);
            if (value != null) {
                params.put(key, value);
            }
        }
        return params.isEmpty() ? null : sanitizer.sanitizeObject(params, sanitizer.requestParamsMaxChars());
    }

    private Object meaningfulValue(String[] values) {
        if (values == null || values.length == 0) {
            return null;
        }
        if (values.length == 1) {
            String value = values[0];
            return StringUtils.hasText(value) ? value : null;
        }
        List<String> meaningfulValues = Arrays.stream(values)
                .filter(StringUtils::hasText)
                .toList();
        return meaningfulValues.isEmpty() ? null : meaningfulValues;
    }
}
