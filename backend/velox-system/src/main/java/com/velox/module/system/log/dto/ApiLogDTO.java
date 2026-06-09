package com.velox.module.system.log.dto;

import java.time.LocalDateTime;

public class ApiLogDTO {
    private String id;
    private LocalDateTime createTime;
    private String accountId;
    private String username;
    private String callerApp;
    private String requestUrl;
    private String requestMethod;
    private String requestUri;
    private String matchedPattern;
    private String javaMethod;
    private Integer httpStatus;
    private String businessCode;
    private String businessMessage;
    private Integer result;
    private String requestQuery;
    private String requestHeaders;
    private String requestBody;
    private String responseBody;
    private Long requestSize;
    private Long responseSize;
    private String errorCode;
    private String errorMessage;
    private String exceptionStack;
    private String serverIp;
    private String serverNode;
    private LocalDateTime requestTime;
    private LocalDateTime responseTime;
    private Long costTimeMs;
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
    private LocalDateTime apiTime;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getCallerApp() { return callerApp; }
    public void setCallerApp(String callerApp) { this.callerApp = callerApp; }
    public String getRequestUrl() { return requestUrl; }
    public void setRequestUrl(String requestUrl) { this.requestUrl = requestUrl; }
    public String getRequestMethod() { return requestMethod; }
    public void setRequestMethod(String requestMethod) { this.requestMethod = requestMethod; }
    public String getRequestUri() { return requestUri; }
    public void setRequestUri(String requestUri) { this.requestUri = requestUri; }
    public String getMatchedPattern() { return matchedPattern; }
    public void setMatchedPattern(String matchedPattern) { this.matchedPattern = matchedPattern; }
    public String getJavaMethod() { return javaMethod; }
    public void setJavaMethod(String javaMethod) { this.javaMethod = javaMethod; }
    public Integer getHttpStatus() { return httpStatus; }
    public void setHttpStatus(Integer httpStatus) { this.httpStatus = httpStatus; }
    public String getBusinessCode() { return businessCode; }
    public void setBusinessCode(String businessCode) { this.businessCode = businessCode; }
    public String getBusinessMessage() { return businessMessage; }
    public void setBusinessMessage(String businessMessage) { this.businessMessage = businessMessage; }
    public Integer getResult() { return result; }
    public void setResult(Integer result) { this.result = result; }
    public String getRequestQuery() { return requestQuery; }
    public void setRequestQuery(String requestQuery) { this.requestQuery = requestQuery; }
    public String getRequestHeaders() { return requestHeaders; }
    public void setRequestHeaders(String requestHeaders) { this.requestHeaders = requestHeaders; }
    public String getRequestBody() { return requestBody; }
    public void setRequestBody(String requestBody) { this.requestBody = requestBody; }
    public String getResponseBody() { return responseBody; }
    public void setResponseBody(String responseBody) { this.responseBody = responseBody; }
    public Long getRequestSize() { return requestSize; }
    public void setRequestSize(Long requestSize) { this.requestSize = requestSize; }
    public Long getResponseSize() { return responseSize; }
    public void setResponseSize(Long responseSize) { this.responseSize = responseSize; }
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public String getExceptionStack() { return exceptionStack; }
    public void setExceptionStack(String exceptionStack) { this.exceptionStack = exceptionStack; }
    public String getServerIp() { return serverIp; }
    public void setServerIp(String serverIp) { this.serverIp = serverIp; }
    public String getServerNode() { return serverNode; }
    public void setServerNode(String serverNode) { this.serverNode = serverNode; }
    public LocalDateTime getRequestTime() { return requestTime; }
    public void setRequestTime(LocalDateTime requestTime) { this.requestTime = requestTime; }
    public LocalDateTime getResponseTime() { return responseTime; }
    public void setResponseTime(LocalDateTime responseTime) { this.responseTime = responseTime; }
    public Long getCostTimeMs() { return costTimeMs; }
    public void setCostTimeMs(Long costTimeMs) { this.costTimeMs = costTimeMs; }
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
    public LocalDateTime getApiTime() { return apiTime; }
    public void setApiTime(LocalDateTime apiTime) { this.apiTime = apiTime; }
}
