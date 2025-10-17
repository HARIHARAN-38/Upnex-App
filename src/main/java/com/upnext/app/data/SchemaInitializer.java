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
    
    private static void executeSchemaScript(Connection connection, String schemaContent) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Drop any existing tables in the current schema to avoid
            // foreign-key incompatibility with leftover tables (e.g. from
            // previous runs or other projects). Disable FK checks during
            // the cleanup so drops succeed regardless of constraints.
            try {
                stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
            } catch (SQLException e) {
                // Some drivers may ignore this; continue anyway
            }

            java.sql.DatabaseMetaData meta = connection.getMetaData();
            try (ResultSet rs = meta.getTables(connection.getCatalog(), null, "%", new String[]{"TABLE"})) {
                java.util.List<String> tables = new java.util.ArrayList<>();
                while (rs.next()) {
                    String tbl = rs.getString("TABLE_NAME");
                    // Skip system tables if any (defensive)
                    if (tbl == null) continue;
                    tables.add(tbl);
                }

                // Drop all tables found
                for (String tbl : tables) {
                    try {
                        stmt.executeUpdate("DROP TABLE IF EXISTS `" + tbl + "`");
                    } catch (SQLException e) {
                        // ignore individual drop failures and continue
                    }
                }
            }

            try {
                stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
            } catch (SQLException e) {
                // ignore
            }

            // Now execute the schema script statements
            String[] statements = schemaContent.split(";");
            for (String statement : statements) {
                String trimmedStmt = statement.trim();
                if (!trimmedStmt.isEmpty()) {
                    stmt.execute(trimmedStmt);
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