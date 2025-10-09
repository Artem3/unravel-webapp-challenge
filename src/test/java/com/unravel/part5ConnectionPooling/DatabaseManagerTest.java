package com.unravel.part5ConnectionPooling;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Slf4j
class DatabaseManagerTest {

    @Mock
    private HikariDataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private HikariPoolMXBean poolBean;

    @InjectMocks
    private DatabaseManager databaseManager;

    @Test
    void testGetConnection_SuccessWithShortDuration() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);

        Connection result = databaseManager.getConnection();

        verify(dataSource).getConnection();
        assertThat(result).isEqualTo(connection);
    }

    @Test
    void testGetConnection_LongDurationLogsWarn() throws SQLException {
        when(dataSource.getConnection()).thenAnswer(invocation -> {
            Thread.sleep(5100);
            return connection;
        });

        Connection result = databaseManager.getConnection();

        verify(dataSource).getConnection();
        assertThat(result).isEqualTo(connection);
    }

    @Test
    void testCloseConnection_Success() throws SQLException {
        when(connection.isClosed()).thenReturn(false);

        databaseManager.closeConnection(connection);

        verify(connection).close();
    }

    @Test
    void testCloseConnection_AlreadyClosed() throws SQLException {
        when(connection.isClosed()).thenReturn(true);

        databaseManager.closeConnection(connection);

        verify(connection, never()).close();
    }

    @Test
    void testInitPool_Success() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);

        databaseManager.initPool();

        verify(dataSource).getConnection();
        verify(connection).close();
    }

    @Test
    void testMonitorPool_WithWaiting_IncreasesMax() {
        when(dataSource.getHikariPoolMXBean()).thenReturn(poolBean);
        when(poolBean.getActiveConnections()).thenReturn(5);
        when(poolBean.getIdleConnections()).thenReturn(5);
        when(poolBean.getThreadsAwaitingConnection()).thenReturn(10);
        when(poolBean.getTotalConnections()).thenReturn(10);
        when(dataSource.getMaximumPoolSize()).thenReturn(20);

        databaseManager.monitorPool();

        verify(dataSource).setMaximumPoolSize(30);
    }

    @Test
    void testMonitorPool_Underutilized_DecreasesMin() {
        when(dataSource.getHikariPoolMXBean()).thenReturn(poolBean);
        when(poolBean.getActiveConnections()).thenReturn(0);
        when(poolBean.getIdleConnections()).thenReturn(16);  // > 10*1.5=15
        when(poolBean.getThreadsAwaitingConnection()).thenReturn(0);
        when(poolBean.getTotalConnections()).thenReturn(16);
        when(dataSource.getMinimumIdle()).thenReturn(10);
        databaseManager.monitorPool();
        verify(dataSource).setMinimumIdle(8);
    }

    @Test
    void testMonitorPool_PoolNull_Skips() {
        when(dataSource.getHikariPoolMXBean()).thenReturn(null);

        databaseManager.monitorPool();
    }
}