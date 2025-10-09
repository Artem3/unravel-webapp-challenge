package com.unravel.part5ConnectionPooling;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseManager {

    private final HikariDataSource dataSource;

    @PostConstruct
    public void initPool() {
        try (Connection conn = dataSource.getConnection()) {
            log.info("Hikari pool initialized successfully during @PostConstruct");
        } catch (SQLException e) {
            log.error("Failed to initialize Hikari pool during @PostConstruct", e);
        }
    }

    /**
     * Retrieves a connection from the pool and logs if acquisition takes too long.
     *
     * @return the database connection
     * @throws SQLException if a connection cannot be obtained
     */
    public Connection getConnection() throws SQLException {
        long start = System.currentTimeMillis();
        Connection conn = dataSource.getConnection();
        long duration = System.currentTimeMillis() - start;
        if (duration > 5000) {
            log.warn("Connection acquisition took too long: {} ms", duration);
        }
        return conn;
    }

    /**
     * Closes the connection if it's open, handling any exceptions.
     *
     * @param connection the connection to close
     */
    public void closeConnection(Connection connection) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            log.error("Error closing connection", e);
        }
    }

    /**
     * Monitors the connection pool metrics periodically and logs issues like waiting connections or underutilization.
     * Includes simple dynamic optimization logic.
     */
    @Scheduled(fixedRate = 2000)  // Run every 2 seconds
    public void monitorPool() {
        HikariPoolMXBean poolBean = dataSource.getHikariPoolMXBean();
        if (poolBean == null) {
            log.info("Pool not initialized yet (no connections requested) - skipping monitoring");
            return;
        }

        int active = poolBean.getActiveConnections();
        int idle = poolBean.getIdleConnections();
        int waiting = poolBean.getThreadsAwaitingConnection();
        int total = poolBean.getTotalConnections();

        // current metrics every time
        log.info("Current metrics: active={}, idle={}, total={}, waiting={}", active, idle, total, waiting);

        if (waiting > 0) {
            log.warn("Connections are waiting: {} - increasing max pool size", waiting);
            // increase max pool size if waiting (but cap at 150 to avoid overload)
            int currentMax = dataSource.getMaximumPoolSize();
            int newMax = Math.min(currentMax + 10, 150);
            if (newMax > currentMax) {
                dataSource.setMaximumPoolSize(newMax);
                log.info("Adjusted maximum-pool-size to {}", newMax);
            }
        }

        if (idle > dataSource.getMinimumIdle() * 1.5 && active < 10) {
            log.info("Pool is underutilized: idle={}, active={} - decreasing min idle", idle, active);
            // decrease min idle if underutilized (but floor at 5)
            int currentMin = dataSource.getMinimumIdle();
            int newMin = Math.max(currentMin - 2, 5);
            if (newMin < currentMin) {
                dataSource.setMinimumIdle(newMin);
                log.info("Adjusted minimum-idle to {}", newMin);
            }
        }
    }
}