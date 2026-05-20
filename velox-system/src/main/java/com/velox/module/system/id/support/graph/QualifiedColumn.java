package com.velox.module.system.id.support.graph;

public record QualifiedColumn(
        QualifiedTable table,
        String columnName
) {

    public String normalizedName() {
        return columnName == null ? "" : columnName.toLowerCase();
    }

    public String sqlName() {
        return table.sqlName() + "." + columnName;
    }
}
