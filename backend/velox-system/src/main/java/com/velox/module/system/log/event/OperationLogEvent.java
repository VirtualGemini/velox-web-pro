package com.velox.module.system.log.event;

import com.velox.module.system.log.domain.model.OperationLogRecord;

public class OperationLogEvent {
    private final OperationLogRecord record;
    public OperationLogEvent(OperationLogRecord record) { this.record = record; }
    public OperationLogRecord getRecord() { return record; }
}
