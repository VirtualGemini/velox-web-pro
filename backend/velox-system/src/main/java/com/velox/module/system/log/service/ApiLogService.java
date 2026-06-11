package com.velox.module.system.log.service;

import com.velox.common.result.PageResult;
import com.velox.module.system.log.domain.model.ApiLogRecord;
import com.velox.module.system.log.dto.ApiLogDTO;
import com.velox.module.system.log.dto.ApiLogQuery;

import java.util.List;

public interface ApiLogService {
    PageResult<ApiLogDTO> list(ApiLogQuery query);
    ApiLogDTO get(String id);
    boolean deleteBatch(List<String> ids);
    boolean clean();
    void record(ApiLogRecord record);
    List<Integer> getHttpStatuses();
}
