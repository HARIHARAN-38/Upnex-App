package com.upnext.app.data;

import com.upnext.app.config.DatabaseConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Provides JDBC connections to the application's database.
 * Implements a simple connection pooling mechanism for better performance.
 */
public class JdbcConnectionProvider {
    // Singleton instance
    private static JdbcConnectionProvider instance;
    
    // Connection pool settings
    private static final int MIN_POOL_SIZE = 3;
    private static final int MAX_POOL_SIZE = 10;
    private static final int CONNECTION_TIMEOUT_MS = 5000;
    
    // Connection pool
    private final ConcurrentLinkedQueue<Connection> connectionPool;
    private final AtomicInteger activeConnections;
    
    /**
     * Private constructor to enforce singleton pattern.
     * 
     * @throws SQLException If there's an error initializing the connection pool
     */
    private JdbcConnectionProvider() throws SQLException {
        // Initialize the database configuration
        DatabaseConfig.initialize();
        
        // Load the JDBC driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC driver not found", e);
        }
        
        // Initialize the connection pool
        connectionPool = new ConcurrentLinkedQueue<>();
        activeConnections = new AtomicInteger(0);
        
        // Pre-populate the connection pool with MIN_POOL_SIZE connections
        for (int i = 0; i < MIN_POOL_SIZE; i++) {
            connectionPool.offer(createConnection());
            activeConnections.incrementAndGet();
        }
    }
    
    /**
     * Gets the singleton instance of the connection provider.
     * 
     * @return The connection provider instance
     * @throws SQLException If there's an error creating the instance
     */
    public static synchronized JdbcConnectionProvider getInstance() throws SQLException {
        if (instance == null) {
            instance = new JdbcConnectionProvider();
        }
        return instance;
    }
    
    /**
     * Gets a connection from the pool. If the pool is empty and the active connection
     * count is less than MAX_POOL_SIZE, a new connection is created.
     * 
     * @return A database connection
     * @throws SQLException If there's an error getting a connection
     */
    public Connection getConnection() throws SQLException {
        Connection connection = connectionPool.poll();
        
        if (connection == null) {
            // If no connection is available in the pool
            if (activeConnections.get() < MAX_POOL_SIZE) {
                // Create a new connection if under the maximum pool size
                connection = createConnection();
                activeConnections.incrementAndGet();
            } else {
                // Wait for a connection to become available
                long waitStartTime = System.currentTimeMillis();
                while (connection == null) {
                    if (System.currentTimeMillis() - waitStartTime > CONNECTION_TIMEOUT_MS) {
                        throw new SQLException("Timed out waiting for a database connection");
                    }
                    
                    try {
                        Thread.sleep(100); // Wait a bit before checking again
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new SQLException("Interrupted while waiting for a connection", e);
                    }
                    
                    connection = connectionPool.poll();
                }
            }
        }
        
        // Validate the connection
        if (!isValid(connection)) {
            // If the connection is not valid, create a new one
            connection.close();
            connection = createConnection();
        }
        
        return connection;
    }
    
    /**
     * Releases a connection back to the pool.
     * 
     * @param connection The connection to release
     */
    public void releaseConnection(Connection connection) {
        if (connection != null) {
            connectionPool.offer(connection);
        }
    }
    
    /**
     * Checks if a connection is valid.
     * 
     * @param connection The connection to check
     * @return True if the connection is valid, false otherwise
     */
    private boolean isValid(Connection connection) {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(1);
        } catch (SQLException e) {
            return false;
        }
    }
    
    /**
     * Creates a new database connection.
     * 
     * @return A new database connection
     * @throws SQLException If there's an error creating the connection
     */
    private Connection createConnection() throws SQLException {
        return DriverManager.getConnection(
                DatabaseConfig.getJdbcUrl(),
                DatabaseConfig.getUser(),
                DatabaseConfig.getPassword());
    }
    
    /**
     * Closes all connections in the pool.
     * 
     * @throws SQLException If there's an error closing the connections
     */
    public void closeAllConnections() throws SQLException {
        Connection connection;
        while ((connection = connectionPool.poll()) != null) {
            connection.close();
            activeConnections.decrementAndGet();
        }
    }
    
    /**
     * Gets the number of active connections.
     * 
     * @return The number of active connections
     */
    public int getActiveConnectionCount() {
        return activeConnections.get();
    }
    
    /**
     * Gets the number of available connections in the pool.
     * 
     * @return The number of available connections
     */
    public int getAvailableConnectionCount() {
        return connectionPool.size();
    }
}