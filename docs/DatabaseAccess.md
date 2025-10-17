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
- Questions posted by users
- Foreign key relationship to users table 
- Tagged with subjects and topics
- Contains voting counts

### Answers Table
- Answers to questions
- Foreign key relationships to questions and users tables
- Contains voting counts

## Connection Management
The application uses a connection pool managed by `JdbcConnectionProvider` to efficiently handle database connections:

1. Get a connection: `JdbcConnectionProvider.getInstance().getConnection()`
2. Execute SQL operations
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

### Driver Reference
- The MySQL JDBC driver (`mysql-connector-j-9.4.0.jar`) resides under `lib/`.
- `pom.xml` declares the driver with `system` scope to add it to the compile and runtime classpath.