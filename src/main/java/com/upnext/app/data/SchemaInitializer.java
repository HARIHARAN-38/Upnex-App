package com.upnext.app.data;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import com.upnext.app.core.Logger;

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

    private static void ensureSeedUsers(Connection connection) {
        try (Statement stmt = connection.createStatement()) {
            try (ResultSet rs = connection.getMetaData().getTables(connection.getCatalog(), null, "users", null)) {
                // if users table exists, check row count
                boolean usersExist = rs.next();
                if (usersExist) {
                    try (ResultSet countRs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
                        if (countRs.next()) {
                            long cnt = countRs.getLong(1);
                            if (cnt == 0) {
                                // insert two default users used by tests
                                stmt.executeUpdate("INSERT INTO users (name,email,password_hash,salt) VALUES ('Test User1','user1@example.com','hash','salt')");
                                stmt.executeUpdate("INSERT INTO users (name,email,password_hash,salt) VALUES ('Test User2','user2@example.com','hash','salt')");
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            // best-effort seeding for tests; log and continue
            logger.logException("Failed to seed users", e);
        }
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
                JdbcConnectionProvider provider = JdbcConnectionProvider.getInstance();
                Connection connection = provider.getConnection();

                    try {
                        executeSchemaScript(connection, schemaContent);
                        verifyRequiredTables(connection);
                        // Execute migration scripts for existing databases
                        executeMigrations(connection);
                        // Ensure basic seed data exists for tests (users 1 and 2)
                        ensureSeedUsers(connection);
                        logger.info("Database schema initialized successfully");
                        return true;
                    } catch (SQLException e) {
                        logger.logException("SQL error initializing database schema", e);
                        return false;
                    } finally {
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
     * Executes migration scripts to update existing databases.
     * 
     * @param connection Database connection
     */
    private static void executeMigrations(Connection connection) {
        try {
            // Execute migration 008 to add context column and ensure constraints
            String migration008 = "/sql/008_add_question_context_and_constraints.sql";
            InputStream migrationStream = SchemaInitializer.class.getResourceAsStream(migration008);
            
            if (migrationStream != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(migrationStream))) {
                    String migrationContent = reader.lines().collect(Collectors.joining("\n"));
                    
                    try (Statement stmt = connection.createStatement()) {
                        // Execute migration statements
                        String[] statements = migrationContent.split(";");
                        for (String statement : statements) {
                            String trimmedStmt = statement.trim();
                            if (!trimmedStmt.isEmpty() && !trimmedStmt.startsWith("--")) {
                                try {
                                    stmt.execute(trimmedStmt);
                                } catch (SQLException e) {
                                    // Ignore "column already exists" errors (code 1060)
                                    if (e.getErrorCode() != 1060) {
                                        throw e;
                                    }
                                }
                            }
                        }
                        logger.info("Migration 008 executed successfully");
                    }
                } catch (Exception e) {
                    logger.logException("Failed to execute migration 008", e);
                }
            } else {
                logger.info("Migration 008 file not found, skipping migration");
            }
            
            // Execute migration 009 to create question_votes table for Reddit-like voting
            String migration009 = "/sql/009_create_question_votes_table.sql";
            InputStream migration009Stream = SchemaInitializer.class.getResourceAsStream(migration009);
            
            if (migration009Stream != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(migration009Stream))) {
                    String migrationContent = reader.lines().collect(Collectors.joining("\n"));
                    
                    try (Statement stmt = connection.createStatement()) {
                        // Execute migration statements
                        String[] statements = migrationContent.split(";");
                        for (String statement : statements) {
                            String trimmedStmt = statement.trim();
                            if (!trimmedStmt.isEmpty() && !trimmedStmt.startsWith("--")) {
                                try {
                                    stmt.execute(trimmedStmt);
                                } catch (SQLException e) {
                                    // Ignore "table already exists" errors (code 1050)
                                    if (e.getErrorCode() != 1050) {
                                        throw e;
                                    }
                                }
                            }
                        }
                        logger.info("Migration 009 executed successfully");
                    }
                } catch (Exception e) {
                    logger.logException("Failed to execute migration 009", e);
                }
            } else {
                logger.info("Migration 009 file not found, skipping migration");
            }
            
            // Execute migration 011 to add performance indexes
            String migration011 = "/sql/011_performance_indexes.sql";
            InputStream migration011Stream = SchemaInitializer.class.getResourceAsStream(migration011);
            
            if (migration011Stream != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(migration011Stream))) {
                    String migrationContent = reader.lines().collect(Collectors.joining("\n"));
                    
                    try (Statement stmt = connection.createStatement()) {
                        // Execute migration statements
                        String[] statements = migrationContent.split(";");
                        for (String statement : statements) {
                            String trimmedStmt = statement.trim();
                            if (!trimmedStmt.isEmpty() && !trimmedStmt.startsWith("--")) {
                                try {
                                    stmt.execute(trimmedStmt);
                                } catch (SQLException e) {
                                    // Ignore "index already exists" errors (code 1061)
                                    if (e.getErrorCode() != 1061) {
                                        logger.warning("Index creation warning: " + e.getMessage());
                                    }
                                }
                            }
                        }
                        logger.info("Migration 011 (performance indexes) executed successfully");
                    }
                } catch (Exception e) {
                    logger.logException("Failed to execute migration 011", e);
                }
            } else {
                logger.info("Migration 011 file not found, skipping migration");
            }
        } catch (Exception e) {
            logger.logException("Error during migration execution", e);
        }
    }
    
    private static void executeSchemaScript(Connection connection, String schemaContent) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Execute the schema script statements
            // The schema should use "CREATE TABLE IF NOT EXISTS" to avoid conflicts
            String[] statements = schemaContent.split(";");
            for (String statement : statements) {
                String trimmedStmt = statement.trim();
                if (!trimmedStmt.isEmpty()) {
                    try {
                        stmt.execute(trimmedStmt);
                    } catch (SQLException e) {
                        // Log the error but continue with other statements
                        logger.warning("Failed to execute schema statement: " + trimmedStmt + " - " + e.getMessage());
                    }
                }
            }
        }
    }

    private static void verifyRequiredTables(Connection connection) throws SQLException {
        Map<String, String> requiredTables = new LinkedHashMap<>();
        requiredTables.put("users",
                "CREATE TABLE IF NOT EXISTS users (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                "name VARCHAR(255) NOT NULL, " +
                "email VARCHAR(255) NOT NULL UNIQUE, " +
                "password_hash VARCHAR(255) NOT NULL, " +
                "salt VARCHAR(255) NOT NULL, " +
                "active BOOLEAN DEFAULT TRUE, " +
                "questions_asked INT NOT NULL DEFAULT 0, " +
                "answers_given INT NOT NULL DEFAULT 0, " +
                "total_upvotes INT NOT NULL DEFAULT 0, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                ");");

        requiredTables.put("skills",
                "CREATE TABLE IF NOT EXISTS skills (" +
                "skill_id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                "user_id BIGINT NOT NULL, " +
                "skill_name VARCHAR(100) NOT NULL, " +
                "description VARCHAR(255), " +
                "proficiency_level INT NOT NULL DEFAULT 1, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                "CONSTRAINT fk_skills_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, " +
                "INDEX idx_skills_user (user_id)" +
                ");");

        requiredTables.put("subjects",
                "CREATE TABLE IF NOT EXISTS subjects (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                "name VARCHAR(100) NOT NULL UNIQUE, " +
                "description VARCHAR(500)" +
                ");");

        requiredTables.put("tags",
                "CREATE TABLE IF NOT EXISTS tags (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                "name VARCHAR(50) NOT NULL UNIQUE, " +
                "usage_count INT NOT NULL DEFAULT 0, " +
                "INDEX idx_tags_usage_count (usage_count)" +
                ");");

        requiredTables.put("questions",
                "CREATE TABLE IF NOT EXISTS questions (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                "user_id BIGINT NOT NULL, " +
                "subject_id BIGINT, " +
                "title VARCHAR(255) NOT NULL, " +
                "content TEXT NOT NULL, " +
                "context TEXT, " +
                "upvotes INT NOT NULL DEFAULT 0, " +
                "downvotes INT NOT NULL DEFAULT 0, " +
                "answer_count INT NOT NULL DEFAULT 0, " +
                "is_solved BOOLEAN NOT NULL DEFAULT FALSE, " +
                "view_count INT NOT NULL DEFAULT 0, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                "CONSTRAINT fk_questions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, " +
                "CONSTRAINT fk_questions_subject FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE SET NULL, " +
                "INDEX idx_questions_user (user_id), " +
                "INDEX idx_questions_subject (subject_id), " +
                "INDEX idx_questions_created_at (created_at)" +
                ");");

        requiredTables.put("question_tags",
                "CREATE TABLE IF NOT EXISTS question_tags (" +
                "question_id BIGINT NOT NULL, " +
                "tag_id BIGINT NOT NULL, " +
                "PRIMARY KEY (question_id, tag_id), " +
                "CONSTRAINT fk_question_tags_question FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE, " +
                "CONSTRAINT fk_question_tags_tag FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE" +
                ");");

        requiredTables.put("answers",
                "CREATE TABLE IF NOT EXISTS answers (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                "question_id BIGINT NOT NULL, " +
                "user_id BIGINT NOT NULL, " +
                "content TEXT NOT NULL, " +
                "is_accepted BOOLEAN DEFAULT FALSE, " +
                "upvotes INT DEFAULT 0, " +
                "downvotes INT DEFAULT 0, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                "CONSTRAINT fk_answers_question FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE, " +
                "CONSTRAINT fk_answers_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, " +
                "INDEX idx_answers_question (question_id), " +
                "INDEX idx_answers_user (user_id)" +
                ");");

        try (Statement statement = connection.createStatement()) {
            DatabaseMetaData metaData = connection.getMetaData();

            for (Map.Entry<String, String> entry : requiredTables.entrySet()) {
                String tableName = entry.getKey();
                if (!tableExists(metaData, tableName)) {
                    logger.info("Creating missing table: " + tableName);
                    statement.execute(entry.getValue());
                }
            }
        }
    }

    private static boolean tableExists(DatabaseMetaData metaData, String tableName) throws SQLException {
        try (ResultSet rs = metaData.getTables(null, null, tableName, null)) {
            if (rs.next()) {
                return true;
            }
        }
        try (ResultSet rs = metaData.getTables(null, null, tableName.toUpperCase(Locale.ROOT), null)) {
            return rs.next();
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