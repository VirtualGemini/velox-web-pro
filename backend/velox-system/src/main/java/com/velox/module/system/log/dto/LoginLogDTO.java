package com.velox.module.system.log.dto;

import java.time.LocalDateTime;

public class LoginLogDTO {
    private String id;
    private LocalDateTime createTime;
    private String accountId;
    private String username;
    private String eventType;
    private String loginMethod;
    private String mfaType;
    private Integer result;
    private String failureCode;
    private String failureMessage;
    private String sessionId;
    private String traceId;
    private String clientIp;
    private String ipVersion;
    private String countryCode;
    private String countryName;
    private String provinceName;
    private String cityName;
    private String districtName;
    private String ipLocation;
    private String isp;
    private String locationSource;
    private LocalDateTime locationParsedAt;
    private String userAgent;
    private String browser;
    private String os;
    private String deviceType;
    private LocalDateTime eventTime;
    private LocalDateTime logoutTime;
    private Integer firstLogin;
    private String riskType;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getLoginMethod() { return loginMethod; }
    public void setLoginMethod(String loginMethod) { this.loginMethod = loginMethod; }
    public String getMfaType() { return mfaType; }
    public void setMfaType(String mfaType) { this.mfaType = mfaType; }
    public Integer getResult() { return result; }
    public void setResult(Integer result) { this.result = result; }
    public String getFailureCode() { return failureCode; }
    public void setFailureCode(String failureCode) { this.failureCode = failureCode; }
    public String getFailureMessage() { return failureMessage; }
    public void setFailureMessage(String failureMessage) { this.failureMessage = failureMessage; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
    public String getClientIp() { return clientIp; }
    public void setClientIp(String clientIp) { this.clientIp = clientIp; }
    public String getIpVersion() { return ipVersion; }
    public void setIpVersion(String ipVersion) { this.ipVersion = ipVersion; }
    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
    public String getCountryName() { return countryName; }
    public void setCountryName(String countryName) { this.countryName = countryName; }
    public String getProvinceName() { return provinceName; }
    public void setProvinceName(String provinceName) { this.provinceName = provinceName; }
    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }
    public String getDistrictName() { return districtName; }
    public void setDistrictName(String districtName) { this.districtName = districtName; }
    public String getIpLocation() { return ipLocation; }
    public void setIpLocation(String ipLocation) { this.ipLocation = ipLocation; }
    public String getIsp() { return isp; }
    public void setIsp(String isp) { this.isp = isp; }
    public String getLocationSource() { return locationSource; }
    public void setLocationSource(String locationSource) { this.locationSource = locationSource; }
    public LocalDateTime getLocationParsedAt() { return locationParsedAt; }
    public void setLocationParsedAt(LocalDateTime locationParsedAt) { this.locationParsedAt = locationParsedAt; }
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public String getBrowser() { return browser; }
    public void setBrowser(String browser) { this.browser = browser; }
    public String getOs() { return os; }
    public void setOs(String os) { this.os = os; }
    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
    public LocalDateTime getEventTime() { return eventTime; }
    public void setEventTime(LocalDateTime eventTime) { this.eventTime = eventTime; }
    public LocalDateTime getLogoutTime() { return logoutTime; }
    public void setLogoutTime(LocalDateTime logoutTime) { this.logoutTime = logoutTime; }
    public Integer getFirstLogin() { return firstLogin; }
    public void setFirstLogin(Integer firstLogin) { this.firstLogin = firstLogin; }
    public String getRiskType() { return riskType; }
    public void setRiskType(String riskType) { this.riskType = riskType; }
}
