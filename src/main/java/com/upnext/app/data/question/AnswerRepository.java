package com.upnext.app.data.question;

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

import com.upnext.app.core.Logger;
import com.upnext.app.data.JdbcConnectionProvider;
import com.upnext.app.domain.question.Answer;
import com.upnext.app.domain.question.AnswerVote;

/**
 * JDBC-backed repository for {@link Answer} and {@link AnswerVote} entities.
 * Handles persistence, voting, and verification logic for answers.
 */
public final class AnswerRepository {
    private static final Logger LOGGER = Logger.getInstance();
    private static final AnswerRepository INSTANCE = new AnswerRepository();
    
    // SQL statements for answer operations
    private static final String INSERT_ANSWER_SQL = 
        "INSERT INTO answers (question_id, user_id, content, created_at) VALUES (?, ?, ?, ?)";
    
    private static final String FIND_ANSWERS_BY_QUESTION_SQL = 
        "SELECT a.*, u.name AS user_name FROM answers a " +
        "LEFT JOIN users u ON a.user_id = u.id " +
        "WHERE a.question_id = ? ORDER BY a.is_accepted DESC, a.upvotes DESC, a.created_at ASC";
    
    private static final String FIND_ANSWER_BY_ID_SQL = 
        "SELECT a.*, u.name AS user_name FROM answers a " +
        "LEFT JOIN users u ON a.user_id = u.id " +
        "WHERE a.id = ?";
    
    // SQL statements for voting operations
    private static final String INSERT_VOTE_SQL = 
        "INSERT INTO answer_votes (answer_id, user_id, is_upvote, created_at) " +
        "VALUES (?, ?, ?, ?) " +
        "ON DUPLICATE KEY UPDATE is_upvote = VALUES(is_upvote), updated_at = CURRENT_TIMESTAMP";
    
    private static final String FIND_USER_VOTE_SQL = 
        "SELECT * FROM answer_votes WHERE answer_id = ? AND user_id = ?";
    
    private static final String DELETE_VOTE_SQL = 
        "DELETE FROM answer_votes WHERE answer_id = ? AND user_id = ?";
    
    private static final String UPDATE_ANSWER_VOTE_COUNTS_SQL = 
        "UPDATE answers SET upvotes = ?, downvotes = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
    
    private static final String GET_VOTE_COUNTS_SQL = 
        "SELECT " +
        "  SUM(CASE WHEN is_upvote = TRUE THEN 1 ELSE 0 END) AS upvotes, " +
        "  SUM(CASE WHEN is_upvote = FALSE THEN 1 ELSE 0 END) AS downvotes " +
        "FROM answer_votes WHERE answer_id = ?";
    
    private static final String MARK_AS_VERIFIED_SQL = 
        "UPDATE answers SET is_accepted = TRUE, updated_at = CURRENT_TIMESTAMP WHERE id = ? AND upvotes >= 10";
    
    private static final String UNMARK_AS_VERIFIED_SQL = 
        "UPDATE answers SET is_accepted = FALSE, updated_at = CURRENT_TIMESTAMP WHERE id = ? AND upvotes < 10";
    
    private static final String CREATE_ANSWER_VOTES_TABLE_SQL = 
        "CREATE TABLE IF NOT EXISTS answer_votes (" +
        "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
        "answer_id BIGINT NOT NULL, " +
        "user_id BIGINT NOT NULL, " +
        "is_upvote BOOLEAN NOT NULL, " +
        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
        "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
        "CONSTRAINT fk_answer_votes_answer FOREIGN KEY (answer_id) REFERENCES answers(id) ON DELETE CASCADE, " +
        "CONSTRAINT fk_answer_votes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, " +
        "UNIQUE KEY uk_answer_votes_user_answer (answer_id, user_id), " +
        "INDEX idx_answer_votes_answer (answer_id), " +
        "INDEX idx_answer_votes_user (user_id))";
    
    private AnswerRepository() {
        try {
            initializeTable();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to initialize answer_votes table", e);
        }
    }
    
    public static AnswerRepository getInstance() {
        return INSTANCE;
    }
    
    /**
     * Initializes the answer_votes table if it doesn't exist.
     */
    private void initializeTable() throws SQLException {
        JdbcConnectionProvider provider = JdbcConnectionProvider.getInstance();
        Connection connection = provider.getConnection();
        try (Statement statement = connection.createStatement()) {
            statement.execute(CREATE_ANSWER_VOTES_TABLE_SQL);
            LOGGER.info("Answer votes table initialized");
        } finally {
            provider.releaseConnection(connection);
        }
    }
    
