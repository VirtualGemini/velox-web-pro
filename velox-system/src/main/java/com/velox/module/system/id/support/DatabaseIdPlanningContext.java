package com.velox.module.system.id.support;

import com.velox.module.system.id.support.graph.DatabaseTableSnapshot;
import com.velox.module.system.id.support.graph.SchemaSnapshot;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public record DatabaseIdPlanningContext(
        SchemaSnapshot snapshot,
        Map<String, Set<String>> sampleCache,
        Map<String, DatabaseIdManagedTable> managedTables,
        List<DatabaseIdPlanDiagnostic> discoveryDiagnostics,
        DatabaseIdPlanValidationResult validationResult
) {

    public static DatabaseIdPlanningContext of(
            SchemaSnapshot snapshot,
            Map<String, Set<String>> sampleCache,
            Map<String, DatabaseIdManagedTable> managedTables
    ) {
        return new DatabaseIdPlanningContext(snapshot, sampleCache, managedTables, List.of(), new DatabaseIdPlanValidationResult(List.of()));
    }

    public DatabaseTableSnapshot table(String tableName) {
        return snapshot.tables().get(DatabaseIdDiscoverySupport.normalizeName(tableName));
    }

    public Set<String> samples(String tableName, String columnName) {
        DatabaseTableSnapshot table = table(tableName);
        if (table == null) {
            return Set.of();
        }
        return sampleCache.getOrDefault(DatabaseIdDiscoverySupport.sampleKey(table.table(), columnName), Set.of());
    }

    public Map<String, DatabaseIdManagedTable> identityDomainsByBusinessType() {
        return managedTables.values().stream()
                .filter(table -> table.identityColumn() != null)
                .collect(Collectors.toMap(
                        DatabaseIdManagedTable::identityMappingBusinessType,
                        table -> table,
                        (left, right) -> left
                ));
    }
}
