package com.velox.module.system.log.dto;

public class ApiLogQuery extends LogPageQuery {
    private String requestMethod;
    private String requestUri;
    private Integer httpStatus;
    private String businessCode;
    private Integer result;
    private String username;
    private String accountId;
    private String clientIp;
    private String countryName;
    private String provinceName;
    private String cityName;
    private String traceId;
    private Long costTimeMin;
    private Long costTimeMax;
    private String apiTimeStart;
    private String apiTimeEnd;

    public String getRequestMethod() { return requestMethod; }
    public void setRequestMethod(String requestMethod) { this.requestMethod = requestMethod; }
    public String getRequestUri() { return requestUri; }
    public void setRequestUri(String requestUri) { this.requestUri = requestUri; }
    public Integer getHttpStatus() { return httpStatus; }
    public void setHttpStatus(Integer httpStatus) { this.httpStatus = httpStatus; }
    public String getBusinessCode() { return businessCode; }
    public void setBusinessCode(String businessCode) { this.businessCode = businessCode; }
    public Integer getResult() { return result; }
    public void setResult(Integer result) { this.result = result; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }
    public String getClientIp() { return clientIp; }
    public void setClientIp(String clientIp) { this.clientIp = clientIp; }
    public String getCountryName() { return countryName; }
    public void setCountryName(String countryName) { this.countryName = countryName; }
    public String getProvinceName() { return provinceName; }
    public void setProvinceName(String provinceName) { this.provinceName = provinceName; }
    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
    public Long getCostTimeMin() { return costTimeMin; }
    public void setCostTimeMin(Long costTimeMin) { this.costTimeMin = costTimeMin; }
    public Long getCostTimeMax() { return costTimeMax; }
    public void setCostTimeMax(Long costTimeMax) { this.costTimeMax = costTimeMax; }
    public String getApiTimeStart() { return apiTimeStart; }
    public void setApiTimeStart(String apiTimeStart) { this.apiTimeStart = apiTimeStart; }
    public String getApiTimeEnd() { return apiTimeEnd; }
    public void setApiTimeEnd(String apiTimeEnd) { this.apiTimeEnd = apiTimeEnd; }
}
