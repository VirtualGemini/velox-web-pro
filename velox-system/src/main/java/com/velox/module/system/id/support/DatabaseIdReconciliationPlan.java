package com.velox.module.system.id.support;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record DatabaseIdReconciliationPlan(
        boolean hasDetectedData,
        boolean needsMigration,
        DatabaseIdPosture targetPosture,
        Map<String, DatabaseIdManagedTable> availableTables,
        Map<String, LinkedHashMap<String, String>> idMappings,
        List<DatabaseIdPlanDiagnostic> diagnostics
) {

    public boolean shouldBootstrap() {
        return !hasDetectedData || needsMigration;
    }
}
