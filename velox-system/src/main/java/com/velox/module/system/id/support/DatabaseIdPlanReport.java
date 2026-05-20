package com.velox.module.system.id.support;

import java.util.List;

public record DatabaseIdPlanReport(
        DatabaseIdPosture targetPosture,
        int managedTableCount,
        int identityDomainCount,
        int migrationCount,
        List<DatabaseIdPlanDiagnostic> diagnostics
) {

    public long warningCount() {
        return diagnostics.stream()
                .filter(diagnostic -> diagnostic.severity() == DatabaseIdDiagnosticSeverity.WARNING)
                .count();
    }

    public long errorCount() {
        return diagnostics.stream()
                .filter(diagnostic -> diagnostic.severity() == DatabaseIdDiagnosticSeverity.ERROR)
                .count();
    }
}
