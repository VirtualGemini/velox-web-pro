package com.velox.module.system.log.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.velox.common.exception.ApiException;
import com.velox.common.exception.BusinessErrorCode;
import com.velox.common.result.PageResult;
import com.velox.framework.web.RequestDateTimeFormatter;
import com.velox.module.system.id.frontend.SystemFrontendIdCodecSupport;
import com.velox.module.system.id.generator.SystemEntityIdGenerator;
import com.velox.module.system.log.domain.model.LoginLogRecord;
import com.velox.module.system.log.dto.LoginLogDTO;
import com.velox.module.system.log.dto.LoginLogQuery;
import com.velox.module.system.log.persistence.LoginLogMapper;
import com.velox.module.system.log.service.LoginLogService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LoginLogServiceImpl implements LoginLogService {

    private final LoginLogMapper loginLogMapper;
    private final SystemEntityIdGenerator entityIdGenerator;
    private final SystemFrontendIdCodecSupport frontendIdCodecSupport;

    public LoginLogServiceImpl(LoginLogMapper loginLogMapper,
                               SystemEntityIdGenerator entityIdGenerator,
                               SystemFrontendIdCodecSupport frontendIdCodecSupport) {
        this.loginLogMapper = loginLogMapper;
        this.entityIdGenerator = entityIdGenerator;
        this.frontendIdCodecSupport = frontendIdCodecSupport;
    }

    @Override
    public PageResult<LoginLogDTO> list(LoginLogQuery query) {
        LambdaQueryWrapper<LoginLogRecord> wrapper = wrapper(query);
        wrapper.orderByDesc(LoginLogRecord::getEventTime).orderByDesc(LoginLogRecord::getCreateTime);
        Page<LoginLogRecord> page = loginLogMapper.selectPage(new Page<>(query.page(), query.size()), wrapper);
        return PageResult.of(
                page.getTotal(),
                page.getCurrent(),
                page.getSize(),
                page.getRecords().stream().map(this::toListDTO).collect(Collectors.toList())
        );
    }

    @Override
    public LoginLogDTO get(String id) {
        String decodedId = frontendIdCodecSupport.decodeIdentifier(id);
        LoginLogRecord record = loginLogMapper.selectOne(new LambdaQueryWrapper<LoginLogRecord>().eq(LoginLogRecord::getId, decodedId).eq(LoginLogRecord::getDeleted, 0));
        if (record == null) {
            throw new ApiException(BusinessErrorCode.LOG_RECORD_NOT_FOUND);
        }
        return toDTO(record);
    }

    @Override
    public boolean deleteBatch(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new ApiException(BusinessErrorCode.LOG_DELETE_IDS_EMPTY);
        }
        List<String> decodedIds = frontendIdCodecSupport.decodeIdentifiers(ids).stream()
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
        if (decodedIds.isEmpty()) {
            throw new ApiException(BusinessErrorCode.LOG_DELETE_IDS_EMPTY);
        }
        return loginLogMapper.update(new LambdaUpdateWrapper<LoginLogRecord>().in(LoginLogRecord::getId, decodedIds).set(LoginLogRecord::getDeleted, 1)) >= 0;
    }

    @Override
    public boolean clean() {
        return loginLogMapper.update(new LambdaUpdateWrapper<LoginLogRecord>().eq(LoginLogRecord::getDeleted, 0).set(LoginLogRecord::getDeleted, 1)) >= 0;
    }

    @Override
    public void record(LoginLogRecord record) {
        if (record.getId() == null) {
            record.setId(entityIdGenerator.nextId(LoginLogRecord.class));
        }
        if (record.getDeleted() == null) {
            record.setDeleted(0);
        }
        loginLogMapper.insert(record);
    }

    private LambdaQueryWrapper<LoginLogRecord> wrapper(LoginLogQuery query) {
        LambdaQueryWrapper<LoginLogRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LoginLogRecord::getDeleted, 0);
        like(wrapper, LoginLogRecord::getUsername, query.getUsername());
        eq(wrapper, LoginLogRecord::getAccountId, frontendIdCodecSupport.decodeIdentifier(query.getAccountId()));
        eq(wrapper, LoginLogRecord::getEventType, query.getEventType());
        eq(wrapper, LoginLogRecord::getLoginMethod, query.getLoginMethod());
        eq(wrapper, LoginLogRecord::getMfaType, query.getMfaType());
        if (query.getResult() != null) wrapper.eq(LoginLogRecord::getResult, query.getResult());
        eq(wrapper, LoginLogRecord::getFailureCode, query.getFailureCode());
        like(wrapper, LoginLogRecord::getClientIp, query.getClientIp());
        like(wrapper, LoginLogRecord::getCountryName, query.getCountryName());
        like(wrapper, LoginLogRecord::getProvinceName, query.getProvinceName());
        like(wrapper, LoginLogRecord::getCityName, query.getCityName());
        eq(wrapper, LoginLogRecord::getTraceId, query.getTraceId());
        if (StringUtils.hasText(query.getEventTimeStart())) wrapper.ge(LoginLogRecord::getEventTime, RequestDateTimeFormatter.parseToUtc(query.getEventTimeStart()));
        if (StringUtils.hasText(query.getEventTimeEnd())) wrapper.le(LoginLogRecord::getEventTime, RequestDateTimeFormatter.parseToUtc(query.getEventTimeEnd()));
        return wrapper;
    }

    private <T> void eq(LambdaQueryWrapper<LoginLogRecord> wrapper, com.baomidou.mybatisplus.core.toolkit.support.SFunction<LoginLogRecord, T> column, String value) {
        if (StringUtils.hasText(value)) {
            wrapper.eq(column, value.trim());
        }
    }

    private <T> void like(LambdaQueryWrapper<LoginLogRecord> wrapper, com.baomidou.mybatisplus.core.toolkit.support.SFunction<LoginLogRecord, T> column, String value) {
        if (StringUtils.hasText(value)) {
            wrapper.like(column, value.trim());
        }
    }

    private LoginLogDTO toDTO(LoginLogRecord r) {
        LoginLogDTO dto = new LoginLogDTO();
        dto.setId(r.getId());
        dto.setCreateTime(r.getCreateTime());
        dto.setAccountId(r.getAccountId());
        dto.setUsername(r.getUsername());
        dto.setEventType(r.getEventType());
        dto.setLoginMethod(r.getLoginMethod());
        dto.setMfaType(r.getMfaType());
        dto.setResult(r.getResult());
        dto.setFailureCode(r.getFailureCode());
        dto.setFailureMessage(r.getFailureMessage());
        dto.setSessionId(r.getSessionId());
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
        dto.setEventTime(r.getEventTime());
        dto.setLogoutTime(r.getLogoutTime());
        dto.setFirstLogin(r.getFirstLogin());
        dto.setRiskType(r.getRiskType());
        return dto;
    }

    private LoginLogDTO toListDTO(LoginLogRecord record) {
        LoginLogDTO dto = toDTO(record);
        dto.setSessionId(null);
        dto.setUserAgent(null);
        return dto;
    }
}
