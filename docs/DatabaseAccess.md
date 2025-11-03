# Database Access Documentation

## Overview
UpNext App uses a MySQL database with JDBC for data persistence. This document outlines the database structure, migration approach, and connection configuration.

## Connection Details
- **Database**: `upnex`
- **User**: `root`
- **Password**: `hari`
- **Host**: `127.0.0.1` (localhost)
- **Port**: Default (3306)

### Configuration Loading
- `DatabaseConfig` reads `config/database.properties` on startup when present.
- If the file is absent, the service uses the defaults listed above.
- Copy `config/database.properties.sample` to `config/database.properties` when you need to override credentials per environment.

## Schema Management
The application uses a schema initialization approach to create required database tables at startup:

1. Schema definitions are stored in `src/main/resources/db/schema.sql`
2. The `SchemaInitializer` class handles execution of schema statements
3. Tables are created if they don't exist (`CREATE TABLE IF NOT EXISTS`)

### Migration Strategy
When making schema changes (adding tables or columns), follow these steps:

1. Update the schema.sql file with new tables/columns
2. Run the SchemaInitializer to apply changes
3. Update corresponding domain models and repositories
4. If needed, write migration SQL for existing data (run manually or through migration script)

## Tables

### Users Table
- Primary user account information
- Contains metrics for home dashboard display
- Auto-incremented ID as primary key

### Skills Table
- User skills with proficiency levels
- Foreign key relationship to users table

### Questions Table
- Questions posted by users with enhanced metadata support
- Foreign key relationship to users table (`user_id`)
- Optional subject categorization (`subject_id`)
- **Enhanced Fields (Step 36-37):**
  - `context`: Optional background information for the question
  - `view_count`: Tracks question popularity
  - `upvotes`/`downvotes`: Community feedback metrics
  - `answer_count`: Denormalized count for performance
  - `is_solved`: Question resolution status

### Tags Table
- Reusable tag entities for question categorization
- `id`: Primary key (BIGINT AUTO_INCREMENT)
- `name`: Tag name (VARCHAR, unique)
- `usage_count`: Popularity tracking for analytics

### Question_Tags Table (Junction)
- Many-to-many relationship between questions and tags
- `question_id`: Foreign key to questions table
- `tag_id`: Foreign key to tags table
- Composite primary key for integrity
- **Transactional Support:** Maintains referential integrity during question creation

### Answers Table
- Answers to questions with enhanced voting system
- Foreign key relationships to questions and users tables
- Enhanced voting system with `upvotes`/`downvotes` columns
- `is_accepted`: Marks the accepted answer for question resolution

## Transactional Patterns (Add Question Feature)

### Question Creation with Tags
The Add Question feature implements sophisticated transactional operations to maintain data integrity:

**Transaction Flow:**
1. **Begin Transaction**: Auto-commit disabled for atomicity
2. **Insert Question**: Generate question ID with prepared statement
3. **Process Tags**: Upsert tags (insert new, reuse existing by name)
4. **Link Tags**: Insert question-tag relationships
5. **Commit/Rollback**: Ensure all operations succeed or none persist

**Key Implementation Details:**
```java
// QuestionRepository.saveWithTags() - Transactional method
public Question saveWithTags(Question question, List<String> tagNames) throws SQLException {
    Connection connection = null;
    try {
        connection = JdbcConnectionProvider.getInstance().getConnection();
        connection.setAutoCommit(false); // Begin transaction
        
        // 1. Insert question and get generated ID
        Question savedQuestion = insertQuestion(connection, question);
        
        // 2. Process tags (upsert existing, create new)
        List<Tag> tags = processTagsTransactionally(connection, tagNames);
        
        // 3. Link question to tags
        linkQuestionToTags(connection, savedQuestion.getId(), tags);
        
        connection.commit(); // Commit transaction
        return savedQuestion;
        
    } catch (SQLException e) {
        if (connection != null) connection.rollback();
        throw e;
    } finally {
        if (connection != null) {
            connection.setAutoCommit(true);
            JdbcConnectionProvider.getInstance().releaseConnection(connection);
        }
    }
}
```

### Tag Management Strategy
**Duplicate Prevention:**
- Tags are normalized to lowercase for consistency
- Database enforces unique constraint on tag names
- Application handles `SQLIntegrityConstraintViolationException` for duplicate insertions

**Performance Optimization:**
- Tag lookup by name uses indexed queries
- Batch tag insertion for multiple tags
- Connection reuse within transaction scope

## Connection Management
The application uses a connection pool managed by `JdbcConnectionProvider` to efficiently handle database connections:

1. Get a connection: `JdbcConnectionProvider.getInstance().getConnection()`
2. Execute SQL operations (with transaction management for complex operations)
3. Always release the connection when done: `JdbcConnectionProvider.getInstance().releaseConnection(connection)`

## Example Usage
```java
Connection connection = null;
PreparedStatement statement = null;
ResultSet resultSet = null;
        
try {
    connection = JdbcConnectionProvider.getInstance().getConnection();
    statement = connection.prepareStatement(SQL_QUERY);
    statement.setString(1, parameter);
            
    resultSet = statement.executeQuery();
    // Process results
} finally {
    // Clean up resources
    if (resultSet != null) resultSet.close();
    if (statement != null) statement.close();
    if (connection != null) {
        JdbcConnectionProvider.getInstance().releaseConnection(connection);
    }
}
```

### Migration History

**Migration 008 (Step 36)**: Add Question Context and Constraints
- Added nullable `context` column to questions table
- Enhanced question metadata support
- File: `sql/008_add_question_context_and_constraints.sql`

**Schema Evolution Notes:**
- All migrations use `IF NOT EXISTS` and `IF EXISTS` clauses for idempotency
- BIGINT consistency maintained across user_id foreign key relationships
- Tag system designed for scalability with proper indexing

### Driver Reference
- The MySQL JDBC driver (`mysql-connector-j-9.4.0.jar`) resides under `lib/`.
- `pom.xml` declares the driver with `system` scope to add it to the compile and runtime classpath.

### Best Practices for Add Question Feature

1. **Always use transactions for multi-table operations**
2. **Handle duplicate tag names gracefully with exception catching**
3. **Validate question data before database insertion**
4. **Use prepared statements to prevent SQL injection**
5. **Maintain referential integrity through proper foreign key constraints**
6. **Log database operations for debugging and analytics**

---

*Last Updated: October 26, 2025 - Added Add Question Schema Documentation (Step 44)*