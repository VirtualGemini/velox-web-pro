package com.velox.module.system.id.support;

import com.velox.framework.id.common.business.IdBusinessTypes;
import com.velox.framework.id.common.message.IdGeneratorCommonMessages;
import com.velox.framework.id.common.type.IdDatabaseInitModes;
import com.velox.framework.id.exception.VeloxIdGeneratorException;
import com.velox.framework.id.properties.VeloxIdProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class DatabaseIdSchemaBootstrapper {

    private static final Logger log = LoggerFactory.getLogger(DatabaseIdSchemaBootstrapper.class);
    private static final String TABLE_NAME = "sys_id_sequence";

    private final DataSource dataSource;
    private final VeloxIdProperties properties;
    private final DatabaseIdSchemaReconciler reconciler;

    public DatabaseIdSchemaBootstrapper(
            DataSource dataSource,
            VeloxIdProperties properties
    ) {
        this.dataSource = dataSource;
        this.properties = properties;
        this.reconciler = new DatabaseIdSchemaReconciler(properties);
    }

    public void bootstrap() {
        boolean updateMode = IdDatabaseInitModes.UPDATE.equals(properties.getDatabase().getInitMode());
        if (!updateMode) {
            return;
        }
        try (Connection connection = dataSource.getConnection()) {
            DatabaseIdSchemaReconciler.DatabaseIdReconciliationPlan reconciliationPlan = reconciler.plan(connection);
            if (!reconciliationPlan.shouldBootstrap()) {
                return;
            }

            if (!tableExists(connection)) {
                createTable(connection);
                log.info("Initialized {} for Velox database id sequencing", TABLE_NAME);
            }

            if (reconciliationPlan.needsMigration()) {
                reconciler.reconcile(connection, reconciliationPlan);
                log.info("Reconciled managed database ids to {}", reconciliationPlan.targetPosture().name().toLowerCase());
            }
            seedBusinessTypes(connection, determineSequenceSeeds(connection));
        } catch (SQLException exception) {
            throw new VeloxIdGeneratorException(IdGeneratorCommonMessages.DATABASE_ID_INITIALIZATION_FAILED, exception);
        }
    }

    private boolean tableExists(Connection connection) {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT business_type FROM " + TABLE_NAME + " WHERE 1 = 0")) {
            statement.executeQuery();
            return true;
        } catch (SQLException exception) {
            return false;
        }
    }

    private void createTable(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                CREATE TABLE IF NOT EXISTS sys_id_sequence (
                  business_type varchar(64) NOT NULL PRIMARY KEY,
                  current_value bigint NOT NULL DEFAULT 0,
                  create_time timestamp DEFAULT CURRENT_TIMESTAMP,
                  update_time timestamp DEFAULT CURRENT_TIMESTAMP
                )
                """)) {
            statement.executeUpdate();
        }
    }

    private void seedBusinessTypes(Connection connection, Map<String, Long> targetValues) throws SQLException {
        for (Map.Entry<String, Long> entry : targetValues.entrySet()) {
            String businessType = entry.getKey();
            long currentValue = entry.getValue();
            if (!hasBusinessType(connection, businessType)) {
                insertBusinessType(connection, businessType, currentValue);
                continue;
            }
            updateBusinessType(connection, businessType, currentValue);
        }
    }

    private boolean hasBusinessType(Connection connection, String businessType) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT business_type FROM sys_id_sequence WHERE business_type = ?")) {
            statement.setString(1, businessType);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private void insertBusinessType(Connection connection, String businessType, long currentValue) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO sys_id_sequence (business_type, current_value) VALUES (?, ?)")) {
            statement.setString(1, businessType);
            statement.setLong(2, currentValue);
            statement.executeUpdate();
        }
    }

    private void updateBusinessType(Connection connection, String businessType, long currentValue) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE sys_id_sequence SET current_value = ?, update_time = CURRENT_TIMESTAMP WHERE business_type = ? AND current_value < ?")) {
            statement.setLong(1, currentValue);
            statement.setString(2, businessType);
            statement.setLong(3, currentValue);
            statement.executeUpdate();
        }
    }

    private Map<String, Long> determineSequenceSeeds(Connection connection) throws SQLException {
        Map<String, Long> values = defaultSequenceSeeds(connection);
        if (properties.isEnabled()) {
            return values;
        }
        values.putAll(reconciler.resolveDatabaseSequenceValues(connection, values.keySet()));
        return values;
    }

    private Map<String, Long> defaultSequenceSeeds(Connection connection) throws SQLException {
        Map<String, Long> values = new LinkedHashMap<>();
        values.put(IdBusinessTypes.DEFAULT, 0L);
        for (String businessType : reconciler.resolveManagedBusinessTypes(connection)) {
            values.putIfAbsent(businessType, 0L);
        }
        return values;
    }
}
