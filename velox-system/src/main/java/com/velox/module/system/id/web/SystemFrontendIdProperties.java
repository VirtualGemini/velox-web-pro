package com.velox.module.system.id.web;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "velox.id.snowflake")
public class SystemFrontendIdProperties {

    /**
     * Whether snowflake ids should be exposed to the frontend as Base62 strings.
     */
    private boolean base62 = true;

    public boolean isBase62() {
        return base62;
    }

    public void setBase62(boolean base62) {
        this.base62 = base62;
    }
}
