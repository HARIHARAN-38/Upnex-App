package com.upnext.app.data.question;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import com.upnext.app.core.Logger;
import com.upnext.app.data.JdbcConnectionProvider;
import com.upnext.app.domain.question.QuestionVote;
import com.upnext.app.domain.question.QuestionVote.VoteType;

/**
 * Repository for managing question votes in the database.
 * Implements Reddit-like voting where each user can vote only once per question.
 */
public class QuestionVoteRepository {
    private static QuestionVoteRepository instance;
    private final Logger logger = Logger.getInstance();
    
    private QuestionVoteRepository() {}
    
    public static synchronized QuestionVoteRepository getInstance() {
        if (instance == null) {
            instance = new QuestionVoteRepository();
        }
        return instance;
    }
    
    /**
     * Gets the current vote by a user for a specific question.
     * 
     * @param userId The ID of the user
     * @param questionId The ID of the question
     * @return Optional containing the vote if it exists
     * @throws SQLException If database error occurs
     */
    public Optional<QuestionVote> findByUserAndQuestion(Long userId, Long questionId) throws SQLException {
        String sql = "SELECT id, user_id, question_id, vote_type, created_at, updated_at " +
                    "FROM question_votes WHERE user_id = ? AND question_id = ?";
        
        try (Connection connection = JdbcConnectionProvider.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setLong(1, userId);
            statement.setLong(2, questionId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapResultSetToVote(resultSet));
                }
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Casts or updates a vote for a question by a user.
     * If the user hasn't voted, creates a new vote.
     * If the user has voted the same way, removes the vote.
     * If the user has voted differently, updates the vote.
     * 
     * @param userId The ID of the user
     * @param questionId The ID of the question
     * @param voteType The type of vote (upvote/downvote)
     * @return The vote operation result
     * @throws SQLException If database error occurs
     */
    public VoteResult castVote(Long userId, Long questionId, VoteType voteType) throws SQLException {
        Optional<QuestionVote> existingVote = findByUserAndQuestion(userId, questionId);
        
        if (existingVote.isPresent()) {
            QuestionVote vote = existingVote.get();
            if (vote.getVoteType() == voteType) {
                // Same vote type - remove the vote (toggle off)
                removeVote(userId, questionId);
                return VoteResult.REMOVED;
            } else {
                // Different vote type - update the vote
                updateVote(userId, questionId, voteType);
                return VoteResult.UPDATED;
            }
        } else {
            // No existing vote - create new vote
            createVote(userId, questionId, voteType);
            return VoteResult.CREATED;
        }
    }
    
    /**
     * Creates a new vote.
     */
    private void createVote(Long userId, Long questionId, VoteType voteType) throws SQLException {
        String sql = "INSERT INTO question_votes (user_id, question_id, vote_type) VALUES (?, ?, ?)";
        
        try (Connection connection = JdbcConnectionProvider.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setLong(1, userId);
            statement.setLong(2, questionId);
            statement.setString(3, voteType.getValue());
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Failed to create vote");
            }
            
            logger.info("Created new vote: user=" + userId + ", question=" + questionId + ", vote=" + voteType);
        }
    }
    
    /**
     * Updates an existing vote to a new type.
     */
    private void updateVote(Long userId, Long questionId, VoteType voteType) throws SQLException {
        String sql = "UPDATE question_votes SET vote_type = ?, updated_at = CURRENT_TIMESTAMP " +
                    "WHERE user_id = ? AND question_id = ?";
        
        try (Connection connection = JdbcConnectionProvider.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, voteType.getValue());
            statement.setLong(2, userId);
            statement.setLong(3, questionId);
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Failed to update vote");
            }
            
            logger.info("Updated vote: user=" + userId + ", question=" + questionId + ", vote=" + voteType);
        }
    }
    
    /**
     * Removes a vote.
     */
    private void removeVote(Long userId, Long questionId) throws SQLException {
        String sql = "DELETE FROM question_votes WHERE user_id = ? AND question_id = ?";
        
        try (Connection connection = JdbcConnectionProvider.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setLong(1, userId);
            statement.setLong(2, questionId);
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Failed to remove vote");
            }
            
            logger.info("Removed vote: user=" + userId + ", question=" + questionId);
        }
    }
    
    /**
     * Counts votes for a question.
     * 
     * @param questionId The ID of the question
     * @return Array containing [upvotes, downvotes]
     * @throws SQLException If database error occurs
     */
    public int[] countVotes(Long questionId) throws SQLException {
        String sql = "SELECT " +
                    "SUM(CASE WHEN vote_type = 'upvote' THEN 1 ELSE 0 END) as upvotes, " +
                    "SUM(CASE WHEN vote_type = 'downvote' THEN 1 ELSE 0 END) as downvotes " +
                    "FROM question_votes WHERE question_id = ?";
        
        try (Connection connection = JdbcConnectionProvider.getInstance().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setLong(1, questionId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new int[] {
                        resultSet.getInt("upvotes"),
                        resultSet.getInt("downvotes")
                    };
                }
            }
        }
        
        return new int[] {0, 0};
    }
    
    /**
     * Maps a ResultSet row to a QuestionVote object.
     */
    private QuestionVote mapResultSetToVote(ResultSet resultSet) throws SQLException {
        QuestionVote vote = new QuestionVote();
        vote.setId(resultSet.getLong("id"));
        vote.setUserId(resultSet.getLong("user_id"));
        vote.setQuestionId(resultSet.getLong("question_id"));
        vote.setVoteType(VoteType.fromString(resultSet.getString("vote_type")));
        vote.setCreatedAt(resultSet.getString("created_at"));
        vote.setUpdatedAt(resultSet.getString("updated_at"));
        return vote;
    }
    
    /**
     * Represents the result of a vote operation.
     */
    public enum VoteResult {
        CREATED,  // New vote was created
        UPDATED,  // Existing vote was updated to different type
        REMOVED   // Existing vote was removed (toggled off)
    }
}