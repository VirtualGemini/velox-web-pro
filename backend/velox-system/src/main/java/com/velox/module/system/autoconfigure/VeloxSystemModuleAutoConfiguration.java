package com.velox.module.system.autoconfigure;

import com.velox.module.system.auth.properties.SystemAccountSecurityProperties;
import com.velox.module.system.auth.properties.SystemAuthProperties;
import com.velox.module.system.log.config.SystemLogProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@EnableConfigurationProperties({
        SystemAuthProperties.class,
        SystemAccountSecurityProperties.class,
        SystemLogProperties.class
})
@Import(SystemModulePackageScan.class)
public class VeloxSystemModuleAutoConfiguration {
}
