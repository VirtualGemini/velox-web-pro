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
import com.velox.module.system.log.domain.model.ApiLogRecord;
import com.velox.module.system.log.dto.ApiLogDTO;
import com.velox.module.system.log.dto.ApiLogQuery;
import com.velox.module.system.log.persistence.ApiLogMapper;
import com.velox.module.system.log.service.ApiLogService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApiLogServiceImpl implements ApiLogService {

    private final ApiLogMapper apiLogMapper;
    private final SystemEntityIdGenerator entityIdGenerator;
    private final SystemFrontendIdCodecSupport frontendIdCodecSupport;

    public ApiLogServiceImpl(ApiLogMapper apiLogMapper,
                             SystemEntityIdGenerator entityIdGenerator,
                             SystemFrontendIdCodecSupport frontendIdCodecSupport) {
        this.apiLogMapper = apiLogMapper;
        this.entityIdGenerator = entityIdGenerator;
        this.frontendIdCodecSupport = frontendIdCodecSupport;
    }

    @Override
    public PageResult<ApiLogDTO> list(ApiLogQuery query) {
        LambdaQueryWrapper<ApiLogRecord> wrapper = wrapper(query);
        wrapper.orderByDesc(ApiLogRecord::getApiTime).orderByDesc(ApiLogRecord::getCreateTime);
        Page<ApiLogRecord> page = apiLogMapper.selectPage(new Page<>(query.page(), query.size()), wrapper);
        return PageResult.of(
                page.getTotal(),
                page.getCurrent(),
                page.getSize(),
                page.getRecords().stream().map(this::toListDTO).collect(Collectors.toList())
        );
    }

    @Override
    public ApiLogDTO get(String id) {
        String decodedId = frontendIdCodecSupport.decodeIdentifier(id);
        ApiLogRecord record = apiLogMapper.selectOne(new LambdaQueryWrapper<ApiLogRecord>().eq(ApiLogRecord::getId, decodedId).eq(ApiLogRecord::getDeleted, 0));
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
        return apiLogMapper.update(new LambdaUpdateWrapper<ApiLogRecord>().in(ApiLogRecord::getId, decodedIds).set(ApiLogRecord::getDeleted, 1)) >= 0;
    }

    @Override
    public boolean clean() {
        return apiLogMapper.update(new LambdaUpdateWrapper<ApiLogRecord>().eq(ApiLogRecord::getDeleted, 0).set(ApiLogRecord::getDeleted, 1)) >= 0;
    }

    @Override
    public void record(ApiLogRecord record) {
        if (record.getId() == null) record.setId(entityIdGenerator.nextId(ApiLogRecord.class));
        if (record.getDeleted() == null) record.setDeleted(0);
        apiLogMapper.insert(record);
    }

    @Override
    public List<Integer> getHttpStatuses() {
        return apiLogMapper.selectList(new LambdaQueryWrapper<ApiLogRecord>()
                        .select(ApiLogRecord::getHttpStatus)
                        .eq(ApiLogRecord::getDeleted, 0)
                        .isNotNull(ApiLogRecord::getHttpStatus)
                        .groupBy(ApiLogRecord::getHttpStatus)
                        .orderByAsc(ApiLogRecord::getHttpStatus))
                .stream()
                .map(ApiLogRecord::getHttpStatus)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    private LambdaQueryWrapper<ApiLogRecord> wrapper(ApiLogQuery query) {
        LambdaQueryWrapper<ApiLogRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiLogRecord::getDeleted, 0);
        eq(wrapper, ApiLogRecord::getRequestMethod, query.getRequestMethod());
        like(wrapper, ApiLogRecord::getRequestUri, query.getRequestUri());
        if (query.getHttpStatus() != null) wrapper.eq(ApiLogRecord::getHttpStatus, query.getHttpStatus());
        eq(wrapper, ApiLogRecord::getBusinessCode, query.getBusinessCode());
        if (query.getResult() != null) wrapper.eq(ApiLogRecord::getResult, query.getResult());
        like(wrapper, ApiLogRecord::getUsername, query.getUsername());
        eq(wrapper, ApiLogRecord::getAccountId, frontendIdCodecSupport.decodeIdentifier(query.getAccountId()));
        like(wrapper, ApiLogRecord::getClientIp, query.getClientIp());
        like(wrapper, ApiLogRecord::getCountryName, query.getCountryName());
        like(wrapper, ApiLogRecord::getProvinceName, query.getProvinceName());
        like(wrapper, ApiLogRecord::getCityName, query.getCityName());
        eq(wrapper, ApiLogRecord::getTraceId, query.getTraceId());
        if (query.getCostTimeMin() != null) wrapper.ge(ApiLogRecord::getCostTimeMs, query.getCostTimeMin());
        if (query.getCostTimeMax() != null) wrapper.le(ApiLogRecord::getCostTimeMs, query.getCostTimeMax());
        if (StringUtils.hasText(query.getApiTimeStart())) wrapper.ge(ApiLogRecord::getApiTime, RequestDateTimeFormatter.parseToUtc(query.getApiTimeStart()));
        if (StringUtils.hasText(query.getApiTimeEnd())) wrapper.le(ApiLogRecord::getApiTime, RequestDateTimeFormatter.parseToUtc(query.getApiTimeEnd()));
        if (StringUtils.hasText(query.getCreateTimeStart())) wrapper.ge(ApiLogRecord::getCreateTime, RequestDateTimeFormatter.parseToUtc(query.getCreateTimeStart()));
        if (StringUtils.hasText(query.getCreateTimeEnd())) wrapper.le(ApiLogRecord::getCreateTime, RequestDateTimeFormatter.parseToUtc(query.getCreateTimeEnd()));
        return wrapper;
    }

    private <T> void eq(LambdaQueryWrapper<ApiLogRecord> wrapper, SFunction<ApiLogRecord, T> column, String value) {
        if (StringUtils.hasText(value)) {
            wrapper.eq(column, value.trim());
        }
    }

    private <T> void like(LambdaQueryWrapper<ApiLogRecord> wrapper, SFunction<ApiLogRecord, T> column, String value) {
        if (StringUtils.hasText(value)) {
            wrapper.like(column, value.trim());
        }
    }

    private ApiLogDTO toDTO(ApiLogRecord r) {
        ApiLogDTO dto = new ApiLogDTO();
        dto.setId(r.getId());
        dto.setCreateTime(r.getCreateTime());
        dto.setAccountId(r.getAccountId());
        dto.setUsername(r.getUsername());
        dto.setCallerApp(r.getCallerApp());
        dto.setRequestUrl(r.getRequestUrl());
        dto.setRequestMethod(r.getRequestMethod());
        dto.setRequestUri(r.getRequestUri());
        dto.setMatchedPattern(r.getMatchedPattern());
        dto.setJavaMethod(r.getJavaMethod());
        dto.setHttpStatus(r.getHttpStatus());
        dto.setBusinessCode(r.getBusinessCode());
        dto.setBusinessMessage(r.getBusinessMessage());
        dto.setResult(r.getResult());
        dto.setRequestQuery(r.getRequestQuery());
        dto.setRequestHeaders(r.getRequestHeaders());
        dto.setRequestBody(r.getRequestBody());
        dto.setResponseBody(r.getResponseBody());
        dto.setRequestSize(r.getRequestSize());
        dto.setResponseSize(r.getResponseSize());
        dto.setErrorCode(r.getErrorCode());
        dto.setErrorMessage(r.getErrorMessage());
        dto.setExceptionStack(r.getExceptionStack());
        dto.setServerIp(r.getServerIp());
        dto.setServerNode(r.getServerNode());
        dto.setRequestTime(r.getRequestTime());
        dto.setResponseTime(r.getResponseTime());
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
        dto.setApiTime(r.getApiTime());
        return dto;
    }

    private ApiLogDTO toListDTO(ApiLogRecord record) {
        ApiLogDTO dto = toDTO(record);
        dto.setMatchedPattern(null);
        dto.setJavaMethod(null);
        dto.setBusinessMessage(null);
        dto.setRequestQuery(null);
        dto.setRequestHeaders(null);
        dto.setRequestBody(null);
        dto.setResponseBody(null);
        dto.setErrorCode(null);
        dto.setErrorMessage(null);
        dto.setExceptionStack(null);
        dto.setUserAgent(null);
        return dto;
    }
}
