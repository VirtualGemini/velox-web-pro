package com.velox.framework.id.properties;

import cn.hutool.core.lang.Snowflake;
import com.velox.framework.id.common.message.IdGeneratorCommonMessages;
import com.velox.framework.id.common.type.IdDatabaseInitModes;
import com.velox.framework.id.common.type.IdGeneratorStrategies;
import com.velox.framework.id.exception.VeloxIdGeneratorException;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = VeloxIdProperties.PREFIX)
public class VeloxIdProperties {

    public static final String PREFIX = "velox.id";
    public static final String ENABLED_KEY = "enabled";
    public static final String ENABLED_TRUE = "true";
    public static final String ENABLED_FALSE = "false";

    /**
     * Whether the id generator capability is enabled.
     */
    private boolean enabled = true;

    /**
     * Application-side id strategy. Defaults to snowflake.
     */
    private String strategy = IdGeneratorStrategies.SNOWFLAKE;

    /**
     * Database-default id governance.
     */
    private final DatabaseProperties database = new DatabaseProperties();

    /**
     * Snowflake strategy configuration.
     */
    private final SnowflakeProperties snowflake = new SnowflakeProperties();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public DatabaseProperties getDatabase() {
        return database;
    }

    public SnowflakeProperties getSnowflake() {
        return snowflake;
    }

    public void validate() {
        strategy = IdGeneratorStrategies.normalize(strategy);
        if (!IdGeneratorStrategies.isSupported(strategy)) {
            throw new VeloxIdGeneratorException(IdGeneratorCommonMessages.unsupportedIdStrategy(strategy));
        }
        database.validate();
        if (IdGeneratorStrategies.SNOWFLAKE.equals(strategy)) {
            snowflake.validate();
        }
    }

    public boolean isSnowflakeStrategy() {
        return IdGeneratorStrategies.SNOWFLAKE.equals(IdGeneratorStrategies.normalize(strategy));
    }

    public boolean isUuidStrategy() {
        return IdGeneratorStrategies.UUID.equals(IdGeneratorStrategies.normalize(strategy));
    }

    public static class DatabaseProperties {

        /**
         * Whether startup should reconcile id database objects.
         */
        private String initMode = IdDatabaseInitModes.NONE;

        /**
         * Warn in prod when database metadata differs from current YAML posture.
         */
        private boolean warnOnProdMismatch = true;

        public String getInitMode() {
            return initMode;
        }

        public void setInitMode(String initMode) {
            this.initMode = initMode;
        }

        public boolean isWarnOnProdMismatch() {
            return warnOnProdMismatch;
        }

        public void setWarnOnProdMismatch(boolean warnOnProdMismatch) {
            this.warnOnProdMismatch = warnOnProdMismatch;
        }

        void validate() {
            initMode = IdDatabaseInitModes.normalize(initMode);
            if (!IdDatabaseInitModes.isSupported(initMode)) {
                throw new VeloxIdGeneratorException(IdGeneratorCommonMessages.unsupportedDatabaseInitMode(initMode));
            }
        }
    }

    public static class SnowflakeProperties {

        private long workerId = 1L;
        private long datacenterId = 1L;
        private long twepoch = Snowflake.DEFAULT_TWEPOCH;
        private long timeOffset = Snowflake.DEFAULT_TIME_OFFSET;
        private boolean useSystemClock;

        public long getWorkerId() {
            return workerId;
        }

        public void setWorkerId(long workerId) {
            this.workerId = workerId;
        }

        public long getDatacenterId() {
            return datacenterId;
        }

        public void setDatacenterId(long datacenterId) {
            this.datacenterId = datacenterId;
        }

        public long getTwepoch() {
            return twepoch;
        }

        public void setTwepoch(long twepoch) {
            this.twepoch = twepoch;
        }

        public long getTimeOffset() {
            return timeOffset;
        }

        public void setTimeOffset(long timeOffset) {
            this.timeOffset = timeOffset;
        }

        public boolean isUseSystemClock() {
            return useSystemClock;
        }

        public void setUseSystemClock(boolean useSystemClock) {
            this.useSystemClock = useSystemClock;
        }

        void validate() {
            if (workerId < 0 || workerId > Snowflake.MAX_WORKER_ID) {
                throw new VeloxIdGeneratorException(
                        IdGeneratorCommonMessages.snowflakeWorkerIdOutOfRange(Snowflake.MAX_WORKER_ID));
            }
            if (datacenterId < 0 || datacenterId > Snowflake.MAX_DATA_CENTER_ID) {
                throw new VeloxIdGeneratorException(
                        IdGeneratorCommonMessages.snowflakeDatacenterIdOutOfRange(Snowflake.MAX_DATA_CENTER_ID));
            }
            if (twepoch <= 0) {
                throw new VeloxIdGeneratorException(IdGeneratorCommonMessages.SNOWFLAKE_TWEPOCH_MUST_BE_POSITIVE);
            }
            if (timeOffset < 0) {
                throw new VeloxIdGeneratorException(
                        IdGeneratorCommonMessages.SNOWFLAKE_TIME_OFFSET_MUST_NOT_BE_NEGATIVE);
            }
        }
    }
}
