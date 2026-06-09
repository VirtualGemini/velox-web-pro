package com.velox.module.system.log.support;

import com.velox.framework.web.api.tracing.TraceIdAccessor;
import com.velox.module.system.log.domain.model.ApiLogRecord;
import com.velox.module.system.log.domain.model.LoginLogRecord;
import com.velox.module.system.log.domain.model.OperationLogRecord;
import com.velox.module.system.log.ip.IpLocation;
import com.velox.module.system.log.ip.IpLocationResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class LogRecordEnricher {

    private final LogClientIpResolver clientIpResolver;
    private final IpLocationResolver ipLocationResolver;
    private final UserAgentResolver userAgentResolver;
    private final LogPayloadSanitizer sanitizer;

    public LogRecordEnricher(LogClientIpResolver clientIpResolver,
                             IpLocationResolver ipLocationResolver,
                             UserAgentResolver userAgentResolver,
                             LogPayloadSanitizer sanitizer) {
        this.clientIpResolver = clientIpResolver;
        this.ipLocationResolver = ipLocationResolver;
        this.userAgentResolver = userAgentResolver;
        this.sanitizer = sanitizer;
    }

    public void enrich(LoginLogRecord record, HttpServletRequest request) {
        String ip = clientIpResolver.resolve(request);
        String userAgent = request != null ? sanitizer.truncateUserAgent(request.getHeader("User-Agent")) : null;
        record.setTraceId(TraceIdAccessor.getCurrentTraceId());
        record.setClientIp(ip);
        record.setUserAgent(userAgent);
        applyLocation(record, ipLocationResolver.resolve(ip));
        UserAgentInfo info = userAgentResolver.resolve(userAgent);
        record.setBrowser(info.getBrowser()); record.setOs(info.getOs()); record.setDeviceType(info.getDeviceType());
    }

    public void enrich(OperationLogRecord record, HttpServletRequest request) {
        String ip = clientIpResolver.resolve(request);
        String userAgent = request != null ? sanitizer.truncateUserAgent(request.getHeader("User-Agent")) : null;
        record.setTraceId(TraceIdAccessor.getCurrentTraceId());
        record.setClientIp(ip);
        record.setUserAgent(userAgent);
        applyLocation(record, ipLocationResolver.resolve(ip));
        UserAgentInfo info = userAgentResolver.resolve(userAgent);
        record.setBrowser(info.getBrowser()); record.setOs(info.getOs()); record.setDeviceType(info.getDeviceType());
    }

    public void enrich(ApiLogRecord record, HttpServletRequest request) {
        String ip = clientIpResolver.resolve(request);
        String userAgent = request != null ? sanitizer.truncateUserAgent(request.getHeader("User-Agent")) : null;
        record.setTraceId(TraceIdAccessor.getCurrentTraceId());
        record.setClientIp(ip);
        record.setUserAgent(userAgent);
        applyLocation(record, ipLocationResolver.resolve(ip));
        UserAgentInfo info = userAgentResolver.resolve(userAgent);
        record.setBrowser(info.getBrowser()); record.setOs(info.getOs()); record.setDeviceType(info.getDeviceType());
    }

    private void applyLocation(LoginLogRecord record, IpLocation location) {
        if (location == null) return;
        record.setIpVersion(location.getIpVersion()); record.setCountryCode(location.getCountryCode()); record.setCountryName(location.getCountryName()); record.setProvinceName(location.getProvinceName()); record.setCityName(location.getCityName()); record.setDistrictName(location.getDistrictName()); record.setIpLocation(location.getLocationText()); record.setIsp(location.getIsp()); record.setLocationSource(location.getSource()); record.setLocationParsedAt(location.getParsedAt());
    }

    private void applyLocation(OperationLogRecord record, IpLocation location) {
        if (location == null) return;
        record.setIpVersion(location.getIpVersion()); record.setCountryCode(location.getCountryCode()); record.setCountryName(location.getCountryName()); record.setProvinceName(location.getProvinceName()); record.setCityName(location.getCityName()); record.setDistrictName(location.getDistrictName()); record.setIpLocation(location.getLocationText()); record.setIsp(location.getIsp()); record.setLocationSource(location.getSource()); record.setLocationParsedAt(location.getParsedAt());
    }

    private void applyLocation(ApiLogRecord record, IpLocation location) {
        if (location == null) return;
        record.setIpVersion(location.getIpVersion()); record.setCountryCode(location.getCountryCode()); record.setCountryName(location.getCountryName()); record.setProvinceName(location.getProvinceName()); record.setCityName(location.getCityName()); record.setDistrictName(location.getDistrictName()); record.setIpLocation(location.getLocationText()); record.setIsp(location.getIsp()); record.setLocationSource(location.getSource()); record.setLocationParsedAt(location.getParsedAt());
    }
}
