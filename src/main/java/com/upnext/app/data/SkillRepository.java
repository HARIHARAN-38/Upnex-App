package com.upnext.app.data;

import com.upnext.app.domain.Skill;
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
 * Repository for Skill entity database operations.
 * Handles CRUD operations for skills in the database.
 */
public class SkillRepository {
    // Singleton instance
    private static SkillRepository instance;
    
    // SQL queries
    private static final String CREATE_TABLE_SQL = 
            "CREATE TABLE IF NOT EXISTS skills (" +
            "skill_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
            "user_id BIGINT NOT NULL, " +
            "skill_name VARCHAR(100) NOT NULL, " +
            "description VARCHAR(255), " +
            "proficiency_level INT NOT NULL DEFAULT 1, " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
            "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE)";
    
    private static final String INSERT_SKILL_SQL = 
            "INSERT INTO skills (user_id, skill_name, description, proficiency_level) " +
            "VALUES (?, ?, ?, ?)";
    
    private static final String FIND_BY_ID_SQL = 
            "SELECT * FROM skills WHERE skill_id = ?";
    
    private static final String FIND_BY_USER_ID_SQL = 
            "SELECT * FROM skills WHERE user_id = ?";
    
    private static final String UPDATE_SKILL_SQL = 
            "UPDATE skills SET skill_name = ?, description = ?, proficiency_level = ? " +
            "WHERE skill_id = ?";
    
    private static final String DELETE_SKILL_SQL = 
            "DELETE FROM skills WHERE skill_id = ?";
    
    private static final String DELETE_USER_SKILLS_SQL = 
            "DELETE FROM skills WHERE user_id = ?";
    
    /**
     * Private constructor to enforce singleton pattern.
     */
    private SkillRepository() {
        // Ensure table exists
        try {
            initializeTable();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize skills table", e);
        }
    }
    
    /**
     * Gets the singleton instance of the repository.
     * 
     * @return The repository instance
     */
    public static synchronized SkillRepository getInstance() {
        if (instance == null) {
            instance = new SkillRepository();
        }
        return instance;
    }
    
    /**
     * Ensures that the skills table exists in the database.
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
     * Saves a new skill to the database.
     * 
     * @param skill The skill to save
     * @return The saved skill with generated ID
     * @throws SQLException If there's an error saving the skill
     */
    public Skill save(Skill skill) throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet generatedKeys = null;
        
        try {
            connection = JdbcConnectionProvider.getInstance().getConnection();
            statement = connection.prepareStatement(INSERT_SKILL_SQL, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, skill.getUserId());
            statement.setString(2, skill.getSkillName());
            statement.setString(3, skill.getDescription());
            statement.setInt(4, skill.getProficiencyLevel());
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating skill failed, no rows affected.");
            }
            
            generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                skill.setSkillId(generatedKeys.getLong(1));
                skill.setCreatedAt(LocalDateTime.now().toString());
                skill.setUpdatedAt(LocalDateTime.now().toString());
            } else {
                throw new SQLException("Creating skill failed, no ID obtained.");
            }
            
            return skill;
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
     * Finds a skill by its ID.
     * 
     * @param skillId The skill ID to search for
     * @return An Optional containing the skill if found, empty otherwise
     * @throws SQLException If there's an error querying the database
     */
    public Optional<Skill> findById(Long skillId) throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = JdbcConnectionProvider.getInstance().getConnection();
            statement = connection.prepareStatement(FIND_BY_ID_SQL);
            statement.setLong(1, skillId);
            
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(mapResultSetToSkill(resultSet));
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
     * Finds all skills associated with a user.
     * 
     * @param userId The ID of the user to find skills for
     * @return A list of skills for the specified user
     * @throws SQLException If there's an error querying the database
     */
    public List<Skill> findByUserId(Long userId) throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = JdbcConnectionProvider.getInstance().getConnection();
            statement = connection.prepareStatement(FIND_BY_USER_ID_SQL);
            statement.setLong(1, userId);
            
            resultSet = statement.executeQuery();
            List<Skill> skills = new ArrayList<>();
            while (resultSet.next()) {
                skills.add(mapResultSetToSkill(resultSet));
            }
            return skills;
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
     * Updates an existing skill in the database.
     * 
     * @param skill The skill to update
     * @return true if the update was successful, false otherwise
     * @throws SQLException If there's an error updating the skill
     */
    public boolean update(Skill skill) throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;
        
        try {
            connection = JdbcConnectionProvider.getInstance().getConnection();
            statement = connection.prepareStatement(UPDATE_SKILL_SQL);
            statement.setString(1, skill.getSkillName());
            statement.setString(2, skill.getDescription());
            statement.setInt(3, skill.getProficiencyLevel());
            statement.setLong(4, skill.getSkillId());
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows > 0) {
                skill.setUpdatedAt(LocalDateTime.now().toString());
            }
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
     * Deletes a skill from the database.
     * 
     * @param skillId The ID of the skill to delete
     * @return true if the deletion was successful, false otherwise
     * @throws SQLException If there's an error deleting the skill
     */
    public boolean deleteById(Long skillId) throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;
        
        try {
            connection = JdbcConnectionProvider.getInstance().getConnection();
            statement = connection.prepareStatement(DELETE_SKILL_SQL);
            statement.setLong(1, skillId);
            
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
     * Deletes all skills for a specific user.
     * 
     * @param userId The ID of the user whose skills should be deleted
     * @return The number of skills deleted
     * @throws SQLException If there's an error deleting the skills
     */
    public int deleteByUserId(Long userId) throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;
        
        try {
            connection = JdbcConnectionProvider.getInstance().getConnection();
            statement = connection.prepareStatement(DELETE_USER_SKILLS_SQL);
            statement.setLong(1, userId);
            
            return statement.executeUpdate();
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
     * Maps a database result set to a Skill object.
     * 
     * @param resultSet The result set containing skill data
     * @return A Skill object populated with data from the result set
     * @throws SQLException If there's an error reading from the result set
     */
    private Skill mapResultSetToSkill(ResultSet resultSet) throws SQLException {
        Long skillId = resultSet.getLong("skill_id");
        Long userId = resultSet.getLong("user_id");
        String skillName = resultSet.getString("skill_name");
        String description = resultSet.getString("description");
        int proficiencyLevel = resultSet.getInt("proficiency_level");
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        Timestamp updatedAt = resultSet.getTimestamp("updated_at");
        
        Skill skill = new Skill();
        skill.setSkillId(skillId);
        skill.setUserId(userId);
        skill.setSkillName(skillName);
        skill.setDescription(description);
        skill.setProficiencyLevel(proficiencyLevel);
        skill.setCreatedAt(createdAt != null ? createdAt.toString() : null);
        skill.setUpdatedAt(updatedAt != null ? updatedAt.toString() : null);
        
        return skill;
    }
}