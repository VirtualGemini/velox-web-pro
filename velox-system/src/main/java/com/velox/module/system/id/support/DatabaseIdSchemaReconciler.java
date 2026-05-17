package com.velox.module.system.id.support;

import com.velox.framework.id.properties.VeloxIdProperties;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DatabaseIdSchemaReconciler {

    private static final int TEMP_CHARACTER_ID_COLUMN_SIZE = 64;
    private static final int SAMPLE_LIMIT = 50;
    private static final Set<String> GOVERNANCE_TABLES = Set.of("sys_id_sequence");
    private static final Pattern PATH_TOKEN_PATTERN = Pattern.compile("[^/,;|>\\s]+");
    private static final List<String> AUDIT_COLUMNS = List.of("create_by", "update_by");
    private static final Set<String> REFERENCE_HINTS = Set.of(
            "parent", "root", "ancestor", "descendant", "owner", "creator", "modifier",
            "manager", "leader", "user", "role", "menu", "tenant", "dept", "org",
            "company", "account", "node", "tree", "ref", "link", "created", "updated"
    );

    private final DatabaseIdValueGenerator valueGenerator;

    public DatabaseIdSchemaReconciler(VeloxIdProperties properties) {
        this.valueGenerator = new DatabaseIdValueGenerator(properties);
    }

    public DatabaseIdReconciliationPlan plan(Connection connection) throws SQLException {
        Discovery discovery = discover(connection);
        Map<String, LinkedHashMap<String, String>> idMappings = new LinkedHashMap<>();
        boolean hasDetectedData = false;

        for (IdentityDomain domain : discovery.identityDomains().values()) {
            List<String> currentIds = readAllDistinctValues(connection, domain.tableName(), domain.identityColumn());
            if (!currentIds.isEmpty()) {
                hasDetectedData = true;
            }
            LinkedHashSet<String> reservedValues = new LinkedHashSet<>(currentIds);
            LinkedHashMap<String, String> mapping = new LinkedHashMap<>();
            for (String currentId : currentIds) {
                if (!shouldMigrateManagedValue(currentId)) {
                    continue;
                }
                mapping.put(currentId, valueGenerator.resolveTargetId(currentId, reservedValues));
            }
            if (!mapping.isEmpty()) {
                idMappings.put(domain.tableName(), mapping);
            }
        }

        return new DatabaseIdReconciliationPlan(
                hasDetectedData,
                !idMappings.isEmpty(),
                valueGenerator.targetPosture(),
                discovery.managedTables(),
                idMappings
        );
    }

    public void reconcile(Connection connection, DatabaseIdReconciliationPlan plan) throws SQLException {
        if (!plan.needsMigration()) {
            return;
        }
        ForeignKeyGuard foreignKeyGuard = ForeignKeyGuard.open(connection, plan.availableTables().keySet());
        try {
            expandManagedCharacterColumns(connection, plan);
            for (ManagedTable table : plan.availableTables().values()) {
                Map<String, String> mappings = plan.idMappings().get(table.tableName());
                if (table.identityColumn() != null) {
                    applyUpdates(connection, table.tableName(), table.identityColumn(), mappings);
                }
            }
            for (ManagedTable table : plan.availableTables().values()) {
                for (ColumnBinding binding : table.bindings()) {
                    Map<String, String> targetMappings = plan.idMappings().get(binding.targetTable());
                    if (binding.pathLike()) {
                        applyPathUpdates(connection, table.tableName(), binding.columnName(), targetMappings);
                    } else {
                        applyUpdates(connection, table.tableName(), binding.columnName(), targetMappings);
                    }
                }
            }
            alignManagedColumnsToBigint(connection, plan);
        } finally {
            foreignKeyGuard.close();
        }
    }

    public Map<String, Long> resolveSnowflakeSequenceValues(Connection connection, Collection<String> businessTypes)
            throws SQLException {
        Map<String, Long> values = businessTypes.stream().collect(Collectors.toMap(
                businessType -> businessType,
                businessType -> 0L,
                (left, right) -> left,
                LinkedHashMap::new
        ));
        Discovery discovery = discover(connection);
        for (IdentityDomain domain : discovery.identityDomains().values()) {
            if (!values.containsKey(domain.tableName())) {
                continue;
            }
            long maxValue = 0L;
            for (String currentId : readAllDistinctValues(connection, domain.tableName(), domain.identityColumn())) {
                if (!valueGenerator.isTargetPosture(currentId)) {
                    continue;
                }
                maxValue = Math.max(maxValue, valueGenerator.decodeSnowflakeId(currentId));
            }
            values.put(domain.tableName(), maxValue);
        }
        return values;
    }

    public Map<String, Long> resolveDatabaseSequenceValues(Connection connection, Collection<String> businessTypes)
            throws SQLException {
        Map<String, Long> values = businessTypes.stream().collect(Collectors.toMap(
                businessType -> businessType,
                businessType -> 0L,
                (left, right) -> left,
                LinkedHashMap::new
        ));
        Discovery discovery = discover(connection);
        for (IdentityDomain domain : discovery.identityDomains().values()) {
            if (!values.containsKey(domain.tableName())) {
                continue;
            }
            long maxValue = 0L;
            for (String currentId : readAllDistinctValues(connection, domain.tableName(), domain.identityColumn())) {
                if (DatabaseIdPostureClassifier.classify(currentId) != DatabaseIdPosture.DB_AUTO_INCREMENT) {
                    continue;
                }
                maxValue = Math.max(maxValue, Long.parseLong(currentId));
            }
            values.put(domain.tableName(), maxValue);
        }
        return values;
    }

    public List<String> resolveManagedBusinessTypes(Connection connection) throws SQLException {
        return discover(connection).identityDomains().values().stream()
                .map(IdentityDomain::tableName)
                .sorted()
                .toList();
    }

    private Discovery discover(Connection connection) throws SQLException {
        Map<String, TableSnapshot> snapshots = loadTableSnapshots(connection);
        Map<String, IdentityDomain> identityDomains = discoverIdentityDomains(connection, snapshots);
        pruneSharedPrimaryKeyDomains(connection, identityDomains);
        Map<String, Set<String>> identityValues = loadIdentityValues(connection, identityDomains);
        Map<String, ManagedTableBuilder> builders = new LinkedHashMap<>();

        for (IdentityDomain domain : identityDomains.values()) {
            builders.put(
                    normalizeTableName(domain.tableName()),
                    new ManagedTableBuilder(domain.tableName(), domain.tableName(), domain.identityColumn())
            );
        }

        loadExplicitBindings(connection, snapshots, identityDomains, builders);
        loadHeuristicBindings(connection, snapshots, identityDomains, identityValues, builders);

        Map<String, ManagedTable> managedTables = builders.values().stream()
                .map(ManagedTableBuilder::build)
                .filter(table -> table.identityColumn() != null || !table.bindings().isEmpty())
                .sorted(Comparator.comparing(ManagedTable::tableName))
                .collect(Collectors.toMap(
                        ManagedTable::tableName,
                        table -> table,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));

        return new Discovery(snapshots, identityDomains, managedTables, identityValues);
    }

    private void pruneSharedPrimaryKeyDomains(Connection connection, Map<String, IdentityDomain> identityDomains) throws SQLException {
        Set<String> sharedPrimaryKeyTables = new LinkedHashSet<>();
        String catalog = connection.getCatalog();
        String schemaPattern = resolveSchemaPattern(connection);
        for (IdentityDomain domain : identityDomains.values()) {
            try (ResultSet resultSet = connection.getMetaData().getImportedKeys(catalog, schemaPattern, domain.tableName())) {
                while (resultSet.next()) {
                    String fkColumnName = resultSet.getString("FKCOLUMN_NAME");
                    String pkTableName = normalizeTableName(resultSet.getString("PKTABLE_NAME"));
                    if (!domain.identityColumn().equalsIgnoreCase(fkColumnName)) {
                        continue;
                    }
                    if (pkTableName.equalsIgnoreCase(domain.tableName())) {
                        continue;
                    }
                    if (identityDomains.containsKey(pkTableName)) {
                        sharedPrimaryKeyTables.add(domain.tableName());
                    }
                }
            }
        }
        for (String tableName : sharedPrimaryKeyTables) {
            identityDomains.remove(tableName);
        }
    }

    private Map<String, TableSnapshot> loadTableSnapshots(Connection connection) throws SQLException {
        String catalog = connection.getCatalog();
        String schemaPattern = resolveSchemaPattern(connection);
        Map<String, TableSnapshotBuilder> builders = new LinkedHashMap<>();
        try (ResultSet tables = connection.getMetaData().getTables(catalog, schemaPattern, "%", new String[]{"TABLE"})) {
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                if (isSystemTable(tableName)) {
                    continue;
                }
                builders.put(normalizeTableName(tableName), new TableSnapshotBuilder(tableName));
            }
        }
        for (TableSnapshotBuilder builder : builders.values()) {
            loadPrimaryKeys(connection, builder);
            loadColumns(connection, builder);
        }
        return builders.values().stream()
                .map(TableSnapshotBuilder::build)
                .sorted(Comparator.comparing(TableSnapshot::tableName))
                .collect(Collectors.toMap(
                        TableSnapshot::tableName,
                        table -> table,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
    }

    private void loadPrimaryKeys(Connection connection, TableSnapshotBuilder builder) throws SQLException {
        String catalog = connection.getCatalog();
        String schemaPattern = resolveSchemaPattern(connection);
        try (ResultSet primaryKeys = connection.getMetaData().getPrimaryKeys(catalog, schemaPattern, builder.tableName())) {
            while (primaryKeys.next()) {
                builder.addPrimaryKey(primaryKeys.getString("COLUMN_NAME"));
            }
        }
    }

    private void loadColumns(Connection connection, TableSnapshotBuilder builder) throws SQLException {
        String catalog = connection.getCatalog();
        String schemaPattern = resolveSchemaPattern(connection);
        try (ResultSet resultSet = connection.getMetaData().getColumns(catalog, schemaPattern, builder.tableName(), "%")) {
            while (resultSet.next()) {
                builder.addColumn(
                        resultSet.getString("COLUMN_NAME"),
                        resultSet.getInt("DATA_TYPE"),
                        resultSet.getString("TYPE_NAME"),
                        resultSet.getInt("COLUMN_SIZE"),
                        resultSet.getInt("NULLABLE") == DatabaseMetaData.columnNullable
                );
            }
        }
    }

    private Map<String, IdentityDomain> discoverIdentityDomains(
            Connection connection,
            Map<String, TableSnapshot> snapshots
    ) throws SQLException {
        Map<String, IdentityDomain> identityDomains = new LinkedHashMap<>();
        for (TableSnapshot snapshot : snapshots.values()) {
            String identityColumn = discoverIdentityColumn(connection, snapshot);
            if (identityColumn == null) {
                continue;
            }
            identityDomains.put(
                    normalizeTableName(snapshot.tableName()),
                    new IdentityDomain(snapshot.tableName(), identityColumn)
            );
        }
        return identityDomains;
    }

    private String discoverIdentityColumn(Connection connection, TableSnapshot snapshot) throws SQLException {
        if (snapshot.primaryKeyColumns().size() != 1) {
            return null;
        }
        String primaryKeyColumn = snapshot.primaryKeyColumns().get(0);
        ColumnMetadata metadata = snapshot.columns().get(normalizeColumnName(primaryKeyColumn));
        if (metadata == null || !metadata.isManagedIdentifierColumn()) {
            return null;
        }
        if (isIdentityName(snapshot.tableName(), primaryKeyColumn)) {
            return primaryKeyColumn;
        }
        if (hasRelationHints(snapshot)) {
            return primaryKeyColumn;
        }
        for (String sample : readSampleValues(connection, snapshot.tableName(), primaryKeyColumn)) {
            if (DatabaseIdPostureClassifier.classify(sample) != DatabaseIdPosture.UNKNOWN) {
                return primaryKeyColumn;
            }
        }
        return null;
    }

    private boolean hasRelationHints(TableSnapshot snapshot) {
        for (ColumnMetadata column : snapshot.columns().values()) {
            String columnName = normalizeColumnName(column.columnName());
            if (snapshot.primaryKeyColumns().stream().anyMatch(pk -> normalizeColumnName(pk).equals(columnName))) {
                continue;
            }
            if (isReferenceHint(columnName) || isAuditColumn(columnName) || isPathColumn(columnName)) {
                return true;
            }
        }
        return false;
    }

    private Map<String, Set<String>> loadIdentityValues(
            Connection connection,
            Map<String, IdentityDomain> identityDomains
    ) throws SQLException {
        Map<String, Set<String>> values = new LinkedHashMap<>();
        for (IdentityDomain domain : identityDomains.values()) {
            values.put(domain.tableName(), new LinkedHashSet<>(readAllDistinctValues(connection, domain.tableName(), domain.identityColumn())));
        }
        return values;
    }

    private void loadExplicitBindings(
            Connection connection,
            Map<String, TableSnapshot> snapshots,
            Map<String, IdentityDomain> identityDomains,
            Map<String, ManagedTableBuilder> builders
    ) throws SQLException {
        for (TableSnapshot snapshot : snapshots.values()) {
            String catalog = connection.getCatalog();
            String schemaPattern = resolveSchemaPattern(connection);
            try (ResultSet resultSet = connection.getMetaData().getImportedKeys(catalog, schemaPattern, snapshot.tableName())) {
                while (resultSet.next()) {
                    String fkColumnName = resultSet.getString("FKCOLUMN_NAME");
                    String pkTableName = normalizeTableName(resultSet.getString("PKTABLE_NAME"));
                    String pkColumnName = resultSet.getString("PKCOLUMN_NAME");
                    IdentityDomain targetDomain = identityDomains.get(pkTableName);
                    if (targetDomain == null || !targetDomain.identityColumn().equalsIgnoreCase(pkColumnName)) {
                        continue;
                    }
                    addBinding(builders, snapshot.tableName(), new ColumnBinding(
                            fkColumnName,
                            targetDomain.tableName(),
                            BindingKind.EXPLICIT_FK,
                            false
                    ));
                }
            }
        }
    }

    private void loadHeuristicBindings(
            Connection connection,
            Map<String, TableSnapshot> snapshots,
            Map<String, IdentityDomain> identityDomains,
            Map<String, Set<String>> identityValues,
            Map<String, ManagedTableBuilder> builders
    ) throws SQLException {
        for (TableSnapshot snapshot : snapshots.values()) {
            for (ColumnMetadata column : snapshot.columns().values()) {
                String columnName = normalizeColumnName(column.columnName());
                if (snapshot.primaryKeyColumns().stream().anyMatch(pk -> normalizeColumnName(pk).equals(columnName))) {
                    continue;
                }
                if (hasBinding(builders, snapshot.tableName(), columnName)) {
                    continue;
                }
                if (isPathColumn(columnName)) {
                    if (!column.isCharacterColumn()) {
                        continue;
                    }
                    String targetTable = inferTargetTable(
                            connection,
                            snapshot.tableName(),
                            column.columnName(),
                            identityDomains,
                            identityValues,
                            true
                    );
                    if (targetTable != null) {
                        addBinding(builders, snapshot.tableName(), new ColumnBinding(
                                column.columnName(),
                                targetTable,
                                BindingKind.PATH_REFERENCE,
                                true
                        ));
                    }
                    continue;
                }
                if (!column.isManagedIdentifierColumn()) {
                    continue;
                }
                if (isAuditColumn(columnName)) {
                    String targetTable = identityDomains.containsKey("sys_user")
                            ? "sys_user"
                            : inferTargetTable(connection, snapshot.tableName(), column.columnName(), identityDomains, identityValues, false);
                    if (targetTable != null) {
                        addBinding(builders, snapshot.tableName(), new ColumnBinding(
                                column.columnName(),
                                targetTable,
                                BindingKind.AUDIT_REFERENCE,
                                false
                        ));
                    }
                    continue;
                }
                if (!isReferenceHint(columnName)) {
                    continue;
                }
                String targetTable = inferTargetTable(
                        connection,
                        snapshot.tableName(),
                        column.columnName(),
                        identityDomains,
                        identityValues,
                        false
                );
                if (targetTable != null) {
                    BindingKind kind = targetTable.equalsIgnoreCase(snapshot.tableName())
                            ? BindingKind.SELF_REFERENCE
                            : BindingKind.INFERRED_REFERENCE;
                    addBinding(builders, snapshot.tableName(), new ColumnBinding(
                            column.columnName(),
                            targetTable,
                            kind,
                            false
                    ));
                }
            }
        }
    }

    private String inferTargetTable(
            Connection connection,
            String sourceTableName,
            String sourceColumnName,
            Map<String, IdentityDomain> identityDomains,
            Map<String, Set<String>> identityValues,
            boolean pathLike
    ) throws SQLException {
        List<String> samples = readSampleValues(connection, sourceTableName, sourceColumnName);
        if (samples.isEmpty()) {
            return null;
        }
        String sourceName = normalizeName(sourceTableName + " " + sourceColumnName);
        String bestTarget = null;
        double bestScore = 0D;
        double secondScore = 0D;
        for (IdentityDomain domain : identityDomains.values()) {
            Set<String> values = identityValues.getOrDefault(domain.tableName(), Set.of());
            double score = scoreSamples(samples, values, pathLike);
            score += nameBonus(sourceName, domain.tableName(), sourceColumnName, pathLike);
            if (score > bestScore) {
                secondScore = bestScore;
                bestScore = score;
                bestTarget = domain.tableName();
            } else if (score > secondScore) {
                secondScore = score;
            }
        }
        if (bestTarget == null) {
            return null;
        }
        double threshold = pathLike ? 0.95D : 0.55D;
        if (bestScore < threshold) {
            return null;
        }
        if (bestScore - secondScore < 0.10D && !bestTarget.equalsIgnoreCase(sourceTableName)) {
            return null;
        }
        return bestTarget;
    }

    private double scoreSamples(List<String> samples, Set<String> targetValues, boolean pathLike) {
        int matches = 0;
        int total = 0;
        for (String sample : samples) {
            if (sample == null || sample.isBlank()) {
                continue;
            }
            total++;
            if (pathLike) {
                matches += pathTokenMatchCount(sample, targetValues);
                total += Math.max(pathTokenCount(sample) - 1, 0);
                continue;
            }
            if (targetValues.contains(sample.trim())) {
                matches++;
            }
        }
        if (total == 0) {
            return 0D;
        }
        return (double) matches / (double) total;
    }

    private int pathTokenMatchCount(String value, Set<String> targetValues) {
        int matches = 0;
        Matcher matcher = PATH_TOKEN_PATTERN.matcher(value);
        while (matcher.find()) {
            if (targetValues.contains(matcher.group())) {
                matches++;
            }
        }
        return matches;
    }

    private int pathTokenCount(String value) {
        int count = 0;
        Matcher matcher = PATH_TOKEN_PATTERN.matcher(value);
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    private double nameBonus(String sourceName, String targetTableName, String columnName, boolean pathLike) {
        String normalizedTarget = normalizeName(targetTableName);
        String targetAlias = resolveDomainAlias(targetTableName);
        double bonus = 0D;
        if (sourceName.contains(normalizedTarget)) {
            bonus += 0.25D;
        }
        if (!targetAlias.isBlank() && sourceName.contains(targetAlias)) {
            bonus += 0.20D;
        }
        if (pathLike) {
            if (sourceName.contains("path") || sourceName.contains("tree")) {
                bonus += 0.20D;
            }
            return bonus;
        }
        String normalizedColumn = normalizeName(columnName);
        for (String hint : REFERENCE_HINTS) {
            if (normalizedColumn.contains(hint) || sourceName.contains(hint)) {
                bonus += 0.10D;
                break;
            }
        }
        if (normalizedColumn.endsWith("id") || normalizedColumn.endsWith("by")) {
            bonus += 0.10D;
        }
        if ("sys_user".equalsIgnoreCase(targetTableName) && isAuditColumn(normalizedColumn)) {
            bonus += 0.25D;
        }
        return bonus;
    }

    private String resolveDomainAlias(String tableName) {
        String normalized = normalizeName(tableName);
        if (normalized.startsWith("sys_") || normalized.startsWith("biz_")) {
            normalized = normalized.substring(4);
        }
        int lastSeparator = normalized.lastIndexOf('_');
        if (lastSeparator >= 0 && lastSeparator < normalized.length() - 1) {
            return normalized.substring(lastSeparator + 1);
        }
        return normalized;
    }

    private boolean hasBinding(Map<String, ManagedTableBuilder> builders, String tableName, String columnName) {
        ManagedTableBuilder builder = builders.get(normalizeTableName(tableName));
        if (builder == null) {
            return false;
        }
        return builder.hasBinding(columnName);
    }

    private void addBinding(Map<String, ManagedTableBuilder> builders, String tableName, ColumnBinding binding) {
        ManagedTableBuilder builder = builders.computeIfAbsent(
                normalizeTableName(tableName),
                ignored -> new ManagedTableBuilder(tableName, normalizeTableName(tableName), null)
        );
        builder.addBinding(binding);
    }

    private boolean shouldMigrateManagedValue(String currentValue) {
        DatabaseIdPosture currentPosture = DatabaseIdPostureClassifier.classify(currentValue);
        DatabaseIdPosture targetPosture = valueGenerator.targetPosture();
        return currentPosture != DatabaseIdPosture.EMPTY && currentPosture != targetPosture;
    }

    private void expandManagedCharacterColumns(Connection connection, DatabaseIdReconciliationPlan plan) throws SQLException {
        for (ManagedTable table : plan.availableTables().values()) {
            if (table.identityColumn() != null) {
                expandManagedCharacterColumn(connection, table.tableName(), table.identityColumn());
            }
            for (ColumnBinding binding : table.bindings()) {
                if (binding.pathLike()) {
                    continue;
                }
                expandManagedCharacterColumn(connection, table.tableName(), binding.columnName());
            }
        }
    }

    private void alignManagedColumnsToBigint(Connection connection, DatabaseIdReconciliationPlan plan) throws SQLException {
        for (ManagedTable table : plan.availableTables().values()) {
            if (table.identityColumn() != null) {
                convertManagedColumnToBigint(connection, table.tableName(), table.identityColumn());
            }
            for (ColumnBinding binding : table.bindings()) {
                if (binding.pathLike()) {
                    continue;
                }
                convertManagedColumnToBigint(connection, table.tableName(), binding.columnName());
            }
        }
    }

    private List<String> readSampleValues(Connection connection, String tableName, String columnName) throws SQLException {
        List<String> values = new ArrayList<>();
        String sql = "SELECT DISTINCT " + columnName + " FROM " + tableName + " WHERE " + columnName
                + " IS NOT NULL ORDER BY " + columnName + " LIMIT " + SAMPLE_LIMIT;
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                values.add(resultSet.getString(1));
            }
        }
        return values;
    }

    private List<String> readAllDistinctValues(Connection connection, String tableName, String columnName) throws SQLException {
        List<String> values = new ArrayList<>();
        String sql = "SELECT DISTINCT " + columnName + " FROM " + tableName + " WHERE " + columnName
                + " IS NOT NULL ORDER BY " + columnName;
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                values.add(resultSet.getString(1));
            }
        }
        return values;
    }

    private void applyUpdates(Connection connection, String tableName, String columnName, Map<String, String> mappings)
            throws SQLException {
        if (mappings == null || mappings.isEmpty()) {
            return;
        }
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE " + tableName + " SET " + columnName + " = ? WHERE " + columnName + " = ?")) {
            for (Map.Entry<String, String> entry : mappings.entrySet()) {
                statement.setString(1, entry.getValue());
                statement.setString(2, entry.getKey());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void applyPathUpdates(Connection connection, String tableName, String columnName, Map<String, String> mappings)
            throws SQLException {
        if (mappings == null || mappings.isEmpty()) {
            return;
        }
        List<String> values = readAllDistinctValues(connection, tableName, columnName);
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE " + tableName + " SET " + columnName + " = ? WHERE " + columnName + " = ?")) {
            for (String value : values) {
                String rewritten = rewritePathValue(value, mappings);
                if (rewritten == null || rewritten.equals(value)) {
                    continue;
                }
                statement.setString(1, rewritten);
                statement.setString(2, value);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private String rewritePathValue(String currentValue, Map<String, String> mappings) {
        if (currentValue == null || currentValue.isBlank() || mappings == null || mappings.isEmpty()) {
            return currentValue;
        }
        Matcher matcher = PATH_TOKEN_PATTERN.matcher(currentValue);
        StringBuilder builder = new StringBuilder();
        int lastIndex = 0;
        boolean changed = false;
        while (matcher.find()) {
            builder.append(currentValue, lastIndex, matcher.start());
            String token = matcher.group();
            String replacement = mappings.get(token);
            if (replacement != null) {
                builder.append(replacement);
                changed = true;
            } else {
                builder.append(token);
            }
            lastIndex = matcher.end();
        }
        if (!changed) {
            return currentValue;
        }
        builder.append(currentValue.substring(lastIndex));
        return builder.toString();
    }

    private void expandManagedCharacterColumn(Connection connection, String tableName, String columnName) throws SQLException {
        ColumnMetadata columnMetadata = readColumnMetadata(connection, tableName, columnName);
        if (columnMetadata == null || !columnMetadata.isCharacterColumn()) {
            return;
        }
        if (columnMetadata.columnSize() >= TEMP_CHARACTER_ID_COLUMN_SIZE) {
            return;
        }
        String nullableClause = columnMetadata.nullable() ? "NULL" : "NOT NULL";
        try (Statement statement = connection.createStatement()) {
            String databaseProduct = connection.getMetaData().getDatabaseProductName().toLowerCase(Locale.ROOT);
            if (databaseProduct.contains("mysql")) {
                statement.execute(
                        "ALTER TABLE " + tableName
                                + " MODIFY COLUMN " + columnName + " "
                                + columnMetadata.renderTypeDefinition(TEMP_CHARACTER_ID_COLUMN_SIZE)
                                + " " + nullableClause
                );
                return;
            }
            statement.execute(
                    "ALTER TABLE " + tableName
                            + " ALTER COLUMN " + columnName + " TYPE "
                            + columnMetadata.renderTypeDefinition(TEMP_CHARACTER_ID_COLUMN_SIZE)
            );
        }
    }

    private void convertManagedColumnToBigint(Connection connection, String tableName, String columnName) throws SQLException {
        ColumnMetadata columnMetadata = readColumnMetadata(connection, tableName, columnName);
        if (columnMetadata == null || columnMetadata.isBigIntColumn()) {
            return;
        }
        String nullableClause = columnMetadata.nullable() ? "NULL" : "NOT NULL";
        String databaseProduct = connection.getMetaData().getDatabaseProductName().toLowerCase(Locale.ROOT);
        try (Statement statement = connection.createStatement()) {
            if (databaseProduct.contains("mysql")) {
                statement.execute(
                        "ALTER TABLE " + tableName
                                + " MODIFY COLUMN " + columnName + " bigint " + nullableClause
                );
                return;
            }
            if (databaseProduct.contains("postgresql")) {
                statement.execute(
                        "ALTER TABLE " + tableName
                                + " ALTER COLUMN " + columnName + " TYPE bigint USING "
                                + renderBigintUsingExpression(columnName, columnMetadata)
                );
                return;
            }
            if (databaseProduct.contains("h2")) {
                statement.execute("ALTER TABLE " + tableName + " ALTER COLUMN " + columnName + " BIGINT");
                return;
            }
            statement.execute(
                    "ALTER TABLE " + tableName
                            + " ALTER COLUMN " + columnName + " TYPE bigint"
            );
        }
    }

    private String renderBigintUsingExpression(String columnName, ColumnMetadata columnMetadata) {
        if (columnMetadata.isCharacterColumn()) {
            return "NULLIF(BTRIM(" + columnName + "), '')::bigint";
        }
        return columnName + "::bigint";
    }

    private ColumnMetadata readColumnMetadata(Connection connection, String tableName, String columnName) throws SQLException {
        String catalog = connection.getCatalog();
        String schemaPattern = resolveSchemaPattern(connection);
        try (ResultSet resultSet = connection.getMetaData().getColumns(catalog, schemaPattern, tableName, columnName)) {
            if (resultSet.next()) {
                return new ColumnMetadata(
                        resultSet.getString("COLUMN_NAME"),
                        resultSet.getInt("DATA_TYPE"),
                        resultSet.getString("TYPE_NAME"),
                        resultSet.getInt("COLUMN_SIZE"),
                        resultSet.getInt("NULLABLE") == DatabaseMetaData.columnNullable
                );
            }
        }
        try (ResultSet fallback = connection.getMetaData().getColumns(catalog, null, tableName, columnName)) {
            if (!fallback.next()) {
                return null;
            }
            return new ColumnMetadata(
                    fallback.getString("COLUMN_NAME"),
                    fallback.getInt("DATA_TYPE"),
                    fallback.getString("TYPE_NAME"),
                    fallback.getInt("COLUMN_SIZE"),
                    fallback.getInt("NULLABLE") == DatabaseMetaData.columnNullable
            );
        }
    }

    private String resolveSchemaPattern(Connection connection) throws SQLException {
        String schema = connection.getSchema();
        return schema == null || schema.isBlank() ? null : schema;
    }

    private boolean isIdentityName(String tableName, String columnName) {
        String normalizedTable = normalizeName(tableName);
        String normalizedColumn = normalizeName(columnName);
        return normalizedColumn.equals("id")
                || normalizedColumn.endsWith("_id")
                || normalizedTable.startsWith("sys_")
                || normalizedTable.startsWith("biz_")
                || normalizedTable.contains("user")
                || normalizedTable.contains("role")
                || normalizedTable.contains("menu")
                || normalizedTable.contains("file")
                || normalizedTable.contains("tree")
                || normalizedTable.contains("node")
                || normalizedTable.contains("dept")
                || normalizedTable.contains("org")
                || normalizedTable.contains("tenant");
    }

    private boolean isReferenceHint(String columnName) {
        String normalized = normalizeName(columnName);
        if (normalized.equals("id")) {
            return false;
        }
        if (isAuditColumn(normalized) || isPathColumn(normalized)) {
            return true;
        }
        if (normalized.endsWith("_id") || normalized.endsWith("_by") || normalized.endsWith("_ref")) {
            return true;
        }
        for (String hint : REFERENCE_HINTS) {
            if (normalized.contains(hint)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAuditColumn(String columnName) {
        String normalized = normalizeName(columnName);
        return AUDIT_COLUMNS.contains(normalized)
                || normalized.equals("created_by")
                || normalized.equals("updated_by")
                || normalized.equals("created_user_id")
                || normalized.equals("updated_user_id");
    }

    private boolean isPathColumn(String columnName) {
        String normalized = normalizeName(columnName);
        return normalized.contains("path")
                || normalized.endsWith("_ids")
                || normalized.endsWith("_path")
                || normalized.contains("ancestor")
                || normalized.contains("descendant");
    }

    private boolean isSystemTable(String tableName) {
        String normalized = normalizeName(tableName);
        if (GOVERNANCE_TABLES.contains(normalized)) {
            return true;
        }
        return normalized.startsWith("pg_")
                || normalized.startsWith("information_schema");
    }

    private String normalizeTableName(String tableName) {
        return tableName == null ? "" : tableName.toLowerCase(Locale.ROOT);
    }

    private String normalizeColumnName(String columnName) {
        return normalizeName(columnName);
    }

    private String normalizeName(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    public record DatabaseIdReconciliationPlan(
            boolean hasDetectedData,
            boolean needsMigration,
            DatabaseIdPosture targetPosture,
            Map<String, ManagedTable> availableTables,
            Map<String, LinkedHashMap<String, String>> idMappings
    ) {

        public boolean shouldBootstrap() {
            return !hasDetectedData || needsMigration;
        }
    }

    private record Discovery(
            Map<String, TableSnapshot> snapshots,
            Map<String, IdentityDomain> identityDomains,
            Map<String, ManagedTable> managedTables,
            Map<String, Set<String>> identityValues
    ) {
    }

    private record IdentityDomain(String tableName, String identityColumn) {
    }

    private record ManagedTable(
            String tableName,
            String businessType,
            String identityColumn,
            List<ColumnBinding> bindings
    ) {
    }

    private record ColumnBinding(String columnName, String targetTable, BindingKind kind, boolean pathLike) {
    }

    private enum BindingKind {
        EXPLICIT_FK,
        INFERRED_REFERENCE,
        SELF_REFERENCE,
        AUDIT_REFERENCE,
        PATH_REFERENCE
    }

    private record TableSnapshot(
            String tableName,
            List<String> primaryKeyColumns,
            Map<String, ColumnMetadata> columns
    ) {
    }

    private record ColumnMetadata(
            String columnName,
            int dataType,
            String typeName,
            int columnSize,
            boolean nullable
    ) {

        boolean isCharacterColumn() {
            return dataType == Types.VARCHAR || dataType == Types.CHAR || dataType == Types.LONGVARCHAR;
        }

        boolean isIntegralColumn() {
            return dataType == Types.BIGINT
                    || dataType == Types.INTEGER
                    || dataType == Types.SMALLINT
                    || dataType == Types.TINYINT
                    || dataType == Types.NUMERIC
                    || dataType == Types.DECIMAL;
        }

        boolean isBigIntColumn() {
            String normalizedType = typeName == null ? "" : typeName.toLowerCase(Locale.ROOT);
            return dataType == Types.BIGINT || "bigint".equals(normalizedType) || "int8".equals(normalizedType);
        }

        boolean isManagedIdentifierColumn() {
            return isCharacterColumn() || isIntegralColumn();
        }

        String renderTypeDefinition(int targetSize) {
            String normalizedType = typeName == null ? "varchar" : typeName.toLowerCase(Locale.ROOT);
            if (targetSize > 0 && normalizedType.contains("char")) {
                return normalizedType + "(" + targetSize + ")";
            }
            if (columnSize > 0 && (normalizedType.contains("char") || normalizedType.contains("text"))) {
                if (normalizedType.contains("text")) {
                    return normalizedType;
                }
                return normalizedType + "(" + columnSize + ")";
            }
            return normalizedType;
        }
    }

    private static final class TableSnapshotBuilder {

        private final String tableName;
        private final List<String> primaryKeyColumns = new ArrayList<>();
        private final Map<String, ColumnMetadata> columns = new LinkedHashMap<>();

        private TableSnapshotBuilder(String tableName) {
            this.tableName = tableName;
        }

        String tableName() {
            return tableName;
        }

        void addPrimaryKey(String columnName) {
            primaryKeyColumns.add(columnName);
        }

        void addColumn(String columnName, int dataType, String typeName, int columnSize, boolean nullable) {
            columns.put(
                    columnName.toLowerCase(Locale.ROOT),
                    new ColumnMetadata(columnName, dataType, typeName, columnSize, nullable)
            );
        }

        TableSnapshot build() {
            return new TableSnapshot(tableName, List.copyOf(primaryKeyColumns), Map.copyOf(columns));
        }
    }

    private static final class ManagedTableBuilder {

        private final String tableName;
        private final String businessType;
        private final String identityColumn;
        private final Map<String, ColumnBinding> bindings = new LinkedHashMap<>();

        private ManagedTableBuilder(String tableName, String businessType, String identityColumn) {
            this.tableName = tableName;
            this.businessType = businessType;
            this.identityColumn = identityColumn;
        }

        void addBinding(ColumnBinding binding) {
            String key = binding.columnName().toLowerCase(Locale.ROOT) + "|" + binding.targetTable().toLowerCase(Locale.ROOT)
                    + "|" + binding.kind().name() + "|" + binding.pathLike();
            bindings.putIfAbsent(key, binding);
        }

        boolean hasBinding(String columnName) {
            String normalized = columnName.toLowerCase(Locale.ROOT);
            for (ColumnBinding binding : bindings.values()) {
                if (binding.columnName().toLowerCase(Locale.ROOT).equals(normalized)) {
                    return true;
                }
            }
            return false;
        }

        ManagedTable build() {
            List<ColumnBinding> orderedBindings = bindings.values().stream()
                    .sorted(Comparator.comparing(ColumnBinding::columnName))
                    .toList();
            return new ManagedTable(tableName, businessType, identityColumn, List.copyOf(orderedBindings));
        }
    }

    private static final class ForeignKeyGuard implements AutoCloseable {

        private final Connection connection;
        private final String databaseProduct;
        private final List<ForeignKeyConstraint> droppedConstraints;

        private ForeignKeyGuard(Connection connection, String databaseProduct, List<ForeignKeyConstraint> droppedConstraints) {
            this.connection = connection;
            this.databaseProduct = databaseProduct;
            this.droppedConstraints = droppedConstraints;
        }

        static ForeignKeyGuard open(Connection connection, Collection<String> managedTables) throws SQLException {
            String databaseProduct = connection.getMetaData().getDatabaseProductName().toLowerCase(Locale.ROOT);
            if (databaseProduct.contains("h2")) {
                execute(connection, "SET REFERENTIAL_INTEGRITY FALSE");
                return new ForeignKeyGuard(connection, databaseProduct, List.of());
            }
            if (databaseProduct.contains("mysql")) {
                execute(connection, "SET FOREIGN_KEY_CHECKS = 0");
                return new ForeignKeyGuard(connection, databaseProduct, List.of());
            }
            if (databaseProduct.contains("postgresql")) {
                List<ForeignKeyConstraint> constraints = loadForeignKeys(connection, managedTables);
                for (ForeignKeyConstraint constraint : constraints) {
                    execute(connection, constraint.dropSql());
                }
                return new ForeignKeyGuard(connection, databaseProduct, constraints);
            }
            return new ForeignKeyGuard(connection, databaseProduct, List.of());
        }

        @Override
        public void close() throws SQLException {
            if (databaseProduct.contains("h2")) {
                execute(connection, "SET REFERENTIAL_INTEGRITY TRUE");
                return;
            }
            if (databaseProduct.contains("mysql")) {
                execute(connection, "SET FOREIGN_KEY_CHECKS = 1");
                return;
            }
            if (databaseProduct.contains("postgresql")) {
                for (ForeignKeyConstraint constraint : droppedConstraints) {
                    execute(connection, constraint.addSql());
                }
            }
        }

        private static List<ForeignKeyConstraint> loadForeignKeys(Connection connection, Collection<String> managedTables)
                throws SQLException {
            Map<String, ForeignKeyConstraintBuilder> builders = new LinkedHashMap<>();
            for (String tableName : managedTables) {
                try (ResultSet resultSet = connection.getMetaData().getImportedKeys(null, null, tableName)) {
                    while (resultSet.next()) {
                        String fkName = resultSet.getString("FK_NAME");
                        if (fkName == null || fkName.isBlank()) {
                            continue;
                        }
                        String fkTable = resultSet.getString("FKTABLE_NAME");
                        String pkTable = resultSet.getString("PKTABLE_NAME");
                        short updateRule = resultSet.getShort("UPDATE_RULE");
                        short deleteRule = resultSet.getShort("DELETE_RULE");
                        String key = fkTable + ":" + fkName;
                        ForeignKeyConstraintBuilder builder = builders.computeIfAbsent(
                                key,
                                ignored -> new ForeignKeyConstraintBuilder(
                                        fkName,
                                        fkTable,
                                        pkTable,
                                        updateRule,
                                        deleteRule
                                )
                        );
                        builder.addColumn(
                                resultSet.getShort("KEY_SEQ"),
                                resultSet.getString("FKCOLUMN_NAME"),
                                resultSet.getString("PKCOLUMN_NAME")
                        );
                    }
                }
            }
            return builders.values().stream()
                    .map(ForeignKeyConstraintBuilder::build)
                    .sorted(Comparator.comparing(ForeignKeyConstraint::fkTable).thenComparing(ForeignKeyConstraint::name))
                    .toList();
        }

        private static void execute(Connection connection, String sql) throws SQLException {
            try (Statement statement = connection.createStatement()) {
                statement.execute(sql);
            }
        }
    }

    private record ForeignKeyConstraint(
            String name,
            String fkTable,
            String pkTable,
            List<ForeignKeyColumn> columns,
            short updateRule,
            short deleteRule
    ) {

        String dropSql() {
            return "ALTER TABLE " + fkTable + " DROP CONSTRAINT " + name;
        }

        String addSql() {
            String fkColumns = joinColumns(columns.stream().map(ForeignKeyColumn::fkColumn).toList());
            String pkColumns = joinColumns(columns.stream().map(ForeignKeyColumn::pkColumn).toList());
            return "ALTER TABLE " + fkTable
                    + " ADD CONSTRAINT " + name
                    + " FOREIGN KEY (" + fkColumns + ") REFERENCES " + pkTable + " (" + pkColumns + ")"
                    + renderRule(" ON UPDATE ", updateRule)
                    + renderRule(" ON DELETE ", deleteRule);
        }

        private static String joinColumns(List<String> columns) {
            StringJoiner joiner = new StringJoiner(", ");
            for (String column : columns) {
                joiner.add(column);
            }
            return joiner.toString();
        }

        private static String renderRule(String prefix, short rule) {
            return switch (rule) {
                case DatabaseMetaData.importedKeyCascade -> prefix + "CASCADE";
                case DatabaseMetaData.importedKeySetNull -> prefix + "SET NULL";
                case DatabaseMetaData.importedKeySetDefault -> prefix + "SET DEFAULT";
                case DatabaseMetaData.importedKeyRestrict -> prefix + "RESTRICT";
                default -> "";
            };
        }
    }

    private record ForeignKeyColumn(short keySeq, String fkColumn, String pkColumn) {
    }

    private static final class ForeignKeyConstraintBuilder {

        private final String name;
        private final String fkTable;
        private final String pkTable;
        private final short updateRule;
        private final short deleteRule;
        private final List<ForeignKeyColumn> columns = new ArrayList<>();

        private ForeignKeyConstraintBuilder(String name, String fkTable, String pkTable, short updateRule, short deleteRule) {
            this.name = name;
            this.fkTable = fkTable;
            this.pkTable = pkTable;
            this.updateRule = updateRule;
            this.deleteRule = deleteRule;
        }

        void addColumn(short keySeq, String fkColumn, String pkColumn) {
            columns.add(new ForeignKeyColumn(keySeq, fkColumn, pkColumn));
        }

        ForeignKeyConstraint build() {
            List<ForeignKeyColumn> orderedColumns = columns.stream()
                    .sorted(Comparator.comparingInt(ForeignKeyColumn::keySeq))
                    .toList();
            return new ForeignKeyConstraint(name, fkTable, pkTable, orderedColumns, updateRule, deleteRule);
        }
    }
}
