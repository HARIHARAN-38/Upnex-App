package com.upnext.app.data.validation;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.upnext.app.core.Logger;
import com.upnext.app.data.JdbcConnectionProvider;

/**
 * Validates the database schema against the requirements for the Question Answering system.
 * Checks for proper table structure, foreign key relationships, and indexes.
 */
public class DatabaseSchemaValidator {
    private static final Logger LOGGER = Logger.getInstance();
    
    /**
     * Results of schema validation
     */
    public static class ValidationResult {
        private final boolean isValid;
        private final List<String> errors;
        private final List<String> warnings;
        private final Map<String, String> connectionInfo;
        
        public ValidationResult(boolean isValid, List<String> errors, List<String> warnings, Map<String, String> connectionInfo) {
            this.isValid = isValid;
            this.errors = new ArrayList<>(errors);
            this.warnings = new ArrayList<>(warnings);
            this.connectionInfo = new HashMap<>(connectionInfo);
        }
        
        public boolean isValid() { return isValid; }
        public List<String> getErrors() { return new ArrayList<>(errors); }
        public List<String> getWarnings() { return new ArrayList<>(warnings); }
        public Map<String, String> getConnectionInfo() { return new HashMap<>(connectionInfo); }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Database Schema Validation Result:\n");
            sb.append("Valid: ").append(isValid).append("\n");
            sb.append("Connection Info: ").append(connectionInfo).append("\n");
            
            if (!errors.isEmpty()) {
                sb.append("Errors:\n");
                for (String error : errors) {
                    sb.append("  - ").append(error).append("\n");
                }
            }
            
            if (!warnings.isEmpty()) {
                sb.append("Warnings:\n");
                for (String warning : warnings) {
                    sb.append("  - ").append(warning).append("\n");
                }
            }
            
