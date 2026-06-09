package com.velox.module.system.log.dto;

public class LoginLogQuery extends LogPageQuery {
    private String username;
    private String accountId;
    private String eventType;
    private String loginMethod;
    private String mfaType;
    private Integer result;
    private String failureCode;
    private String clientIp;
    private String countryName;
    private String provinceName;
    private String cityName;
    private String districtName;
    private String traceId;
    private String eventTimeStart;
    private String eventTimeEnd;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }
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
    public String getClientIp() { return clientIp; }
    public void setClientIp(String clientIp) { this.clientIp = clientIp; }
    public String getCountryName() { return countryName; }
    public void setCountryName(String countryName) { this.countryName = countryName; }
    public String getProvinceName() { return provinceName; }
    public void setProvinceName(String provinceName) { this.provinceName = provinceName; }
    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }
    public String getDistrictName() { return districtName; }
    public void setDistrictName(String districtName) { this.districtName = districtName; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
    public String getEventTimeStart() { return eventTimeStart; }
    public void setEventTimeStart(String eventTimeStart) { this.eventTimeStart = eventTimeStart; }
    public String getEventTimeEnd() { return eventTimeEnd; }
    public void setEventTimeEnd(String eventTimeEnd) { this.eventTimeEnd = eventTimeEnd; }
}
