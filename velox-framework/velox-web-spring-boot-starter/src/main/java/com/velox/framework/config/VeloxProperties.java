package com.velox.framework.config;

import com.velox.framework.web.properties.VeloxWebProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 应用属性配置
 */
@ConfigurationProperties(prefix = "velox")
public class VeloxProperties extends VeloxWebProperties {
}
