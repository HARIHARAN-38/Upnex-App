package com.upnext.app.data.question;

import com.upnext.app.core.Logger;
import com.upnext.app.data.JdbcConnectionProvider;
import com.upnext.app.domain.question.Subject;
import java.sql.*;
import java.util.*;

/**
 * Repository for Subject entity database operations.
 * Handles CRUD operations for question subjects in the database.
 */
public class SubjectRepository {
    private static final Logger logger = Logger.getInstance();
    private static SubjectRepository instance;

    // SQL queries
    private static final String INSERT_SUBJECT_SQL = 
            "INSERT INTO subjects (name, description) VALUES (?, ?)";
    
    private static final String FIND_BY_ID_SQL = 
            "SELECT * FROM subjects WHERE id = ?";
    
    private static final String FIND_BY_NAME_SQL = 
            "SELECT * FROM subjects WHERE name = ?";
    
    private static final String UPDATE_SUBJECT_SQL = 
            "UPDATE subjects SET name = ?, description = ? WHERE id = ?";
    
    private static final String DELETE_SUBJECT_SQL = 
            "DELETE FROM subjects WHERE id = ?";
    
    private static final String FIND_ALL_SUBJECTS_SQL = 
            "SELECT * FROM subjects ORDER BY name";

    /**
     * Private constructor to enforce singleton pattern.
     */
    private SubjectRepository() {
    }
    
    /**
     * Gets the singleton instance of the repository.
     * 
     * @return The repository instance
     */
    public static synchronized SubjectRepository getInstance() {
        if (instance == null) {
            instance = new SubjectRepository();
        }
        return instance;
    }
    
    /**
     * Saves a new subject to the database.
     * 
     * @param subject The subject to save
     * @return The saved subject with generated ID
     * @throws SQLException If there's an error saving the subject
     */
    public Subject save(Subject subject) throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet generatedKeys = null;
        
        try {
            connection = JdbcConnectionProvider.getInstance().getConnection();
            statement = connection.prepareStatement(INSERT_SUBJECT_SQL, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, subject.getName());
            statement.setString(2, subject.getDescription());
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating subject failed, no rows affected.");
            }
            
            generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                subject.setId(generatedKeys.getLong(1));
            } else {
                throw new SQLException("Creating subject failed, no ID obtained.");
            }
            
            return subject;
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
     * Finds a subject by its ID.
     * 
     * @param id The subject ID to search for
     * @return An Optional containing the subject if found, empty otherwise
     * @throws SQLException If there's an error querying the database
     */
    public Optional<Subject> findById(Long id) throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = JdbcConnectionProvider.getInstance().getConnection();
            statement = connection.prepareStatement(FIND_BY_ID_SQL);
            statement.setLong(1, id);
            
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(mapResultSetToSubject(resultSet));
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
     * Finds a subject by its name.
     * 
     * @param name The subject name to search for
     * @return An Optional containing the subject if found, empty otherwise
     * @throws SQLException If there's an error querying the database
     */
    public Optional<Subject> findByName(String name) throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = JdbcConnectionProvider.getInstance().getConnection();
            statement = connection.prepareStatement(FIND_BY_NAME_SQL);
            statement.setString(1, name);
            
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(mapResultSetToSubject(resultSet));
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
     * Updates an existing subject in the database.
     * 
     * @param subject The subject to update
     * @return true if the update was successful, false otherwise
     * @throws SQLException If there's an error updating the subject
     */
    public boolean update(Subject subject) throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;
        
        try {
            connection = JdbcConnectionProvider.getInstance().getConnection();
            statement = connection.prepareStatement(UPDATE_SUBJECT_SQL);
            statement.setString(1, subject.getName());
            statement.setString(2, subject.getDescription());
            statement.setLong(3, subject.getId());
            
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
     * Deletes a subject from the database.
     * 
     * @param id The ID of the subject to delete
     * @return true if the deletion was successful, false otherwise
     * @throws SQLException If there's an error deleting the subject
     */
    public boolean delete(Long id) throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;
        
        try {
            connection = JdbcConnectionProvider.getInstance().getConnection();
            statement = connection.prepareStatement(DELETE_SUBJECT_SQL);
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
     * Finds all subjects in the database.
     * 
     * @return A list of all subjects
     * @throws SQLException If there's an error querying the database
     */
    public List<Subject> findAll() throws SQLException {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = JdbcConnectionProvider.getInstance().getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(FIND_ALL_SUBJECTS_SQL);
            
            List<Subject> subjects = new ArrayList<>();
            while (resultSet.next()) {
                subjects.add(mapResultSetToSubject(resultSet));
            }
            return subjects;
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
     * Maps a database result set to a Subject object.
     * 
     * @param resultSet The result set containing subject data
     * @return A Subject object populated with data from the result set
     * @throws SQLException If there's an error reading from the result set
     */
    private Subject mapResultSetToSubject(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong("id");
        String name = resultSet.getString("name");
        String description = resultSet.getString("description");
        
        Subject subject = new Subject();
        subject.setId(id);
        subject.setName(name);
        subject.setDescription(description);
        
        return subject;
    }
}