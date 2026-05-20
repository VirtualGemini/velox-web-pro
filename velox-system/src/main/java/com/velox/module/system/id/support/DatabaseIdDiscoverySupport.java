package com.velox.module.system.id.support;

import com.velox.module.system.id.properties.SystemDatabaseIdGovernanceProperties;
import com.velox.module.system.id.support.graph.DatabaseColumnSnapshot;
import com.velox.module.system.id.support.graph.DatabaseForeignKeySnapshot;
import com.velox.module.system.id.support.graph.DatabaseTableSnapshot;
import com.velox.module.system.id.support.graph.QualifiedTable;
import com.velox.module.system.id.support.graph.SchemaSnapshot;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

final class DatabaseIdDiscoverySupport {

    static final int LOW_CARDINALITY_THRESHOLD = 16;
    static final Pattern PATH_TOKEN_PATTERN = Pattern.compile("[^/,;|>\\s]+");
    static final Set<String> AUDIT_COLUMNS = Set.of(
            "create_by", "update_by", "created_by", "updated_by", "created_user_id", "updated_user_id", "owner_id"
    );
    static final Set<String> DENY_NAME_TOKENS = Set.of("type", "status", "level", "state", "category");
    static final Set<String> REFERENCE_SUFFIXES = Set.of("_id", "_by", "_ref");

    private DatabaseIdDiscoverySupport() {
    }

    static Map<String, DatabaseIdManagedTable> discoverManagedTables(
            SchemaSnapshot snapshot,
            Map<String, Set<String>> sampleCache,
            SystemDatabaseIdGovernanceProperties governanceProperties
    ) {
        return discover(snapshot, sampleCache, governanceProperties).managedTables();
    }

