package com.velox.module.system.log.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "system.log")
public class SystemLogProperties {

    private final IpLocation ipLocation = new IpLocation();
    private final Payload payload = new Payload();
    private boolean trustProxyHeaders;

    public IpLocation getIpLocation() { return ipLocation; }
    public Payload getPayload() { return payload; }
    public boolean isTrustProxyHeaders() { return trustProxyHeaders; }
    public void setTrustProxyHeaders(boolean trustProxyHeaders) { this.trustProxyHeaders = trustProxyHeaders; }

    public static class IpLocation {
        private boolean enabled = true;
        private String provider = "ip2region";
        private String xdbPath;
        private String xdbV4Path;
        private String xdbV6Path;
        private int cacheSize = 10000;
        private String unknownText = "unknown";
        private String intranetText = "intranet";

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getProvider() { return provider; }
        public void setProvider(String provider) { this.provider = provider; }
        public String getXdbPath() { return xdbPath; }
        public void setXdbPath(String xdbPath) { this.xdbPath = xdbPath; }
        public String getXdbV4Path() { return xdbV4Path; }
        public void setXdbV4Path(String xdbV4Path) { this.xdbV4Path = xdbV4Path; }
        public String getXdbV6Path() { return xdbV6Path; }
        public void setXdbV6Path(String xdbV6Path) { this.xdbV6Path = xdbV6Path; }
        public int getCacheSize() { return cacheSize; }
        public void setCacheSize(int cacheSize) { this.cacheSize = cacheSize; }
        public String getUnknownText() { return unknownText; }
        public void setUnknownText(String unknownText) { this.unknownText = unknownText; }
        public String getIntranetText() { return intranetText; }
        public void setIntranetText(String intranetText) { this.intranetText = intranetText; }
    }

    public static class Payload {
        private boolean captureRequestBody;
        private boolean captureResponseBody;
        private int maxBodyBytes = 16384;
        private List<String> bodyIncludedPaths = List.of();
        private List<String> allowedContentTypes = List.of(
                "application/json",
                "text/plain",
                "application/xml",
                "text/xml",
                "application/x-www-form-urlencoded"
        );
        private List<String> excludedPaths = List.of();
        private int requestParamsMaxChars = 4000;
        private int responseBodyMaxChars = 4000;
        private int errorMessageMaxChars = 1000;
        private int exceptionStackMaxChars = 4000;
        private int userAgentMaxChars = 512;

        public boolean isCaptureRequestBody() { return captureRequestBody; }
        public void setCaptureRequestBody(boolean captureRequestBody) { this.captureRequestBody = captureRequestBody; }
        public boolean isCaptureResponseBody() { return captureResponseBody; }
        public void setCaptureResponseBody(boolean captureResponseBody) { this.captureResponseBody = captureResponseBody; }
        public int getMaxBodyBytes() { return maxBodyBytes; }
        public void setMaxBodyBytes(int maxBodyBytes) { this.maxBodyBytes = maxBodyBytes; }
        public List<String> getBodyIncludedPaths() { return bodyIncludedPaths; }
        public void setBodyIncludedPaths(List<String> bodyIncludedPaths) { this.bodyIncludedPaths = bodyIncludedPaths == null ? List.of() : bodyIncludedPaths; }
        public List<String> getAllowedContentTypes() { return allowedContentTypes; }
        public void setAllowedContentTypes(List<String> allowedContentTypes) { this.allowedContentTypes = allowedContentTypes == null ? List.of() : allowedContentTypes; }
        public List<String> getExcludedPaths() { return excludedPaths; }
        public void setExcludedPaths(List<String> excludedPaths) { this.excludedPaths = excludedPaths == null ? List.of() : excludedPaths; }
        public int getRequestParamsMaxChars() { return requestParamsMaxChars; }
        public void setRequestParamsMaxChars(int requestParamsMaxChars) { this.requestParamsMaxChars = requestParamsMaxChars; }
        public int getResponseBodyMaxChars() { return responseBodyMaxChars; }
        public void setResponseBodyMaxChars(int responseBodyMaxChars) { this.responseBodyMaxChars = responseBodyMaxChars; }
        public int getErrorMessageMaxChars() { return errorMessageMaxChars; }
        public void setErrorMessageMaxChars(int errorMessageMaxChars) { this.errorMessageMaxChars = errorMessageMaxChars; }
        public int getExceptionStackMaxChars() { return exceptionStackMaxChars; }
        public void setExceptionStackMaxChars(int exceptionStackMaxChars) { this.exceptionStackMaxChars = exceptionStackMaxChars; }
        public int getUserAgentMaxChars() { return userAgentMaxChars; }
        public void setUserAgentMaxChars(int userAgentMaxChars) { this.userAgentMaxChars = userAgentMaxChars; }
    }
}
