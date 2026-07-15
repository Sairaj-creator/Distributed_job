package com.taskflow.testsupport;

import com.taskflow.config.ConfigService;
import com.taskflow.persistence.ConnectionManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseTestSupport {
    private DatabaseTestSupport() {
    }

    public static ConnectionManager migratedConnectionManager() {
        ConfigService configService = new ConfigService();
        System.setProperty("TASKFLOW_DB_URL", "jdbc:h2:mem:taskflow_test_" + System.nanoTime() + ";DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
        System.setProperty("TASKFLOW_DB_USER", "sa");
        System.setProperty("TASKFLOW_DB_PASSWORD", "");
        ConnectionManager connectionManager = new ConnectionManager(configService);
        try (Connection connection = connectionManager.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("DROP ALL OBJECTS");
            for (String sql : migrationSql().split(";")) {
                if (!sql.isBlank()) {
                    statement.execute(sql);
                }
            }
            return connectionManager;
        } catch (SQLException ex) {
            connectionManager.close();
            throw new IllegalStateException("failed to migrate test database", ex);
        }
    }

    private static String migrationSql() {
        String path = "com/taskflow/persistence/migration/V1__init_schema.sql";
        try (InputStream stream = DatabaseTestSupport.class.getClassLoader().getResourceAsStream(path)) {
            if (stream == null) {
                throw new IllegalStateException("missing migration " + path);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("failed to read migration", ex);
        }
    }
}
