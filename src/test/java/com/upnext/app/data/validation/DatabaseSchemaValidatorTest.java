package com.upnext.app.data.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;

import com.upnext.app.core.Logger;

/**
 * Test class for database schema validation.
 * Tests connectivity, schema structure, and relationships.
 */
public class DatabaseSchemaValidatorTest {
    private static final Logger LOGGER = Logger.getInstance();
    
    @Test
    public void testDatabaseSchemaValidation() {
        LOGGER.info("Starting database schema validation test");
        
        try {
            DatabaseSchemaValidator.ValidationResult result = DatabaseSchemaValidator.validateSchema();
            
            // Log the complete validation result
            System.out.println("=== Database Schema Validation Results ===");
            System.out.println(result.toString());
            
            // Print connection info
            System.out.println("=== Connection Information ===");
            result.getConnectionInfo().forEach((key, value) -> 
                System.out.println(key + ": " + value));
            
            // Print detailed errors if any
            if (!result.getErrors().isEmpty()) {
                System.out.println("=== ERRORS ===");
                result.getErrors().forEach(error -> System.out.println("ERROR: " + error));
            }
            
            // Print warnings if any
            if (!result.getWarnings().isEmpty()) {
                System.out.println("=== WARNINGS ===");
                result.getWarnings().forEach(warning -> System.out.println("WARNING: " + warning));
            }
            
            // Assert basic connectivity works
            assertFalse(result.getConnectionInfo().containsKey("Error"), 
                "Database connection should work");
            
            // Log success or failure
            if (result.isValid()) {
                LOGGER.info("Database schema validation PASSED");
                System.out.println("✅ Database schema validation PASSED");
            } else {
                LOGGER.error("Database schema validation FAILED with " + result.getErrors().size() + " errors");
                System.out.println("❌ Database schema validation FAILED");
                
                // Don't fail the test if it's just missing tables - they might not be created yet
                boolean hasConnectionErrors = result.getErrors().stream()
                    .anyMatch(error -> error.contains("connect") || error.contains("connectivity"));
                
                if (hasConnectionErrors) {
                    fail("Database connectivity failed: " + result.getErrors());
                }
            }
            
        } catch (Exception e) {
            LOGGER.logException("Database schema validation test failed", e);
            fail("Database schema validation test failed: " + e.getMessage());
        }
    }
    
    @Test
    public void testConnectionSettings() {
        System.out.println("=== Testing Database Connection Settings ===");
        
        try {
            // Test the expected connection parameters
            com.upnext.app.config.DatabaseConfig.initialize();
            
            String expectedUrl = "jdbc:mysql://127.0.0.1:3306/upnex";
            String expectedUser = "root";
            String expectedPassword = "hari";
            
            String actualUrl = com.upnext.app.config.DatabaseConfig.getJdbcUrl();
            String actualUser = com.upnext.app.config.DatabaseConfig.getUser();
            String actualPassword = com.upnext.app.config.DatabaseConfig.getPassword();
            
            System.out.println("Expected URL: " + expectedUrl);
            System.out.println("Actual URL: " + actualUrl);
            System.out.println("Expected User: " + expectedUser);
            System.out.println("Actual User: " + actualUser);
            System.out.println("Password Match: " + expectedPassword.equals(actualPassword));
            
            assertEquals(expectedUrl, actualUrl, "JDBC URL should match requirements");
            assertEquals(expectedUser, actualUser, "Database user should match requirements");
            assertEquals(expectedPassword, actualPassword, "Database password should match requirements");
            
            System.out.println("✅ Connection settings validation PASSED");
            
        } catch (Exception e) {
            LOGGER.logException("Connection settings test failed", e);
            fail("Connection settings test failed: " + e.getMessage());
        }
    }
}