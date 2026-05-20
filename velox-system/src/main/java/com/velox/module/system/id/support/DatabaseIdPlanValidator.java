package com.velox.module.system.id.support;

import com.velox.framework.id.exception.VeloxIdGeneratorException;
import com.velox.module.system.id.support.graph.DatabaseColumnSnapshot;
import com.velox.module.system.id.support.graph.DatabaseTableSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DatabaseIdPlanValidator {

    public DatabaseIdPlanValidationResult inspect(DatabaseIdPlanningContext context) {
        List<DatabaseIdPlanDiagnostic> diagnostics = new ArrayList<>();
        Map<String, DatabaseIdManagedTable> targetDomains = context.identityDomainsByBusinessType();
        for (DatabaseIdManagedTable table : context.managedTables().values()) {
            DatabaseTableSnapshot tableSnapshot = requireTable(context, table.tableName());
            if (table.identityColumn() != null) {
                DatabaseColumnSnapshot identityColumn = requireColumn(tableSnapshot, table.identityColumn());
                if (!identityColumn.isManageableScalar()) {
                    diagnostics.add(error("IDENTITY_NOT_SCALAR", location(table.tableName(), table.identityColumn()),
                            "Managed identity column must be scalar"));
                }
                if (DatabaseIdDiscoverySupport.isPathColumn(identityColumn.normalizedColumnName())) {
                    diagnostics.add(error("IDENTITY_PATH_CONTAINER", location(table.tableName(), table.identityColumn()),
                            "Managed identity column must not be a path container"));
                }
            }
            for (DatabaseIdColumnBinding binding : table.bindings()) {
                if (!targetDomains.containsKey(binding.targetBusinessType())) {
                    diagnostics.add(error("TARGET_DOMAIN_MISSING", location(table.tableName(), binding.columnName()),
                            "Managed binding target business type is missing: " + binding.targetBusinessType()));
                    continue;
                }
                DatabaseColumnSnapshot column = requireColumn(tableSnapshot, binding.columnName());
                if (binding.pathLike()) {
                    if (!column.isCharacterLike()) {
                        diagnostics.add(error("PATH_BINDING_NOT_CHARACTER", location(table.tableName(), binding.columnName()),
                                "Path binding column must stay character-like"));
                    }
                    continue;
                }
                if (!column.isManageableScalar()) {
                    diagnostics.add(error("SCALAR_BINDING_NOT_MANAGEABLE", location(table.tableName(), binding.columnName()),
                            "Scalar binding column must be manageable scalar"));
                }
                if (DatabaseIdDiscoverySupport.isPathColumn(column.normalizedColumnName())) {
                    diagnostics.add(error("SCALAR_BINDING_PATH_LIKE", location(table.tableName(), binding.columnName()),
                            "Path-like binding must not be treated as scalar"));
                }
                if (binding.kind() == DatabaseIdReferenceKind.AUDIT_REFERENCE) {
                    diagnostics.add(warning("AUDIT_REFERENCE", location(table.tableName(), binding.columnName()),
                            "Audit reference is managed through weak evidence"));
                } else if (binding.kind() == DatabaseIdReferenceKind.WEAK_REFERENCE) {
                    diagnostics.add(warning("WEAK_REFERENCE", location(table.tableName(), binding.columnName()),
                            "Reference is managed through weak evidence"));
                }
            }
        }
        return new DatabaseIdPlanValidationResult(List.copyOf(diagnostics));
    }

    public void validate(DatabaseIdPlanningContext context) {
        DatabaseIdPlanValidationResult result = inspect(context);
        if (result.hasErrors()) {
            throw new VeloxIdGeneratorException("Database id plan validation failed: " + result.renderErrors());
        }
    }

    private DatabaseTableSnapshot requireTable(DatabaseIdPlanningContext context, String tableName) {
        DatabaseTableSnapshot table = context.table(tableName);
        if (table == null) {
            throw new VeloxIdGeneratorException("Managed table snapshot is missing: " + tableName);
        }
        return table;
    }

    private DatabaseColumnSnapshot requireColumn(DatabaseTableSnapshot table, String columnName) {
        DatabaseColumnSnapshot column = table.column(columnName);
        if (column == null) {
            throw new VeloxIdGeneratorException("Managed column snapshot is missing: "
                    + table.table().sqlName() + "." + columnName);
        }
        return column;
    }

    private DatabaseIdPlanDiagnostic error(String code, String location, String message) {
        return new DatabaseIdPlanDiagnostic(DatabaseIdDiagnosticSeverity.ERROR, code, location, message);
    }

    private DatabaseIdPlanDiagnostic warning(String code, String location, String message) {
        return new DatabaseIdPlanDiagnostic(DatabaseIdDiagnosticSeverity.WARNING, code, location, message);
    }

    private String location(String tableName, String columnName) {
        return tableName + "." + columnName;
    }
}
