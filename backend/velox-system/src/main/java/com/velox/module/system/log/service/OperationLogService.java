package com.velox.module.system.log.service;

import com.velox.common.result.PageResult;
import com.velox.module.system.log.domain.model.OperationLogRecord;
import com.velox.module.system.log.dto.OperationLogDTO;
import com.velox.module.system.log.dto.OperationLogQuery;

import java.util.List;

public interface OperationLogService {
    PageResult<OperationLogDTO> list(OperationLogQuery query);
    OperationLogDTO get(String id);
    boolean deleteBatch(List<String> ids);
    boolean clean();
    void record(OperationLogRecord record);
}
