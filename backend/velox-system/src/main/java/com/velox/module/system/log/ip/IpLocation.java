package com.velox.module.system.log.ip;

import java.time.LocalDateTime;

public class IpLocation {
    private String ipVersion;
    private String countryCode;
    private String countryName;
    private String provinceName;
    private String cityName;
    private String isp;
    private String locationText;
    private String source;
    private LocalDateTime parsedAt;

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
    public String getIsp() { return isp; }
    public void setIsp(String isp) { this.isp = isp; }
    public String getLocationText() { return locationText; }
    public void setLocationText(String locationText) { this.locationText = locationText; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public LocalDateTime getParsedAt() { return parsedAt; }
    public void setParsedAt(LocalDateTime parsedAt) { this.parsedAt = parsedAt; }
}
