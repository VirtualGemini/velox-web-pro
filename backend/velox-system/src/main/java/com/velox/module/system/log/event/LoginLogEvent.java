package com.velox.module.system.log.event;

import com.velox.module.system.log.domain.model.LoginLogRecord;

public class LoginLogEvent {
    private final LoginLogRecord record;
    public LoginLogEvent(LoginLogRecord record) { this.record = record; }
    public LoginLogRecord getRecord() { return record; }
}
