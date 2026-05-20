package com.velox.module.system.id.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "velox.id.database.governance")
public class SystemDatabaseIdGovernanceProperties {

    private boolean reportEnabled = true;
    private final List<ColumnIgnore> ignores = new ArrayList<>();
    private final List<ReferenceOverride> overrides = new ArrayList<>();

    public boolean isReportEnabled() {
        return reportEnabled;
    }

    public void setReportEnabled(boolean reportEnabled) {
        this.reportEnabled = reportEnabled;
    }

    public List<ReferenceOverride> getOverrides() {
        return overrides;
    }

    public List<ColumnIgnore> getIgnores() {
        return ignores;
    }

    public static class ColumnIgnore {

        private String table;
        private String column;
        private boolean enabled = true;

        public String getTable() {
            return table;
        }

        public void setTable(String table) {
            this.table = table;
        }

        public String getColumn() {
            return column;
        }

        public void setColumn(String column) {
            this.column = column;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class ReferenceOverride {

        private String table;
        private String column;
        private String targetBusinessType;
        private boolean pathLike;
        private boolean enabled = true;

        public String getTable() {
            return table;
        }

        public void setTable(String table) {
            this.table = table;
        }

        public String getColumn() {
            return column;
        }

        public void setColumn(String column) {
            this.column = column;
        }

        public String getTargetBusinessType() {
            return targetBusinessType;
        }

        public void setTargetBusinessType(String targetBusinessType) {
            this.targetBusinessType = targetBusinessType;
        }

        public boolean isPathLike() {
            return pathLike;
        }

        public void setPathLike(boolean pathLike) {
            this.pathLike = pathLike;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
