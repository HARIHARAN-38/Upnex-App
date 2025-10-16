package com.upnext.app.data;

import com.upnext.app.core.Logger;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * Utility class for database schema initialization.
 * Verifies and sets up the required database schema.
 */
public class SchemaInitializer {
    private static final Logger logger = Logger.getInstance();
    private static final String SCHEMA_FILE = "/db/schema.sql";
    
    private SchemaInitializer() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Initializes the database schema.
     * 
     * @return true if initialization was successful, false otherwise
     */
    public static boolean initialize() {
        logger.info("Initializing database schema...");
        
        // Load schema file
        try (InputStream schemaStream = SchemaInitializer.class.getResourceAsStream(SCHEMA_FILE)) {
            if (schemaStream == null) {
                logger.error("Schema file not found: " + SCHEMA_FILE);
                return false;
            }
            
            // Read schema content
            String schemaContent;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(schemaStream))) {
                schemaContent = reader.lines().collect(Collectors.joining("\n"));
            }
            
            // Execute schema
            try {
                // Get a connection provider instance
                JdbcConnectionProvider provider = JdbcConnectionProvider.getInstance();
                // Get a database connection
                Connection connection = provider.getConnection();
                
                try {
                    try (Statement stmt = connection.createStatement()) {
                        // Split on semicolons to get individual statements
                        String[] statements = schemaContent.split(";");
                        for (String statement : statements) {
                            String trimmedStmt = statement.trim();
                            if (!trimmedStmt.isEmpty()) {
                                stmt.execute(trimmedStmt);
                            }
                        }
                    }
                    
                    logger.info("Database schema initialized successfully");
                    return true;
                } catch (SQLException e) {
                    logger.logException("SQL error initializing database schema", e);
                    return false;
                } finally {
                    // Make sure to release the connection
                    provider.releaseConnection(connection);
                }
            } catch (SQLException e) {
                logger.logException("Failed to get database connection", e);
                return false;
            }
        } catch (Exception e) {
            logger.logException("Failed to initialize database schema", e);
            return false;
        }
    }
    
    /**
     * Main method for standalone execution.
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        if (initialize()) {
            System.out.println("Database schema verified and initialized successfully");
            System.exit(0);
        } else {
            System.err.println("Failed to initialize database schema");
            System.exit(1);
        }
    }
}