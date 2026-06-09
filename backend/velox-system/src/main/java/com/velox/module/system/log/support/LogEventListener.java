package com.velox.module.system.log.support;

import com.velox.module.system.log.event.ApiLogEvent;
import com.velox.module.system.log.event.LoginLogEvent;
import com.velox.module.system.log.event.OperationLogEvent;
import com.velox.module.system.log.service.ApiLogService;
import com.velox.module.system.log.service.LoginLogService;
import com.velox.module.system.log.service.OperationLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;

@Component
public class LogEventListener {

    private static final Logger log = LoggerFactory.getLogger(LogEventListener.class);

    private final Executor executor;
    private final LoginLogService loginLogService;
    private final OperationLogService operationLogService;
    private final ApiLogService apiLogService;

    public LogEventListener(@Qualifier("logTaskExecutor") Executor executor,
                            LoginLogService loginLogService,
                            OperationLogService operationLogService,
                            ApiLogService apiLogService) {
        this.executor = executor;
        this.loginLogService = loginLogService;
        this.operationLogService = operationLogService;
        this.apiLogService = apiLogService;
    }

    @EventListener
    public void onLoginLog(LoginLogEvent event) {
        execute(() -> loginLogService.record(event.getRecord()), "login");
    }

    @EventListener
    public void onOperationLog(OperationLogEvent event) {
        execute(() -> operationLogService.record(event.getRecord()), "operation");
    }

    @EventListener
    public void onApiLog(ApiLogEvent event) {
        execute(() -> apiLogService.record(event.getRecord()), "api");
    }

    private void execute(Runnable runnable, String type) {
        try {
            executor.execute(() -> {
                try {
                    runnable.run();
                } catch (Exception exception) {
                    log.warn("Failed to persist {} log", type, exception);
                }
            });
        } catch (Exception exception) {
            log.warn("Failed to submit {} log task", type, exception);
        }
    }
}
