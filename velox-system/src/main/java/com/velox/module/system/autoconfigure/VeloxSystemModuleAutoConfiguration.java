package com.velox.module.system.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import(SystemModulePackageScan.class)
public class VeloxSystemModuleAutoConfiguration {
}