    /**
     * Saves a new answer to the database.
     * 
     * @param answer The answer to save
     * @return The saved answer with ID populated
     * @throws SQLException If a database error occurs
     */
    public Answer save(Answer answer) throws SQLException {
        if (answer == null) {
            throw new IllegalArgumentException("Answer cannot be null");
        }
        if (answer.getQuestionId() == null) {
            throw new IllegalArgumentException("Answer must have a question ID");
        }
        if (answer.getUserId() == null) {
            throw new IllegalArgumentException("Answer must have a user ID");
        }
        if (answer.getContent() == null || answer.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Answer content cannot be empty");
        }
        
        JdbcConnectionProvider provider = JdbcConnectionProvider.getInstance();
        Connection connection = provider.getConnection();
        
        try (PreparedStatement statement = connection.prepareStatement(INSERT_ANSWER_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, answer.getQuestionId());
            statement.setLong(2, answer.getUserId());
            statement.setString(3, answer.getContent());
            statement.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating answer failed, no rows affected");
            }
            
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    answer.setId(generatedKeys.getLong(1));
                    answer.setCreatedAt(LocalDateTime.now());
                    answer.setUpdatedAt(LocalDateTime.now());
                } else {
                    throw new SQLException("Creating answer failed, no ID obtained");
                }
            }
            
            LOGGER.info("Answer saved successfully with ID: " + answer.getId());
            return answer;
            
        } finally {
            provider.releaseConnection(connection);
        }
    }
    
    /**
     * Finds answers for a specific question, ordered by acceptance and vote count.
     * 
     * @param questionId The question ID
     * @return List of answers for the question
     * @throws SQLException If a database error occurs
     */
    public List<Answer> findByQuestionId(Long questionId) throws SQLException {
        if (questionId == null) {
            throw new IllegalArgumentException("Question ID cannot be null");
        }
        
        List<Answer> answers = new ArrayList<>();
        JdbcConnectionProvider provider = JdbcConnectionProvider.getInstance();
        Connection connection = provider.getConnection();
        
        try (PreparedStatement statement = connection.prepareStatement(FIND_ANSWERS_BY_QUESTION_SQL)) {
            statement.setLong(1, questionId);
            
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    answers.add(mapAnswerFromResultSet(rs));
                }
            }
            
            LOGGER.info("Found " + answers.size() + " answers for question " + questionId);
            return answers;
            
        } finally {
            provider.releaseConnection(connection);
        }
    }
    
    /**
     * Finds an answer by its ID.
     * 
     * @param answerId The answer ID
     * @return Optional containing the answer if found
     * @throws SQLException If a database error occurs
     */
    public Optional<Answer> findById(Long answerId) throws SQLException {
        if (answerId == null) {
            throw new IllegalArgumentException("Answer ID cannot be null");
        }
        
        JdbcConnectionProvider provider = JdbcConnectionProvider.getInstance();
        Connection connection = provider.getConnection();
        
        try (PreparedStatement statement = connection.prepareStatement(FIND_ANSWER_BY_ID_SQL)) {
            statement.setLong(1, answerId);
            
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapAnswerFromResultSet(rs));
                }
                return Optional.empty();
            }
            
        } finally {
            provider.releaseConnection(connection);
        }
    }
    
    /**
     * Casts a vote on an answer. If the user has already voted, updates the vote.
     * If the same vote is cast again, removes the vote.
     * 
     * @param answerId The ID of the answer to vote on
     * @param userId The ID of the user casting the vote
     * @param isUpvote True for upvote, false for downvote
     * @return The updated vote counts as VoteResult
     * @throws SQLException If a database error occurs
     */
    public VoteResult voteAnswer(Long answerId, Long userId, boolean isUpvote) throws SQLException {
        if (answerId == null || userId == null) {
            throw new IllegalArgumentException("Answer ID and User ID cannot be null");
        }
        
        JdbcConnectionProvider provider = JdbcConnectionProvider.getInstance();
        Connection connection = provider.getConnection();
        
        try {
            connection.setAutoCommit(false);
            
            // Check if user already voted
            Optional<AnswerVote> existingVote = findUserVote(connection, answerId, userId);
            
            if (existingVote.isPresent()) {
                AnswerVote vote = existingVote.get();
                if (vote.isUpvote() == isUpvote) {
                    // Same vote - remove it
                    deleteVote(connection, answerId, userId);
                } else {
                    // Different vote - update it
                    updateVote(connection, answerId, userId, isUpvote);
                }
            } else {
                // New vote - create it
                createVote(connection, answerId, userId, isUpvote);
            }
            
            // Recalculate vote counts
            VoteResult voteResult = recalculateVoteCounts(connection, answerId);
            
            // Check for verified answer status (10+ upvotes)
            updateVerifiedStatus(connection, answerId, voteResult.getUpvotes());
            
            connection.commit();
            
            LOGGER.info("Vote processed for answer " + answerId + " by user " + userId + 
                       " (upvote: " + isUpvote + "). New counts: " + voteResult.getUpvotes() + 
                       " upvotes, " + voteResult.getDownvotes() + " downvotes");
            
            return voteResult;
            
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
            provider.releaseConnection(connection);
        }
    }
    
    /**
     * Finds a user's vote on a specific answer.
     */
    private Optional<AnswerVote> findUserVote(Connection connection, Long answerId, Long userId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(FIND_USER_VOTE_SQL)) {
            statement.setLong(1, answerId);
            statement.setLong(2, userId);
            
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapVoteFromResultSet(rs));
                }
                return Optional.empty();
            }
        }
    }
    
    /**
     * Creates a new vote.
     */
    private void createVote(Connection connection, Long answerId, Long userId, boolean isUpvote) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_VOTE_SQL)) {
            statement.setLong(1, answerId);
            statement.setLong(2, userId);
            statement.setBoolean(3, isUpvote);
            statement.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            statement.executeUpdate();
        }
    }
    
    /**
     * Updates an existing vote.
     */
    private void updateVote(Connection connection, Long answerId, Long userId, boolean isUpvote) throws SQLException {
        createVote(connection, answerId, userId, isUpvote); // ON DUPLICATE KEY UPDATE handles this
    }
    
    /**
     * Deletes a vote.
     */
    private void deleteVote(Connection connection, Long answerId, Long userId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_VOTE_SQL)) {
            statement.setLong(1, answerId);
            statement.setLong(2, userId);
            statement.executeUpdate();
        }
    }
    
    /**
     * Recalculates and updates vote counts for an answer.
     */
    private VoteResult recalculateVoteCounts(Connection connection, Long answerId) throws SQLException {
        int upvotes = 0;
        int downvotes = 0;
        
        // Get current vote counts
        try (PreparedStatement statement = connection.prepareStatement(GET_VOTE_COUNTS_SQL)) {
            statement.setLong(1, answerId);
            
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    upvotes = rs.getInt("upvotes");
                    downvotes = rs.getInt("downvotes");
                }
            }
        }
        
        // Update answer table
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_ANSWER_VOTE_COUNTS_SQL)) {
            statement.setInt(1, upvotes);
            statement.setInt(2, downvotes);
            statement.setLong(3, answerId);
            statement.executeUpdate();
        }
        
        return new VoteResult(upvotes, downvotes);
    }
    
    /**
     * Updates the verified status of an answer based on upvote count.
     */
    private void updateVerifiedStatus(Connection connection, Long answerId, int upvotes) throws SQLException {
        String sql = upvotes >= 10 ? MARK_AS_VERIFIED_SQL : UNMARK_AS_VERIFIED_SQL;
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, answerId);
            int updated = statement.executeUpdate();
            
            if (updated > 0) {
                String status = upvotes >= 10 ? "verified" : "unverified";
                LOGGER.info("Answer " + answerId + " marked as " + status + " (upvotes: " + upvotes + ")");
            }
        }
    }
    
    /**
     * Maps a ResultSet row to an Answer object.
     */
    private Answer mapAnswerFromResultSet(ResultSet rs) throws SQLException {
        Answer answer = new Answer();
        answer.setId(rs.getLong("id"));
        answer.setQuestionId(rs.getLong("question_id"));
        answer.setUserId(rs.getLong("user_id"));
        answer.setContent(rs.getString("content"));
        answer.setAccepted(rs.getBoolean("is_accepted"));
        answer.setUpvotes(rs.getInt("upvotes"));
        answer.setDownvotes(rs.getInt("downvotes"));
        
        // Try to get user name if available
        try {
            String userName = rs.getString("user_name");
            if (userName != null) {
                answer.setUserName(userName);
            }
        } catch (SQLException e) {
            // Column might not exist in some queries
        }
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            answer.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            answer.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return answer;
    }
    
    /**
     * Maps a ResultSet row to an AnswerVote object.
     */
    private AnswerVote mapVoteFromResultSet(ResultSet rs) throws SQLException {
        AnswerVote vote = new AnswerVote();
        vote.setId(rs.getLong("id"));
        vote.setAnswerId(rs.getLong("answer_id"));
        vote.setUserId(rs.getLong("user_id"));
        vote.setUpvote(rs.getBoolean("is_upvote"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            vote.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            vote.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return vote;
    }
    
    /**
     * Result of a voting operation containing updated vote counts.
     */
    public static class VoteResult {
        private final int upvotes;
        private final int downvotes;
        
        public VoteResult(int upvotes, int downvotes) {
            this.upvotes = upvotes;
            this.downvotes = downvotes;
        }
        
        public int getUpvotes() { return upvotes; }
        public int getDownvotes() { return downvotes; }
        public int getNetVotes() { return upvotes - downvotes; }
        public boolean isVerified() { return upvotes >= 10; }
        
        @Override
        public String toString() {
            return "VoteResult{upvotes=" + upvotes + ", downvotes=" + downvotes + ", verified=" + isVerified() + "}";
        }
    }
}