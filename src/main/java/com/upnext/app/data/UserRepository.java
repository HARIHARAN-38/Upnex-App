package com.upnext.app.data;

import com.upnext.app.domain.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository for User entity database operations.
 * Handles CRUD operations for users in the database.
 */
public class UserRepository {
    // Singleton instance
    private static UserRepository instance;
    
    // SQL queries
    private static final String CREATE_TABLE_SQL = 
            "CREATE TABLE IF NOT EXISTS users (" +
            "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
            "name VARCHAR(255) NOT NULL, " +
            "email VARCHAR(255) NOT NULL UNIQUE, " +
            "password_hash VARCHAR(255) NOT NULL, " +
            "salt VARCHAR(255) NOT NULL, " +
            "active BOOLEAN DEFAULT TRUE, " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
    
    private static final String INSERT_USER_SQL = 
            "INSERT INTO users (name, email, password_hash, salt) " +
            "VALUES (?, ?, ?, ?)";
    
    private static final String FIND_BY_ID_SQL = 
            "SELECT * FROM users WHERE id = ?";
    
    private static final String FIND_BY_EMAIL_SQL = 
            "SELECT * FROM users WHERE email = ?";
    
    private static final String UPDATE_USER_SQL = 
            "UPDATE users SET name = ?, email = ?, password_hash = ?, " +
            "salt = ?, active = ? WHERE id = ?";
    
    private static final String DELETE_USER_SQL = 
            "DELETE FROM users WHERE id = ?";
    
    private static final String FIND_ALL_USERS_SQL = 
            "SELECT * FROM users";
    
    /**
     * Private constructor to enforce singleton pattern.
     */
    private UserRepository() {
        // Ensure table exists
        try {
            initializeTable();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize user table", e);
        }
    }
    
    /**
     * Gets the singleton instance of the repository.
     * 
     * @return The repository instance
     */
    public static synchronized UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }
    
    /**
     * Ensures that the users table exists in the database.
     * 
     * @throws SQLException If there's an error creating the table
     */
    private void initializeTable() throws SQLException {
        Connection connection = null;
        Statement statement = null;
        
        try {
            connection = JdbcConnectionProvider.getInstance().getConnection();
            statement = connection.createStatement();
            statement.execute(CREATE_TABLE_SQL);
        } finally {
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                JdbcConnectionProvider.getInstance().releaseConnection(connection);
            }
        }
    }
    
    /**
     * Saves a new user to the database.
     * 
     * @param user The user to save
     * @return The saved user with generated ID
     * @throws SQLException If there's an error saving the user
     */
    public User save(User user) throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet generatedKeys = null;
        
        try {
            connection = JdbcConnectionProvider.getInstance().getConnection();
            statement = connection.prepareStatement(INSERT_USER_SQL, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, user.getName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPasswordHash());
            statement.setString(4, user.getSalt());
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }
            
            generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                user.setId(generatedKeys.getLong(1));
                user.setCreatedAt(LocalDateTime.now().toString());
            } else {
                throw new SQLException("Creating user failed, no ID obtained.");
            }
            
            return user;
        } finally {
            if (generatedKeys != null) {
                generatedKeys.close();
            }
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                JdbcConnectionProvider.getInstance().releaseConnection(connection);
            }
        }
    }
    
    /**
     * Finds a user by their ID.
     * 
     * @param id The user ID to search for
     * @return An Optional containing the user if found, empty otherwise
     * @throws SQLException If there's an error querying the database
     */
    public Optional<User> findById(Long id) throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = JdbcConnectionProvider.getInstance().getConnection();
            statement = connection.prepareStatement(FIND_BY_ID_SQL);
            statement.setLong(1, id);
            
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(mapResultSetToUser(resultSet));
            } else {
                return Optional.empty();
            }
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                JdbcConnectionProvider.getInstance().releaseConnection(connection);
            }
        }
    }
    
    /**
     * Finds a user by their email address.
     * 
     * @param email The email to search for
     * @return An Optional containing the user if found, empty otherwise
     * @throws SQLException If there's an error querying the database
     */
    public Optional<User> findByEmail(String email) throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = JdbcConnectionProvider.getInstance().getConnection();
            statement = connection.prepareStatement(FIND_BY_EMAIL_SQL);
            statement.setString(1, email);
            
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(mapResultSetToUser(resultSet));
            } else {
                return Optional.empty();
            }
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                JdbcConnectionProvider.getInstance().releaseConnection(connection);
            }
        }
    }
    
    /**
     * Updates an existing user in the database.
     * 
     * @param user The user to update
     * @return true if the update was successful, false otherwise
     * @throws SQLException If there's an error updating the user
     */
    public boolean update(User user) throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;
        
        try {
            connection = JdbcConnectionProvider.getInstance().getConnection();
            statement = connection.prepareStatement(UPDATE_USER_SQL);
            statement.setString(1, user.getName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPasswordHash());
            statement.setString(4, user.getSalt());
            statement.setBoolean(5, user.isActive());
            statement.setLong(6, user.getId());
            
            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;
        } finally {
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                JdbcConnectionProvider.getInstance().releaseConnection(connection);
            }
        }
    }
    
    /**
     * Deletes a user from the database.
     * 
     * @param id The ID of the user to delete
     * @return true if the deletion was successful, false otherwise
     * @throws SQLException If there's an error deleting the user
     */
    public boolean delete(Long id) throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;
        
        try {
            connection = JdbcConnectionProvider.getInstance().getConnection();
            statement = connection.prepareStatement(DELETE_USER_SQL);
            statement.setLong(1, id);
            
            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;
        } finally {
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                JdbcConnectionProvider.getInstance().releaseConnection(connection);
            }
        }
    }
    
    /**
     * Finds all users in the database.
     * 
     * @return A list of all users
     * @throws SQLException If there's an error querying the database
     */
    public List<User> findAll() throws SQLException {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = JdbcConnectionProvider.getInstance().getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(FIND_ALL_USERS_SQL);
            
            List<User> users = new ArrayList<>();
            while (resultSet.next()) {
                users.add(mapResultSetToUser(resultSet));
            }
            return users;
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                JdbcConnectionProvider.getInstance().releaseConnection(connection);
            }
        }
    }
    
    /**
     * Maps a database result set to a User object.
     * 
     * @param resultSet The result set containing user data
     * @return A User object populated with data from the result set
     * @throws SQLException If there's an error reading from the result set
     */
    private User mapResultSetToUser(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong("id");
        String name = resultSet.getString("name");
        String email = resultSet.getString("email");
        String passwordHash = resultSet.getString("password_hash");
        String salt = resultSet.getString("salt");
        boolean active = resultSet.getBoolean("active");
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        user.setSalt(salt);
        user.setActive(active);
        user.setCreatedAt(createdAt != null ? createdAt.toString() : null);
        
        return user;
    }
}