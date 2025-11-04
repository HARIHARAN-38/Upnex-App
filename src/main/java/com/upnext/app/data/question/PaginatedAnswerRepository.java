package com.upnext.app.data.question;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.upnext.app.core.Logger;
import com.upnext.app.data.JdbcConnectionProvider;
import com.upnext.app.domain.question.Answer;

/**
 * Optimized repository for paginated answer queries.
 * Provides efficient pagination and loading for large answer lists.
 */
public final class PaginatedAnswerRepository {
    private static final Logger LOGGER = Logger.getInstance();
    private static final PaginatedAnswerRepository INSTANCE = new PaginatedAnswerRepository();
    
    // Optimized pagination query with proper indexing
    private static final String FIND_ANSWERS_PAGINATED_SQL = 
        "SELECT a.*, u.name AS user_name, u.email AS user_email " +
        "FROM answers a " +
        "LEFT JOIN users u ON a.user_id = u.id " +
        "WHERE a.question_id = ? " +
        "ORDER BY a.is_accepted DESC, a.upvotes DESC, a.created_at ASC " +
        "LIMIT ? OFFSET ?";
    
    // Count query for pagination metadata
    private static final String COUNT_ANSWERS_SQL = 
        "SELECT COUNT(*) FROM answers WHERE question_id = ?";
    
    // Optimized query for loading answer with vote counts
    private static final String FIND_ANSWER_WITH_VOTES_SQL = 
        "SELECT a.*, u.name AS user_name, u.email AS user_email, " +
        "COALESCE(v.upvotes, 0) AS upvote_count, " +
        "COALESCE(v.downvotes, 0) AS downvote_count " +
        "FROM answers a " +
        "LEFT JOIN users u ON a.user_id = u.id " +
        "LEFT JOIN (" +
        "  SELECT answer_id, " +
        "    SUM(CASE WHEN is_upvote = TRUE THEN 1 ELSE 0 END) AS upvotes, " +
        "    SUM(CASE WHEN is_upvote = FALSE THEN 1 ELSE 0 END) AS downvotes " +
        "  FROM answer_votes GROUP BY answer_id" +
        ") v ON a.id = v.answer_id " +
        "WHERE a.id = ?";
    
    private PaginatedAnswerRepository() {
        // Private constructor for singleton
    }
    
    public static PaginatedAnswerRepository getInstance() {
        return INSTANCE;
    }
    
    /**
     * Represents a paginated result set with metadata.
     */
    public static class PaginatedResult<T> {
        private final List<T> items;
        private final int totalCount;
        private final int pageSize;
        private final int currentPage;
        private final boolean hasNext;
        private final boolean hasPrevious;
        
        public PaginatedResult(List<T> items, int totalCount, int pageSize, int currentPage) {
            this.items = Objects.requireNonNull(items);
            this.totalCount = totalCount;
            this.pageSize = pageSize;
            this.currentPage = currentPage;
            this.hasNext = (currentPage + 1) * pageSize < totalCount;
            this.hasPrevious = currentPage > 0;
        }
        
        public List<T> getItems() { return items; }
        public int getTotalCount() { return totalCount; }
        public int getPageSize() { return pageSize; }
        public int getCurrentPage() { return currentPage; }
        public boolean hasNext() { return hasNext; }
        public boolean hasPrevious() { return hasPrevious; }
        public int getTotalPages() { return (int) Math.ceil((double) totalCount / pageSize); }
    }
    
    /**
     * Finds answers for a question with pagination support.
     * 
     * @param questionId The question ID
     * @param page The page number (0-based)
     * @param pageSize The number of answers per page
     * @return Paginated result with answers and metadata
     * @throws SQLException If a database error occurs
     */
    public PaginatedResult<Answer> findAnswersPaginated(Long questionId, int page, int pageSize) throws SQLException {
        Objects.requireNonNull(questionId, "Question ID must not be null");
        
        if (page < 0) {
            throw new IllegalArgumentException("Page must be non-negative");
        }
        if (pageSize <= 0 || pageSize > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }
        
        JdbcConnectionProvider provider = JdbcConnectionProvider.getInstance();
        Connection connection = provider.getConnection();
        
        try {
            // First get the total count
            int totalCount = getTotalAnswerCount(connection, questionId);
            
            // Calculate offset
            int offset = page * pageSize;
            
            // If offset is beyond available data, return empty result
            if (offset >= totalCount && totalCount > 0) {
                return new PaginatedResult<>(new ArrayList<>(), totalCount, pageSize, page);
            }
            
            // Get the paginated answers
            List<Answer> answers = getAnswersPage(connection, questionId, pageSize, offset);
            
            return new PaginatedResult<>(answers, totalCount, pageSize, page);
            
        } finally {
            provider.releaseConnection(connection);
        }
    }
    
    /**
     * Gets the total count of answers for a question.
     */
    private int getTotalAnswerCount(Connection connection, Long questionId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(COUNT_ANSWERS_SQL)) {
            statement.setLong(1, questionId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        }
    }
    
    /**
     * Gets a page of answers for a question.
     */
    private List<Answer> getAnswersPage(Connection connection, Long questionId, int limit, int offset) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(FIND_ANSWERS_PAGINATED_SQL)) {
            statement.setLong(1, questionId);
            statement.setInt(2, limit);
            statement.setInt(3, offset);
            
            List<Answer> answers = new ArrayList<>();
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Answer answer = mapAnswerResult(rs);
                    answers.add(answer);
                }
            }
            
            LOGGER.debug("Loaded " + answers.size() + " answers for question " + questionId + " (page offset: " + offset + ")");
            
            return answers;
        }
    }
    
    /**
     * Finds a single answer with optimized vote count loading.
     * 
     * @param answerId The answer ID
     * @return The answer with vote counts, or null if not found
     * @throws SQLException If a database error occurs
     */
    public Answer findAnswerWithVotes(Long answerId) throws SQLException {
        Objects.requireNonNull(answerId, "Answer ID must not be null");
        
        JdbcConnectionProvider provider = JdbcConnectionProvider.getInstance();
        Connection connection = provider.getConnection();
        
        try (PreparedStatement statement = connection.prepareStatement(FIND_ANSWER_WITH_VOTES_SQL)) {
            statement.setLong(1, answerId);
            
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    Answer answer = mapAnswerResult(rs);
                    // Set vote counts from the aggregated query
                    answer.setUpvotes(rs.getInt("upvote_count"));
                    answer.setDownvotes(rs.getInt("downvote_count"));
                    return answer;
                }
                return null;
            }
        } finally {
            provider.releaseConnection(connection);
        }
    }
    
    /**
     * Maps a ResultSet row to an Answer object.
     */
    private Answer mapAnswerResult(ResultSet rs) throws SQLException {
        Answer answer = new Answer();
        
        answer.setId(rs.getLong("id"));
        answer.setQuestionId(rs.getLong("question_id"));
        answer.setUserId(rs.getLong("user_id"));
        answer.setContent(rs.getString("content"));
        answer.setUpvotes(rs.getInt("upvotes"));
        answer.setDownvotes(rs.getInt("downvotes"));
        answer.setAccepted(rs.getBoolean("is_accepted"));
        
        // Set user information
        String userName = rs.getString("user_name");
        if (userName != null) {
            answer.setUserName(userName);
        }
        
        // Set timestamps
        if (rs.getTimestamp("created_at") != null) {
            answer.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            answer.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        
        return answer;
    }
}