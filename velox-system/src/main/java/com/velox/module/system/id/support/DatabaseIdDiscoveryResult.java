package com.velox.module.system.id.support;

import java.util.List;
import java.util.Map;

public record DatabaseIdDiscoveryResult(
        Map<String, DatabaseIdManagedTable> managedTables,
        List<DatabaseIdPlanDiagnostic> diagnostics
) {
}
