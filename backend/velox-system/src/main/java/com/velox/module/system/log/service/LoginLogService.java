package com.velox.module.system.log.service;

import com.velox.common.result.PageResult;
import com.velox.module.system.log.domain.model.LoginLogRecord;
import com.velox.module.system.log.dto.LoginLogDTO;
import com.velox.module.system.log.dto.LoginLogQuery;

import java.util.List;

public interface LoginLogService {
    PageResult<LoginLogDTO> list(LoginLogQuery query);
    LoginLogDTO get(String id);
    boolean deleteBatch(List<String> ids);
    boolean clean();
    void record(LoginLogRecord record);
}
