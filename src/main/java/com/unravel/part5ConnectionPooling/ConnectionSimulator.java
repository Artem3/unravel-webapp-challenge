package com.unravel.part5ConnectionPooling;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Simulator for running sequential connection scenarios to test the pool under different loads.
 * Runs automatically after app startup via CommandLineRunner.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConnectionSimulator implements CommandLineRunner {

    private final DatabaseManager databaseManager;

    @Override
    public void run(String... args) throws Exception {
        log.info("-------------------------------------------");
        log.info("Starting connection simulation scenarios...");

        // Scenario 1: Low load - sequential requests
        simulateLowLoad(5);  // 5 requests in sequence
        Thread.sleep(5000);

        // Scenario 2: Medium load - some concurrency
        simulateMediumLoad(50, 15);  // 50 requests in 15 threads
        Thread.sleep(10000);

        // Scenario 3: High load - high concurrency
        simulateHighLoad(200, 60);  // 200 requests in 60 threads
        Thread.sleep(30000);

        log.info("All simulation scenarios completed.");
        log.info("-------------------------------------------");
    }

    // Helper: Perform a simple query to simulate DB work
    private void performQuery(Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT 1");
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                log.debug("Query result: {}", rs.getInt(1));  // Just to simulate work
            }
        }
    }

    // Scenario 1: Low load - sequential single-threaded requests
    private void simulateLowLoad(int requestCount) {
        log.info("");
        log.info("---Starting low load scenario: {} sequential requests", requestCount);
        for (int i = 0; i < requestCount; i++) {
            try (Connection conn = databaseManager.getConnection()) {
                performQuery(conn);
                Thread.sleep(100);  // Small delay to simulate processing
            } catch (SQLException | InterruptedException e) {
                log.error("Error in low load simulation", e);
            }
        }
        log.info("Low load scenario completed");
    }

    // Scenario 2: Medium load - concurrent requests
    private void simulateMediumLoad(int requestCount, int threadCount) {
        log.info("");
        log.info("--- Starting medium load scenario: {} requests in {} threads", requestCount, threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < requestCount; i++) {
            executor.submit(() -> {
                try (Connection conn = databaseManager.getConnection()) {
                    performQuery(conn);
                    Thread.sleep(700);  // Increased for pressure and duration
                } catch (SQLException | InterruptedException e) {
                    log.error("Error in medium load simulation", e);
                }
            });
        }
        shutdownExecutor(executor);
        log.info("--- Medium load scenario completed");
    }

    // Scenario 3: High load - high concurrency
    private void simulateHighLoad(int requestCount, int threadCount) {
        log.info("");
        log.info("---Starting high load scenario: {} requests in {} threads", requestCount, threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < requestCount; i++) {
            executor.submit(() -> {
                try (Connection conn = databaseManager.getConnection()) {
                    performQuery(conn);
                    Thread.sleep(1000);  // Increased for pressure
                } catch (SQLException | InterruptedException e) {
                    log.error("Error in high load simulation", e);
                }
            });
        }
        shutdownExecutor(executor);
        log.info("High load scenario completed");
        log.info("-------------------------------------------");
    }

    // shut down gracefully
    private void shutdownExecutor(ExecutorService executor) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(15, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}