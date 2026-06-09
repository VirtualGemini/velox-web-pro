package com.velox.module.system.log.support;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Locale;

@Component
public class UserAgentResolver {

    public UserAgentInfo resolve(String userAgent) {
        UserAgentInfo info = new UserAgentInfo();
        if (!StringUtils.hasText(userAgent)) {
            info.setBrowser("Unknown");
            info.setOs("Unknown");
            info.setDeviceType("Unknown");
            return info;
        }
        String ua = userAgent.toLowerCase(Locale.ROOT);
        info.setBrowser(browser(ua));
        info.setOs(os(ua));
        info.setDeviceType(device(ua));
        return info;
    }

    private String browser(String ua) {
        if (ua.contains("edg/")) return "Edge";
        if (ua.contains("chrome/")) return "Chrome";
        if (ua.contains("firefox/")) return "Firefox";
        if (ua.contains("safari/") && !ua.contains("chrome/")) return "Safari";
        if (ua.contains("msie") || ua.contains("trident")) return "IE";
        return "Unknown";
    }

    private String os(String ua) {
        if (ua.contains("windows")) return "Windows";
        if (ua.contains("mac os x")) return "macOS";
        if (ua.contains("android")) return "Android";
        if (ua.contains("iphone") || ua.contains("ipad")) return "iOS";
        if (ua.contains("linux")) return "Linux";
        return "Unknown";
    }

    private String device(String ua) {
        if (ua.contains("mobile") || ua.contains("iphone") || ua.contains("android")) return "Mobile";
        if (ua.contains("ipad") || ua.contains("tablet")) return "Tablet";
        return "Desktop";
    }
}
