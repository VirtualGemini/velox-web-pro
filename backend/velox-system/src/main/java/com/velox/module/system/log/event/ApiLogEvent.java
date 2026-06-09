package com.velox.module.system.log.event;

import com.velox.module.system.log.domain.model.ApiLogRecord;

public class ApiLogEvent {
    private final ApiLogRecord record;
    public ApiLogEvent(ApiLogRecord record) { this.record = record; }
    public ApiLogRecord getRecord() { return record; }
}