            return sb.toString();
        }
    }
    
    /**
     * Expected table definitions with required columns
     */
    private static final Map<String, List<String>> REQUIRED_TABLES = new HashMap<>();
    static {
        REQUIRED_TABLES.put("users", Arrays.asList(
            "id", "name", "email", "password_hash", "salt", "active", 
            "questions_asked", "answers_given", "total_upvotes", "created_at", "updated_at"
        ));
        
        REQUIRED_TABLES.put("skills", Arrays.asList(
            "skill_id", "user_id", "skill_name", "description", "proficiency_level", "created_at", "updated_at"
        ));
        
        REQUIRED_TABLES.put("subjects", Arrays.asList(
            "id", "name", "description"
        ));
        
        REQUIRED_TABLES.put("tags", Arrays.asList(
            "id", "name", "usage_count"
        ));
        
        REQUIRED_TABLES.put("questions", Arrays.asList(
            "id", "user_id", "subject_id", "title", "content", "context", 
            "upvotes", "downvotes", "answer_count", "is_solved", "view_count", "created_at", "updated_at"
        ));
        
        REQUIRED_TABLES.put("question_tags", Arrays.asList(
            "question_id", "tag_id"
        ));
        
        REQUIRED_TABLES.put("answers", Arrays.asList(
            "id", "question_id", "user_id", "content", "is_accepted", 
            "upvotes", "downvotes", "created_at", "updated_at"
        ));
    }
    
    /**
     * Expected foreign key relationships
     */
    private static final Map<String, List<String>> REQUIRED_FOREIGN_KEYS = new HashMap<>();
    static {
        REQUIRED_FOREIGN_KEYS.put("skills", Arrays.asList("user_id -> users(id)"));
        REQUIRED_FOREIGN_KEYS.put("questions", Arrays.asList(
            "user_id -> users(id)", 
            "subject_id -> subjects(id)"
        ));
        REQUIRED_FOREIGN_KEYS.put("question_tags", Arrays.asList(
            "question_id -> questions(id)", 
            "tag_id -> tags(id)"
        ));
        REQUIRED_FOREIGN_KEYS.put("answers", Arrays.asList(
            "question_id -> questions(id)", 
            "user_id -> users(id)"
        ));
    }
    
    /**
     * Expected indexes for performance
     */
    private static final Map<String, List<String>> REQUIRED_INDEXES = new HashMap<>();
    static {
        REQUIRED_INDEXES.put("users", Arrays.asList("email"));
        REQUIRED_INDEXES.put("skills", Arrays.asList("user_id"));
        REQUIRED_INDEXES.put("tags", Arrays.asList("usage_count"));
        REQUIRED_INDEXES.put("questions", Arrays.asList("user_id", "subject_id", "created_at"));
        REQUIRED_INDEXES.put("answers", Arrays.asList("question_id", "user_id"));
    }
    
    /**
     * Validates the complete database schema
     */
    public static ValidationResult validateSchema() {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        Map<String, String> connectionInfo = new HashMap<>();
        
        try {
            JdbcConnectionProvider provider = JdbcConnectionProvider.getInstance();
            
            try (Connection connection = provider.getConnection()) {
                // Collect connection information
                collectConnectionInfo(connection, connectionInfo);
                
                // Test basic connectivity
                if (!testConnection(connection, errors)) {
                    return new ValidationResult(false, errors, warnings, connectionInfo);
                }
                
                // Validate table structure
                validateTableStructure(connection, errors, warnings);
                
                // Validate foreign key relationships
                validateForeignKeys(connection, errors, warnings);
                
                // Validate indexes
                validateIndexes(connection, errors, warnings);
                
                // Check data integrity
                validateDataIntegrity(connection, errors, warnings);
                
                provider.releaseConnection(connection);
            }
            
        } catch (SQLException e) {
            LOGGER.logException("Database schema validation failed", e);
            errors.add("Failed to connect to database: " + e.getMessage());
        }
        
        boolean isValid = errors.isEmpty();
        return new ValidationResult(isValid, errors, warnings, connectionInfo);
    }
    
    /**
     * Collects connection information for reporting
     */
    private static void collectConnectionInfo(Connection connection, Map<String, String> connectionInfo) {
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            connectionInfo.put("Database URL", metaData.getURL());
            connectionInfo.put("Database Product", metaData.getDatabaseProductName());
            connectionInfo.put("Database Version", metaData.getDatabaseProductVersion());
            connectionInfo.put("Driver Name", metaData.getDriverName());
            connectionInfo.put("Driver Version", metaData.getDriverVersion());
            connectionInfo.put("User Name", metaData.getUserName());
        } catch (SQLException e) {
            LOGGER.logException("Failed to collect connection info", e);
            connectionInfo.put("Error", "Failed to collect connection info: " + e.getMessage());
        }
    }
    
    /**
     * Tests basic database connectivity
     */
    private static boolean testConnection(Connection connection, List<String> errors) {
        try {
            if (connection == null || connection.isClosed()) {
                errors.add("Database connection is null or closed");
                return false;
            }
            
            if (!connection.isValid(5)) {
                errors.add("Database connection is not valid");
                return false;
            }
            
            // Test a simple query
            try (PreparedStatement stmt = connection.prepareStatement("SELECT 1")) {
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next() || rs.getInt(1) != 1) {
                        errors.add("Basic database query test failed");
                        return false;
                    }
                }
            }
            
            LOGGER.info("Database connection test passed");
            return true;
            
        } catch (SQLException e) {
            errors.add("Database connectivity test failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Validates that all required tables exist with correct columns
     */
    private static void validateTableStructure(Connection connection, List<String> errors, List<String> warnings) {
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            
            for (Map.Entry<String, List<String>> tableEntry : REQUIRED_TABLES.entrySet()) {
                String tableName = tableEntry.getKey();
                List<String> requiredColumns = tableEntry.getValue();
                
                // Check if table exists
                try (ResultSet tables = metaData.getTables(null, null, tableName, new String[]{"TABLE"})) {
                    if (!tables.next()) {
                        errors.add("Required table '" + tableName + "' does not exist");
                        continue;
                    }
                }
                
                // Check columns
                List<String> existingColumns = new ArrayList<>();
                try (ResultSet columns = metaData.getColumns(null, null, tableName, null)) {
                    while (columns.next()) {
                        existingColumns.add(columns.getString("COLUMN_NAME").toLowerCase());
                    }
                }
                
                for (String requiredColumn : requiredColumns) {
                    if (!existingColumns.contains(requiredColumn.toLowerCase())) {
                        errors.add("Table '" + tableName + "' is missing required column '" + requiredColumn + "'");
                    }
                }
                
                LOGGER.info("Table '" + tableName + "' structure validation completed");
            }
            
        } catch (SQLException e) {
            errors.add("Failed to validate table structure: " + e.getMessage());
        }
    }
    
    /**
     * Validates foreign key relationships
     */
    private static void validateForeignKeys(Connection connection, List<String> errors, List<String> warnings) {
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            
            for (Map.Entry<String, List<String>> fkEntry : REQUIRED_FOREIGN_KEYS.entrySet()) {
                String tableName = fkEntry.getKey();
                
                // Get existing foreign keys for the table
                List<String> existingFKs = new ArrayList<>();
                try (ResultSet fks = metaData.getImportedKeys(null, null, tableName)) {
                    while (fks.next()) {
                        String fkColumn = fks.getString("FKCOLUMN_NAME");
                        String pkTable = fks.getString("PKTABLE_NAME");
                        String pkColumn = fks.getString("PKCOLUMN_NAME");
                        existingFKs.add(fkColumn.toLowerCase() + " -> " + pkTable.toLowerCase() + "(" + pkColumn.toLowerCase() + ")");
                    }
                }
                
                // Check each required foreign key
                for (String requiredFK : fkEntry.getValue()) {
                    boolean found = existingFKs.stream()
                            .anyMatch(existingFK -> existingFK.equalsIgnoreCase(requiredFK));
                    
                    if (!found) {
                        warnings.add("Table '" + tableName + "' may be missing foreign key constraint: " + requiredFK);
                    }
                }
                
                LOGGER.info("Foreign key validation completed for table '" + tableName + "'");
            }
            
        } catch (SQLException e) {
            warnings.add("Failed to validate foreign keys: " + e.getMessage());
        }
    }
    
    /**
     * Validates that required indexes exist for performance
     */
    private static void validateIndexes(Connection connection, List<String> errors, List<String> warnings) {
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            
            for (Map.Entry<String, List<String>> indexEntry : REQUIRED_INDEXES.entrySet()) {
                String tableName = indexEntry.getKey();
                List<String> requiredIndexColumns = indexEntry.getValue();
                
                // Get existing indexes
                List<String> existingIndexColumns = new ArrayList<>();
                try (ResultSet indexes = metaData.getIndexInfo(null, null, tableName, false, false)) {
                    while (indexes.next()) {
                        String columnName = indexes.getString("COLUMN_NAME");
                        if (columnName != null) {
                            existingIndexColumns.add(columnName.toLowerCase());
                        }
                    }
                }
                
                // Check required indexes
                for (String requiredColumn : requiredIndexColumns) {
                    if (!existingIndexColumns.contains(requiredColumn.toLowerCase())) {
                        warnings.add("Table '" + tableName + "' may be missing index on column '" + requiredColumn + "' for performance");
                    }
                }
                
                LOGGER.info("Index validation completed for table '" + tableName + "'");
            }
            
        } catch (SQLException e) {
            warnings.add("Failed to validate indexes: " + e.getMessage());
        }
    }
    
    /**
     * Validates basic data integrity
     */
    private static void validateDataIntegrity(Connection connection, List<String> errors, List<String> warnings) {
        try {
            // Check for orphaned records in junction tables
            validateOrphanedRecords(connection, "question_tags", "question_id", "questions", "id", warnings);
            validateOrphanedRecords(connection, "question_tags", "tag_id", "tags", "id", warnings);
            validateOrphanedRecords(connection, "answers", "question_id", "questions", "id", warnings);
            validateOrphanedRecords(connection, "answers", "user_id", "users", "id", warnings);
            validateOrphanedRecords(connection, "questions", "user_id", "users", "id", warnings);
            validateOrphanedRecords(connection, "skills", "user_id", "users", "id", warnings);
            
            LOGGER.info("Data integrity validation completed");
            
        } catch (SQLException e) {
            warnings.add("Failed to validate data integrity: " + e.getMessage());
        }
    }
    
    /**
     * Checks for orphaned records between two tables
     */
    private static void validateOrphanedRecords(Connection connection, String childTable, String childColumn, 
                                               String parentTable, String parentColumn, List<String> warnings) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + childTable + " c " +
                    "LEFT JOIN " + parentTable + " p ON c." + childColumn + " = p." + parentColumn + " " +
                    "WHERE p." + parentColumn + " IS NULL AND c." + childColumn + " IS NOT NULL";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                int orphanedCount = rs.getInt(1);
                if (orphanedCount > 0) {
                    warnings.add("Found " + orphanedCount + " orphaned records in " + childTable + 
                               " referencing non-existent " + parentTable + " records");
                }
            }
        }
    }
}