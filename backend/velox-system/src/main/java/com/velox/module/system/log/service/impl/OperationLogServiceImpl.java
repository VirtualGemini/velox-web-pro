package com.velox.module.system.log.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.velox.common.exception.ApiException;
import com.velox.common.exception.BusinessErrorCode;
import com.velox.common.result.PageResult;
import com.velox.framework.web.RequestDateTimeFormatter;
import com.velox.module.system.id.frontend.SystemFrontendIdCodecSupport;
import com.velox.module.system.id.generator.SystemEntityIdGenerator;
import com.velox.module.system.log.domain.model.OperationLogRecord;
import com.velox.module.system.log.dto.OperationLogDTO;
import com.velox.module.system.log.dto.OperationLogQuery;
import com.velox.module.system.log.persistence.OperationLogMapper;
import com.velox.module.system.log.service.OperationLogService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OperationLogServiceImpl implements OperationLogService {

    private final OperationLogMapper operationLogMapper;
    private final SystemEntityIdGenerator entityIdGenerator;
    private final SystemFrontendIdCodecSupport frontendIdCodecSupport;

    public OperationLogServiceImpl(OperationLogMapper operationLogMapper,
                                   SystemEntityIdGenerator entityIdGenerator,
                                   SystemFrontendIdCodecSupport frontendIdCodecSupport) {
        this.operationLogMapper = operationLogMapper;
        this.entityIdGenerator = entityIdGenerator;
        this.frontendIdCodecSupport = frontendIdCodecSupport;
    }

    @Override
    public PageResult<OperationLogDTO> list(OperationLogQuery query) {
        LambdaQueryWrapper<OperationLogRecord> wrapper = wrapper(query);
        wrapper.orderByDesc(OperationLogRecord::getOperationTime).orderByDesc(OperationLogRecord::getCreateTime);
        Page<OperationLogRecord> page = operationLogMapper.selectPage(new Page<>(query.page(), query.size()), wrapper);
        return PageResult.of(
                page.getTotal(),
                page.getCurrent(),
                page.getSize(),
                page.getRecords().stream().map(this::toListDTO).collect(Collectors.toList())
        );
    }

    @Override
    public OperationLogDTO get(String id) {
        String decodedId = frontendIdCodecSupport.decodeIdentifier(id);
        OperationLogRecord record = operationLogMapper.selectOne(new LambdaQueryWrapper<OperationLogRecord>().eq(OperationLogRecord::getId, decodedId).eq(OperationLogRecord::getDeleted, 0));
        if (record == null) {
            throw new ApiException(BusinessErrorCode.LOG_RECORD_NOT_FOUND);
        }
        return toDTO(record);
    }

    @Override
    public boolean deleteBatch(List<String> ids) {
        if (ids == null || ids.isEmpty()) throw new ApiException(BusinessErrorCode.LOG_DELETE_IDS_EMPTY);
        List<String> decodedIds = frontendIdCodecSupport.decodeIdentifiers(ids).stream()
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
        if (decodedIds.isEmpty()) {
            throw new ApiException(BusinessErrorCode.LOG_DELETE_IDS_EMPTY);
        }
        return operationLogMapper.update(new LambdaUpdateWrapper<OperationLogRecord>().in(OperationLogRecord::getId, decodedIds).set(OperationLogRecord::getDeleted, 1)) >= 0;
    }

    @Override
    public boolean clean() {
        return operationLogMapper.update(new LambdaUpdateWrapper<OperationLogRecord>().eq(OperationLogRecord::getDeleted, 0).set(OperationLogRecord::getDeleted, 1)) >= 0;
    }

    @Override
    public void record(OperationLogRecord record) {
        if (record.getId() == null) record.setId(entityIdGenerator.nextId(OperationLogRecord.class));
        if (record.getDeleted() == null) record.setDeleted(0);
        operationLogMapper.insert(record);
    }

    private LambdaQueryWrapper<OperationLogRecord> wrapper(OperationLogQuery query) {
        LambdaQueryWrapper<OperationLogRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OperationLogRecord::getDeleted, 0);
        like(wrapper, OperationLogRecord::getModuleName, query.getModuleName());
        like(wrapper, OperationLogRecord::getActionName, query.getActionName());
        eq(wrapper, OperationLogRecord::getOperationType, query.getOperationType());
        like(wrapper, OperationLogRecord::getUsername, query.getUsername());
        eq(wrapper, OperationLogRecord::getAccountId, frontendIdCodecSupport.decodeIdentifier(query.getAccountId()));
        if (query.getResult() != null) wrapper.eq(OperationLogRecord::getResult, query.getResult());
        like(wrapper, OperationLogRecord::getClientIp, query.getClientIp());
        like(wrapper, OperationLogRecord::getCountryName, query.getCountryName());
        like(wrapper, OperationLogRecord::getProvinceName, query.getProvinceName());
        like(wrapper, OperationLogRecord::getCityName, query.getCityName());
        eq(wrapper, OperationLogRecord::getTraceId, query.getTraceId());
        if (StringUtils.hasText(query.getOperationTimeStart())) wrapper.ge(OperationLogRecord::getOperationTime, RequestDateTimeFormatter.parseToUtc(query.getOperationTimeStart()));
        if (StringUtils.hasText(query.getOperationTimeEnd())) wrapper.le(OperationLogRecord::getOperationTime, RequestDateTimeFormatter.parseToUtc(query.getOperationTimeEnd()));
        return wrapper;
    }

    private <T> void eq(LambdaQueryWrapper<OperationLogRecord> wrapper, SFunction<OperationLogRecord, T> column, String value) {
        if (StringUtils.hasText(value)) {
            wrapper.eq(column, value.trim());
        }
    }

    private <T> void like(LambdaQueryWrapper<OperationLogRecord> wrapper, SFunction<OperationLogRecord, T> column, String value) {
        if (StringUtils.hasText(value)) {
            wrapper.like(column, value.trim());
        }
    }

    private OperationLogDTO toDTO(OperationLogRecord r) {
        OperationLogDTO dto = new OperationLogDTO();
        dto.setId(r.getId());
        dto.setCreateTime(r.getCreateTime());
        dto.setModuleName(r.getModuleName());
        dto.setActionName(r.getActionName());
        dto.setOperationType(r.getOperationType());
        dto.setTargetType(r.getTargetType());
        dto.setTargetId(r.getTargetId());
        dto.setAccountId(r.getAccountId());
        dto.setUsername(r.getUsername());
        dto.setOperatorType(r.getOperatorType());
        dto.setRequestMethod(r.getRequestMethod());
        dto.setRequestUri(r.getRequestUri());
        dto.setJavaMethod(r.getJavaMethod());
        dto.setRequestParams(r.getRequestParams());
        dto.setBeforeData(r.getBeforeData());
        dto.setAfterData(r.getAfterData());
        dto.setResponseSummary(r.getResponseSummary());
        dto.setResult(r.getResult());
        dto.setErrorCode(r.getErrorCode());
        dto.setErrorMessage(r.getErrorMessage());
        dto.setCostTimeMs(r.getCostTimeMs());
        dto.setTraceId(r.getTraceId());
        dto.setClientIp(r.getClientIp());
        dto.setIpVersion(r.getIpVersion());
        dto.setCountryCode(r.getCountryCode());
        dto.setCountryName(r.getCountryName());
        dto.setProvinceName(r.getProvinceName());
        dto.setCityName(r.getCityName());
        dto.setIpLocation(r.getIpLocation());
        dto.setIsp(r.getIsp());
        dto.setLocationSource(r.getLocationSource());
        dto.setLocationParsedAt(r.getLocationParsedAt());
        dto.setUserAgent(r.getUserAgent());
        dto.setBrowser(r.getBrowser());
        dto.setOs(r.getOs());
        dto.setDeviceType(r.getDeviceType());
        dto.setOperationTime(r.getOperationTime());
        return dto;
    }

    private OperationLogDTO toListDTO(OperationLogRecord record) {
        OperationLogDTO dto = toDTO(record);
        dto.setTargetType(null);
        dto.setTargetId(null);
        dto.setOperatorType(null);
        dto.setJavaMethod(null);
        dto.setRequestParams(null);
        dto.setBeforeData(null);
        dto.setAfterData(null);
        dto.setResponseSummary(null);
        dto.setErrorCode(null);
        dto.setErrorMessage(null);
        dto.setUserAgent(null);
        return dto;
    }
}
