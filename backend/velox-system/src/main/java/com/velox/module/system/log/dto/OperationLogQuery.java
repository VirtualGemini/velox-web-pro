package com.velox.module.system.log.dto;

public class OperationLogQuery extends LogPageQuery {
    private String moduleName;
    private String actionName;
    private String operationType;
    private String username;
    private String accountId;
    private Integer result;
    private String clientIp;
    private String countryName;
    private String provinceName;
    private String cityName;
    private String traceId;
    private String operationTimeStart;
    private String operationTimeEnd;

    public String getModuleName() { return moduleName; }
    public void setModuleName(String moduleName) { this.moduleName = moduleName; }
    public String getActionName() { return actionName; }
    public void setActionName(String actionName) { this.actionName = actionName; }
    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }
    public Integer getResult() { return result; }
    public void setResult(Integer result) { this.result = result; }
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
    public String getOperationTimeStart() { return operationTimeStart; }
    public void setOperationTimeStart(String operationTimeStart) { this.operationTimeStart = operationTimeStart; }
    public String getOperationTimeEnd() { return operationTimeEnd; }
    public void setOperationTimeEnd(String operationTimeEnd) { this.operationTimeEnd = operationTimeEnd; }
}