    static DatabaseIdDiscoveryResult discover(
            SchemaSnapshot snapshot,
            Map<String, Set<String>> sampleCache,
            SystemDatabaseIdGovernanceProperties governanceProperties
    ) {
        List<DatabaseIdPlanDiagnostic> diagnostics = new java.util.ArrayList<>();
        Map<String, DatabaseIdManagedTableBuilder> builders = discoverIdentityBuilders(snapshot, sampleCache);
        loadExplicitBindings(snapshot, builders);
        applyIgnores(snapshot, builders, diagnostics, governanceProperties);
        applyOverrides(builders, diagnostics, governanceProperties);
        loadWeakBindings(snapshot, sampleCache, builders, diagnostics, governanceProperties);
        Map<String, DatabaseIdManagedTable> managedTables = builders.values().stream()
                .map(DatabaseIdManagedTableBuilder::build)
                .sorted(Comparator.comparing(DatabaseIdManagedTable::tableName))
                .collect(Collectors.toMap(
                        table -> normalizeName(table.tableName()),
                        table -> table,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
        return new DatabaseIdDiscoveryResult(managedTables, List.copyOf(diagnostics));
    }

    private static void applyOverrides(
            Map<String, DatabaseIdManagedTableBuilder> builders,
            List<DatabaseIdPlanDiagnostic> diagnostics,
            SystemDatabaseIdGovernanceProperties governanceProperties
    ) {
        for (SystemDatabaseIdGovernanceProperties.ReferenceOverride override : governanceProperties.getOverrides()) {
            if (!override.isEnabled()) {
                continue;
            }
            String normalizedTable = normalizeName(override.getTable());
            DatabaseIdManagedTableBuilder sourceBuilder = builders.get(normalizedTable);
            if (sourceBuilder == null) {
                diagnostics.add(new DatabaseIdPlanDiagnostic(
                        DatabaseIdDiagnosticSeverity.WARNING,
                        "OVERRIDE_SOURCE_MISSING",
                        override.getTable() + "." + override.getColumn(),
                        "Override source table was not discovered as a managed table"
                ));
                continue;
            }
            if (isIgnored(governanceProperties, override.getTable(), override.getColumn())) {
                diagnostics.add(new DatabaseIdPlanDiagnostic(
                        DatabaseIdDiagnosticSeverity.WARNING,
                        "REFERENCE_OVERRIDE_IGNORED",
                        override.getTable() + "." + override.getColumn(),
                        "Reference override was skipped because the column is explicitly ignored"
                ));
                continue;
            }
            sourceBuilder.addBinding(new DatabaseIdColumnBinding(
                    override.getColumn(),
                    override.getTargetBusinessType(),
                    override.isPathLike() ? DatabaseIdReferenceKind.PATH_REFERENCE : DatabaseIdReferenceKind.WEAK_REFERENCE,
                    override.isPathLike()
            ));
            diagnostics.add(new DatabaseIdPlanDiagnostic(
                    DatabaseIdDiagnosticSeverity.WARNING,
                    "REFERENCE_OVERRIDE",
                    override.getTable() + "." + override.getColumn(),
                    "Reference override applied toward " + override.getTargetBusinessType()
            ));
        }
    }

    private static void applyIgnores(
            SchemaSnapshot snapshot,
            Map<String, DatabaseIdManagedTableBuilder> builders,
            List<DatabaseIdPlanDiagnostic> diagnostics,
            SystemDatabaseIdGovernanceProperties governanceProperties
    ) {
        for (SystemDatabaseIdGovernanceProperties.ColumnIgnore ignore : governanceProperties.getIgnores()) {
            if (!ignore.isEnabled()) {
                continue;
            }
            String normalizedTable = normalizeName(ignore.getTable());
            DatabaseIdManagedTableBuilder sourceBuilder = builders.get(normalizedTable);
            if (sourceBuilder == null) {
                diagnostics.add(new DatabaseIdPlanDiagnostic(
                        DatabaseIdDiagnosticSeverity.WARNING,
                        "IGNORE_SOURCE_MISSING",
                        ignore.getTable() + "." + ignore.getColumn(),
                        "Ignored column source table was not discovered as a managed table"
                ));
                continue;
            }
            if (sourceBuilder.isIdentityColumn(ignore.getColumn())) {
                diagnostics.add(new DatabaseIdPlanDiagnostic(
                        DatabaseIdDiagnosticSeverity.WARNING,
                        "IDENTITY_COLUMN_IGNORED",
                        ignore.getTable() + "." + ignore.getColumn(),
                        "Identity column ignore was skipped because primary identity columns cannot be ignored"
                ));
                continue;
            }
            if (columnExists(snapshot, ignore.getTable(), ignore.getColumn())) {
                sourceBuilder.removeBindings(ignore.getColumn());
                diagnostics.add(new DatabaseIdPlanDiagnostic(
                        DatabaseIdDiagnosticSeverity.WARNING,
                        "REFERENCE_IGNORED",
                        ignore.getTable() + "." + ignore.getColumn(),
                        "Column was explicitly excluded from ID governance"
                ));
            }
        }
    }

    private static Map<String, DatabaseIdManagedTableBuilder> discoverIdentityBuilders(
            SchemaSnapshot snapshot,
            Map<String, Set<String>> sampleCache
    ) {
        Map<String, DatabaseIdManagedTableBuilder> builders = new LinkedHashMap<>();
        for (DatabaseTableSnapshot table : snapshot.tables().values()) {
            if (table.primaryKeyColumns().size() != 1) {
                continue;
            }
            String primaryKeyColumn = table.primaryKeyColumns().get(0);
            DatabaseColumnSnapshot primaryKey = table.column(primaryKeyColumn);
            if (primaryKey == null || !primaryKey.isManageableScalar()) {
                continue;
            }
            Set<String> samples = sampleCache.getOrDefault(sampleKey(table.table(), primaryKeyColumn), Set.of());
            DatabaseIdPosture posture = summarizePosture(samples, primaryKey);
            if (!isManagedIdentityColumn(table, primaryKey, samples, posture)) {
                continue;
            }
            String businessType = normalizeBusinessType(table.table().tableName());
            builders.put(
                    table.table().normalizedName(),
                    new DatabaseIdManagedTableBuilder(table.table(), primaryKeyColumn, businessType)
            );
        }
        pruneSharedPrimaryKeys(snapshot, builders);
        return builders;
    }

    private static void pruneSharedPrimaryKeys(
            SchemaSnapshot snapshot,
            Map<String, DatabaseIdManagedTableBuilder> builders
    ) {
        Set<String> remove = new LinkedHashSet<>();
        for (DatabaseTableSnapshot table : snapshot.tables().values()) {
            DatabaseIdManagedTableBuilder builder = builders.get(table.table().normalizedName());
            if (builder == null) {
                continue;
            }
            for (DatabaseForeignKeySnapshot foreignKey : table.importedKeys()) {
                if (foreignKey.fkColumns().size() != 1 || foreignKey.pkColumns().size() != 1) {
                    continue;
                }
                if (!foreignKey.fkColumns().get(0).equalsIgnoreCase(builder.identityColumn())) {
                    continue;
                }
                if (builders.containsKey(foreignKey.pkTable().normalizedName())
                        && !foreignKey.pkTable().normalizedName().equals(table.table().normalizedName())) {
                    remove.add(table.table().normalizedName());
                }
            }
        }
        remove.forEach(builders::remove);
    }

    private static void loadExplicitBindings(
            SchemaSnapshot snapshot,
            Map<String, DatabaseIdManagedTableBuilder> builders
    ) {
        for (DatabaseTableSnapshot table : snapshot.tables().values()) {
            DatabaseIdManagedTableBuilder sourceBuilder = builders.get(table.table().normalizedName());
            if (sourceBuilder == null) {
                sourceBuilder = new DatabaseIdManagedTableBuilder(table.table(), null, normalizeBusinessType(table.table().tableName()));
                builders.put(table.table().normalizedName(), sourceBuilder);
            }
            for (DatabaseForeignKeySnapshot foreignKey : table.importedKeys()) {
                if (foreignKey.fkColumns().isEmpty() || foreignKey.pkColumns().isEmpty()) {
                    continue;
                }
                if (foreignKey.fkColumns().size() != 1 || foreignKey.pkColumns().size() != 1) {
                    continue;
                }
                DatabaseIdManagedTableBuilder targetBuilder = builders.get(foreignKey.pkTable().normalizedName());
                if (targetBuilder == null || targetBuilder.identityColumn() == null) {
                    continue;
                }
                if (!foreignKey.pkColumns().get(0).equalsIgnoreCase(targetBuilder.identityColumn())) {
                    continue;
                }
                DatabaseIdReferenceKind kind = foreignKey.fkTable().normalizedName().equals(foreignKey.pkTable().normalizedName())
                        ? DatabaseIdReferenceKind.SELF_REFERENCE
                        : DatabaseIdReferenceKind.EXPLICIT_FK;
                sourceBuilder.addBinding(new DatabaseIdColumnBinding(
                        foreignKey.fkColumns().get(0),
                        targetBuilder.identityMappingBusinessType(),
                        kind,
                        false
                ));
            }
        }
    }

    private static void loadWeakBindings(
            SchemaSnapshot snapshot,
            Map<String, Set<String>> sampleCache,
            Map<String, DatabaseIdManagedTableBuilder> builders,
            List<DatabaseIdPlanDiagnostic> diagnostics,
            SystemDatabaseIdGovernanceProperties governanceProperties
    ) {
        for (DatabaseTableSnapshot table : snapshot.tables().values()) {
            DatabaseIdManagedTableBuilder sourceBuilder = builders.get(table.table().normalizedName());
            if (sourceBuilder == null) {
                continue;
            }
            for (DatabaseColumnSnapshot column : table.columns().values()) {
                String normalizedColumn = column.normalizedColumnName();
                if (sourceBuilder.isIdentityColumn(normalizedColumn) || sourceBuilder.hasBinding(normalizedColumn)) {
                    continue;
                }
                if (isIgnored(governanceProperties, table.table().sqlName(), column.columnName())) {
                    continue;
                }
                if (isStructurallyDenied(column, sampleCache.getOrDefault(sampleKey(table.table(), column.columnName()), Set.of()))) {
                    if (looksLikeReferenceCandidate(normalizedColumn)) {
                        diagnostics.add(new DatabaseIdPlanDiagnostic(
                                DatabaseIdDiagnosticSeverity.WARNING,
                                "STRUCTURAL_DENY",
                                table.table().sqlName() + "." + column.columnName(),
                                "Candidate column was rejected by structural deny rules"
                        ));
                    }
                    continue;
                }
                if (isPathColumn(normalizedColumn)) {
                    if (!column.isCharacterLike()) {
                        continue;
                    }
                    DatabaseIdManagedTableBuilder targetBuilder = inferTargetBuilder(
                            table.table(),
                            column,
                            sampleCache.getOrDefault(sampleKey(table.table(), column.columnName()), Set.of()),
                            builders,
                            sampleCache,
                            true,
                            diagnostics
                    );
                    if (targetBuilder != null) {
                        sourceBuilder.addBinding(new DatabaseIdColumnBinding(
                                column.columnName(),
                                targetBuilder.identityMappingBusinessType(),
                                DatabaseIdReferenceKind.PATH_REFERENCE,
                                true
                        ));
                        diagnostics.add(new DatabaseIdPlanDiagnostic(
                                DatabaseIdDiagnosticSeverity.WARNING,
                                "PATH_REFERENCE",
                                table.table().sqlName() + "." + column.columnName(),
                                "Path container reference was inferred toward " + targetBuilder.identityMappingBusinessType()
                        ));
                    }
                    continue;
                }
                if (!column.isManageableScalar()) {
                    continue;
                }
                DatabaseIdManagedTableBuilder targetBuilder = inferTargetBuilder(
                        table.table(),
                        column,
                        sampleCache.getOrDefault(sampleKey(table.table(), column.columnName()), Set.of()),
                        builders,
                        sampleCache,
                        false,
                        diagnostics
                );
                if (targetBuilder == null) {
                    continue;
                }
                boolean audit = isAuditColumn(normalizedColumn);
                sourceBuilder.addBinding(new DatabaseIdColumnBinding(
                        column.columnName(),
                        targetBuilder.identityMappingBusinessType(),
                        audit ? DatabaseIdReferenceKind.AUDIT_REFERENCE : DatabaseIdReferenceKind.WEAK_REFERENCE,
                        false
                ));
                diagnostics.add(new DatabaseIdPlanDiagnostic(
                        DatabaseIdDiagnosticSeverity.WARNING,
                        audit ? "AUDIT_REFERENCE_INFERRED" : "WEAK_REFERENCE_INFERRED",
                        table.table().sqlName() + "." + column.columnName(),
                        "Reference was inferred toward " + targetBuilder.identityMappingBusinessType()
                ));
            }
        }
    }

    private static DatabaseIdManagedTableBuilder inferTargetBuilder(
            QualifiedTable sourceTable,
            DatabaseColumnSnapshot sourceColumn,
            Set<String> sourceSamples,
            Map<String, DatabaseIdManagedTableBuilder> builders,
            Map<String, Set<String>> sampleCache,
            boolean pathLike,
            List<DatabaseIdPlanDiagnostic> diagnostics
    ) {
        if (sourceSamples.isEmpty()) {
            return null;
        }
        String normalizedColumn = sourceColumn.normalizedColumnName();
        boolean auditColumn = isAuditColumn(normalizedColumn);
        boolean weakCandidate = pathLike || auditColumn || isReferenceCandidateName(normalizedColumn);
        if (!weakCandidate) {
            return null;
        }

        DatabaseIdManagedTableBuilder best = null;
        double bestScore = 0D;
        double secondScore = 0D;
        String bestTarget = null;
        String secondTarget = null;

        for (DatabaseIdManagedTableBuilder target : builders.values()) {
            if (target.identityColumn() == null) {
                continue;
            }
            Set<String> targetSamples = sampleCache.getOrDefault(sampleKey(target.table(), target.identityColumn()), Set.of());
            if (targetSamples.isEmpty()) {
                continue;
            }
            double matchRatio = pathLike ? scorePathSamples(sourceSamples, targetSamples) : scoreScalarSamples(sourceSamples, targetSamples);
            if (matchRatio <= 0D) {
                continue;
            }
            double score = matchRatio + nameBonus(sourceTable, sourceColumn.columnName(), target, pathLike, auditColumn);
            if (score > bestScore) {
                secondScore = bestScore;
                secondTarget = bestTarget;
                bestScore = score;
                bestTarget = target.identityMappingBusinessType();
                best = target;
            } else if (score > secondScore) {
                secondScore = score;
                secondTarget = target.identityMappingBusinessType();
            }
        }

        double threshold = pathLike ? 0.95D : (auditColumn ? 0.95D : 0.70D);
        if (best == null || bestScore < threshold) {
            diagnostics.add(new DatabaseIdPlanDiagnostic(
                    DatabaseIdDiagnosticSeverity.WARNING,
                    "REFERENCE_EVIDENCE_TOO_WEAK",
                    sourceTable.sqlName() + "." + sourceColumn.columnName(),
                    "Reference candidate was skipped because evidence score was below threshold"
            ));
            return null;
        }
        if (bestScore - secondScore < 0.10D && !sameTable(best.table().sqlName(), sourceTable.sqlName())) {
            diagnostics.add(new DatabaseIdPlanDiagnostic(
                    DatabaseIdDiagnosticSeverity.WARNING,
                    "AMBIGUOUS_REFERENCE_TARGET",
                    sourceTable.sqlName() + "." + sourceColumn.columnName(),
                    "Reference candidate was skipped because targets were ambiguous: "
                            + bestTarget + " vs " + secondTarget
            ));
            return null;
        }
        return best;
    }

    static DatabaseIdPosture summarizePosture(Set<String> samples, DatabaseColumnSnapshot column) {
        if (samples.isEmpty()) {
            return DatabaseIdPosture.EMPTY;
        }
        if (column.autoIncrement() || column.generated()) {
            return DatabaseIdPosture.DB_AUTO_INCREMENT;
        }
        Set<DatabaseIdPosture> postures = new LinkedHashSet<>();
        for (String sample : samples) {
            DatabaseIdPosture posture = DatabaseIdPostureClassifier.classify(sample);
            if (posture == DatabaseIdPosture.EMPTY) {
                continue;
            }
            postures.add(posture);
        }
        if (postures.isEmpty()) {
            return DatabaseIdPosture.EMPTY;
        }
        if (postures.size() == 1) {
            return postures.iterator().next();
        }
        return DatabaseIdPosture.MIXED;
    }

    private static boolean isManagedIdentityColumn(
            DatabaseTableSnapshot table,
            DatabaseColumnSnapshot primaryKey,
            Set<String> samples,
            DatabaseIdPosture posture
    ) {
        if (primaryKey.autoIncrement() || primaryKey.generated()) {
            return true;
        }
        if (posture == DatabaseIdPosture.SNOWFLAKE || posture == DatabaseIdPosture.DB_AUTO_INCREMENT) {
            return true;
        }
        if (table.uniqueKeys().stream().anyMatch(keys -> keys.size() == 1 && keys.get(0).equalsIgnoreCase(primaryKey.columnName()))) {
            return true;
        }
        if (hasInboundReferenceHint(table)) {
            return true;
        }
        return !samples.isEmpty() && allNumericLike(samples);
    }

    private static boolean hasInboundReferenceHint(DatabaseTableSnapshot table) {
        for (DatabaseColumnSnapshot column : table.columns().values()) {
            String normalizedColumn = column.normalizedColumnName();
            if (normalizedColumn.equals(normalizeName(table.primaryKeyColumns().get(0)))) {
                continue;
            }
            if (isPathColumn(normalizedColumn) || isReferenceCandidateName(normalizedColumn)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isIgnored(
            SystemDatabaseIdGovernanceProperties governanceProperties,
            String tableName,
            String columnName
    ) {
        String normalizedTable = normalizeName(tableName);
        String normalizedColumn = normalizeName(columnName);
        for (SystemDatabaseIdGovernanceProperties.ColumnIgnore ignore : governanceProperties.getIgnores()) {
            if (!ignore.isEnabled()) {
                continue;
            }
            if (normalizeName(ignore.getTable()).equals(normalizedTable)
                    && normalizeName(ignore.getColumn()).equals(normalizedColumn)) {
                return true;
            }
        }
        return false;
    }

    private static boolean columnExists(SchemaSnapshot snapshot, String tableName, String columnName) {
        DatabaseTableSnapshot table = snapshot.tables().get(normalizeName(tableName));
        if (table == null) {
            return false;
        }
        return table.columns().values().stream()
                .anyMatch(column -> normalizeName(column.columnName()).equals(normalizeName(columnName)));
    }

    private static boolean isStructurallyDenied(DatabaseColumnSnapshot column, Set<String> samples) {
        if (!column.isManageableScalar() && !column.isCharacterLike()) {
            return true;
        }
        String normalizedName = column.normalizedColumnName();
        boolean denyName = DENY_NAME_TOKENS.stream().anyMatch(normalizedName::contains);
        if (!denyName) {
            return false;
        }
        if (samples.isEmpty()) {
            return false;
        }
        return samples.size() <= LOW_CARDINALITY_THRESHOLD;
    }

    private static boolean allNumericLike(Set<String> samples) {
        for (String sample : samples) {
            DatabaseIdPosture posture = DatabaseIdPostureClassifier.classify(sample);
            if (posture != DatabaseIdPosture.DB_AUTO_INCREMENT && posture != DatabaseIdPosture.SNOWFLAKE) {
                return false;
            }
        }
        return true;
    }

    private static double scoreScalarSamples(Set<String> sourceSamples, Set<String> targetSamples) {
        int matches = 0;
        int total = 0;
        for (String sample : sourceSamples) {
            if (sample == null || sample.isBlank()) {
                continue;
            }
            total++;
            if (targetSamples.contains(sample.trim())) {
                matches++;
            }
        }
        if (total == 0) {
            return 0D;
        }
        return (double) matches / (double) total;
    }

    private static double scorePathSamples(Set<String> sourceSamples, Set<String> targetSamples) {
        int matches = 0;
        int total = 0;
        for (String sample : sourceSamples) {
            if (sample == null || sample.isBlank()) {
                continue;
            }
            Matcher matcher = PATH_TOKEN_PATTERN.matcher(sample);
            while (matcher.find()) {
                total++;
                if (targetSamples.contains(matcher.group())) {
                    matches++;
                }
            }
        }
        if (total == 0) {
            return 0D;
        }
        return (double) matches / (double) total;
    }

    private static double nameBonus(
            QualifiedTable sourceTable,
            String sourceColumnName,
            DatabaseIdManagedTableBuilder target,
            boolean pathLike,
            boolean audit
    ) {
        String sourceName = normalizeName(sourceTable.tableName() + "_" + sourceColumnName);
        String targetName = normalizeName(target.identityMappingBusinessType());
        double bonus = 0D;
        if (sourceName.contains(targetName.replace(".", "_"))) {
            bonus += 0.20D;
        }
        if (pathLike && (sourceName.contains("path") || sourceName.contains("ancestor") || sourceName.contains("descendant"))) {
            bonus += 0.20D;
        }
        if (audit && "sys_user".equals(normalizeName(target.identityMappingBusinessType()))) {
            bonus += 0.30D;
        }
        return bonus;
    }

    static boolean isAuditColumn(String columnName) {
        return AUDIT_COLUMNS.contains(normalizeName(columnName));
    }

    static boolean isPathColumn(String columnName) {
        String normalized = normalizeName(columnName);
        return normalized.contains("path")
                || normalized.endsWith("_ids")
                || normalized.endsWith("_path")
                || normalized.contains("ancestor")
                || normalized.contains("descendant");
    }

    private static boolean isReferenceCandidateName(String columnName) {
        if (columnName.equals("id")) {
            return false;
        }
        if (isAuditColumn(columnName) || isPathColumn(columnName)) {
            return true;
        }
        for (String suffix : REFERENCE_SUFFIXES) {
            if (columnName.endsWith(suffix)) {
                return true;
            }
        }
        return columnName.endsWith("ids");
    }

    private static boolean looksLikeReferenceCandidate(String columnName) {
        return isReferenceCandidateName(columnName) || DENY_NAME_TOKENS.stream().anyMatch(columnName::contains);
    }

    static String sampleKey(QualifiedTable table, String columnName) {
        return table.normalizedName() + "|" + normalizeName(columnName);
    }

    private static boolean sameTable(String left, String right) {
        return normalizeName(left).equals(normalizeName(right));
    }

    static String normalizeBusinessType(String businessType) {
        return businessType == null ? "" : businessType.trim();
    }

    static String normalizeName(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    record DatabaseIdManagedTableBuilder(
            QualifiedTable table,
            String identityColumn,
            String identityMappingBusinessType,
            Map<String, DatabaseIdColumnBinding> bindings
    ) {

        DatabaseIdManagedTableBuilder(QualifiedTable table, String identityColumn, String identityMappingBusinessType) {
            this(table, identityColumn, identityMappingBusinessType, new LinkedHashMap<>());
        }

        void addBinding(DatabaseIdColumnBinding binding) {
            String key = binding.columnName().toLowerCase(Locale.ROOT)
                    + "|" + binding.targetBusinessType().toLowerCase(Locale.ROOT)
                    + "|" + binding.kind().name()
                    + "|" + binding.pathLike();
            bindings.putIfAbsent(key, binding);
        }

        void removeBindings(String columnName) {
            String normalized = columnName.toLowerCase(Locale.ROOT);
            bindings.entrySet().removeIf(entry -> entry.getValue().columnName().toLowerCase(Locale.ROOT).equals(normalized));
        }

        boolean hasBinding(String columnName) {
            String normalized = columnName.toLowerCase(Locale.ROOT);
            for (DatabaseIdColumnBinding binding : bindings.values()) {
                if (binding.columnName().toLowerCase(Locale.ROOT).equals(normalized)) {
                    return true;
                }
            }
            return false;
        }

        boolean isIdentityColumn(String columnName) {
            return identityColumn != null && identityColumn.equalsIgnoreCase(columnName);
        }

        DatabaseIdManagedTable build() {
            List<DatabaseIdColumnBinding> orderedBindings = bindings.values().stream()
                    .sorted(Comparator.comparing(DatabaseIdColumnBinding::columnName))
                    .toList();
            return new DatabaseIdManagedTable(
                    table.sqlName(),
                    identityColumn,
                    identityMappingBusinessType,
                    List.copyOf(orderedBindings)
            );
        }
    }
}
