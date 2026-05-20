package com.velox.module.system.id.support.graph;

import java.util.Map;

public record SchemaSnapshot(
        Map<String, DatabaseTableSnapshot> tables
) {
}
