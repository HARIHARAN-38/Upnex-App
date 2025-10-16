package com.upnext.app.data;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tests for the JdbcConnectionProvider class.
 * This class can be run manually to verify the connection provider works correctly.
 */
public class JdbcConnectionProviderTest {
    // Use fully qualified class name to avoid import issues
    private static com.upnext.app.data.JdbcConnectionProvider connectionProvider;
    private static final Logger LOGGER = Logger.getLogger(JdbcConnectionProviderTest.class.getName());
    
    /**
     * Main method to run the tests.
     * 
     * @param args Command-line arguments (not used)
     */
    public static void main(String[] args) {
        try {
            setup();
            JdbcConnectionProviderTest tester = new JdbcConnectionProviderTest();
            
            System.out.println("Running testGetConnection...");
            tester.testGetConnection();
            
            System.out.println("Running testSimpleQuery...");
            tester.testSimpleQuery();
            
            System.out.println("Running testConnectionPoolSize...");
            tester.testConnectionPoolSize();
            
            System.out.println("All tests passed!");
        } catch (SQLException e) {
            System.err.println("Test failed with SQLException: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "Test failed", e);
        } finally {
            try {
                cleanup();
            } catch (SQLException e) {
                System.err.println("Error during cleanup: " + e.getMessage());
                LOGGER.log(Level.SEVERE, "Cleanup failed", e);
            }
        }
    }
    
    /**
     * Set up the test environment.
     * 
     * @throws SQLException If there's an error during setup
     */
    public static void setup() throws SQLException {
        // Get the connection provider instance
        connectionProvider = com.upnext.app.data.JdbcConnectionProvider.getInstance();
    }
    
    /**
     * Clean up the test environment.
     * 
     * @throws SQLException If there's an error during cleanup
     */
    public static void cleanup() throws SQLException {
        // Close all connections in the pool
        if (connectionProvider != null) {
            connectionProvider.closeAllConnections();
        }
    }
    
    /**
     * Test getting a connection from the provider.
     * 
     * @throws SQLException If there's an error getting a connection
     */
    public void testGetConnection() throws SQLException {
        // Get a connection from the pool
        Connection connection = connectionProvider.getConnection();
        
        // Verify the connection is not null and valid
        if (connection == null) {
            throw new AssertionError("Connection should not be null");
        }
        
        if (!connection.isValid(1)) {
            throw new AssertionError("Connection should be valid");
        }
        
        // Release the connection back to the pool
        connectionProvider.releaseConnection(connection);
    }
    
    /**
     * Test executing a simple query.
     * 
     * @throws SQLException If there's an error executing the query
     */
    public void testSimpleQuery() throws SQLException {
        // Get a connection from the pool
        Connection connection = connectionProvider.getConnection();
        
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT 1")) {
            
            // Verify the query result
            if (!resultSet.next()) {
                throw new AssertionError("Result set should have at least one row");
            }
            
            int value = resultSet.getInt(1);
            if (value != 1) {
                throw new AssertionError("Result should be 1, but was: " + value);
            }
            
        } finally {
            // Release the connection back to the pool
            connectionProvider.releaseConnection(connection);
        }
    }
    
    /**
     * Test the connection pool size functionality.
     * 
     * @throws SQLException If there's an error during the test
     */
    public void testConnectionPoolSize() throws SQLException {
        // Verify the initial active connection count is at least the minimum pool size
        int activeCount = connectionProvider.getActiveConnectionCount();
        if (activeCount < 3) {
            throw new AssertionError("Initial active connection count should be at least 3, but was: " + activeCount);
        }
        
        // Get multiple connections
        Connection[] connections = new Connection[5];
        for (int i = 0; i < connections.length; i++) {
            connections[i] = connectionProvider.getConnection();
            if (connections[i] == null) {
                throw new AssertionError("Connection should not be null");
            }
        }
        
        // Release the connections back to the pool
        for (Connection connection : connections) {
            connectionProvider.releaseConnection(connection);
        }
        
        // Verify the available connection count
        int availableCount = connectionProvider.getAvailableConnectionCount();
        if (availableCount <= 0) {
            throw new AssertionError("Available connection count should be greater than 0, but was: " + availableCount);
        }
    }
}