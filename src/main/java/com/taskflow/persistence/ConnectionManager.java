package com.taskflow.persistence;

import com.taskflow.config.ConfigService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Owns the HikariCP data source used by JDBC repositories.
 */
public final class ConnectionManager implements AutoCloseable {
    private final HikariDataSource dataSource;

    public ConnectionManager(ConfigService configService) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(configService.get("taskflow.db.url"));
        config.setUsername(configService.get("taskflow.db.user"));
        config.setPassword(configService.get("taskflow.db.password"));
        config.setMaximumPoolSize(configService.getInt("taskflow.db.poolSize", 4));
        this.dataSource = new HikariDataSource(config);
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void close() {
        dataSource.close();
    }
}
