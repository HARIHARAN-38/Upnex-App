package com.upnext.app.data.question;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.upnext.app.core.Logger;
import com.upnext.app.data.JdbcConnectionProvider;
import com.upnext.app.domain.question.Answer;
import com.upnext.app.domain.question.Question;
import com.upnext.app.domain.question.QuestionSearchCriteria;

/**
 * JDBC-backed repository for {@link Question} entities.
 * Handles persistence, search, and tag management for questions.
 */
public final class QuestionRepository {
    private static final Logger LOGGER = Logger.getInstance();
    private static final QuestionRepository INSTANCE = new QuestionRepository();

    private static final String CREATE_SUBJECTS_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS subjects (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(100) NOT NULL UNIQUE, " +
                    "description VARCHAR(500))";

    private static final String CREATE_QUESTIONS_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS questions (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
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
                    "FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE SET NULL)";

    private static final String CREATE_TAGS_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS tags (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(50) NOT NULL UNIQUE, " +
                    "usage_count INT NOT NULL DEFAULT 0)";

    private static final String CREATE_QUESTION_TAGS_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS question_tags (" +
                    "question_id BIGINT NOT NULL, " +
                    "tag_id BIGINT NOT NULL, " +
                    "PRIMARY KEY (question_id, tag_id), " +
                    "FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE)";

    private static final String INSERT_QUESTION_SQL =
            "INSERT INTO questions (user_id, subject_id, title, content) VALUES (?, ?, ?, ?)";

    private static final String UPDATE_QUESTION_SQL =
            "UPDATE questions SET title = ?, content = ?, subject_id = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";

    private static final String DELETE_QUESTION_SQL = "DELETE FROM questions WHERE id = ?";

    private static final String FIND_BY_ID_SQL =
            "SELECT q.*, s.name AS subject_name FROM questions q " +
                    "LEFT JOIN subjects s ON q.subject_id = s.id WHERE q.id = ?";

    private static final String FIND_PAGE_SQL =
            "SELECT q.*, s.name AS subject_name FROM questions q " +
                    "LEFT JOIN subjects s ON q.subject_id = s.id " +
                    "ORDER BY q.created_at DESC LIMIT ? OFFSET ?";

    private static final String FIND_BY_USER_SQL =
            "SELECT q.*, s.name AS subject_name FROM questions q " +
                    "LEFT JOIN subjects s ON q.subject_id = s.id " +
                    "WHERE q.user_id = ? ORDER BY q.created_at DESC LIMIT ? OFFSET ?";

    private static final String FIND_TAGS_SQL =
            "SELECT t.name FROM tags t " +
                    "INNER JOIN question_tags qt ON t.id = qt.tag_id " +
                    "WHERE qt.question_id = ? ORDER BY t.name";

    private static final String CLEAR_TAGS_SQL =
            "DELETE FROM question_tags WHERE question_id = ?";

    private static final String UPSERT_TAG_SQL =
            "INSERT INTO tags (name, usage_count) VALUES (?, 1) " +
                    "ON DUPLICATE KEY UPDATE usage_count = usage_count + 1";

    private static final String FIND_TAG_ID_SQL = "SELECT id FROM tags WHERE name = ?";

    private static final String LINK_TAG_SQL =
            "INSERT INTO question_tags (question_id, tag_id) VALUES (?, ?) " +
                    "ON DUPLICATE KEY UPDATE question_id = question_id";

    private static final String UPDATE_VOTES_SQL =
            "UPDATE questions SET upvotes = ?, downvotes = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";

    private static final String UPDATE_ANSWER_COUNT_SQL =
            "UPDATE questions SET answer_count = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";

    private static final String UPDATE_SOLVED_SQL =
            "UPDATE questions SET is_solved = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
            
    private static final String UPDATE_VIEW_COUNT_SQL =
            "UPDATE questions SET view_count = view_count + 1, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
            
    private static final String CREATE_ANSWERS_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS answers (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "question_id BIGINT NOT NULL, " +
                    "user_id BIGINT NOT NULL, " +
                    "content TEXT NOT NULL, " +
                    "upvotes INT NOT NULL DEFAULT 0, " +
                    "downvotes INT NOT NULL DEFAULT 0, " +
                    "is_accepted BOOLEAN NOT NULL DEFAULT FALSE, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE)";
                    
    private static final String INSERT_ANSWER_SQL =
            "INSERT INTO answers (question_id, user_id, content, created_at) VALUES (?, ?, ?, ?)";
            
    private static final String FIND_ANSWERS_SQL =
            "SELECT a.*, u.name AS user_name FROM answers a " +
                    "LEFT JOIN users u ON a.user_id = u.id " +
                    "WHERE a.question_id = ? ORDER BY a.is_accepted DESC, a.upvotes DESC, a.created_at ASC";
                    
    private static final String UPDATE_ANSWER_VOTES_SQL =
            "UPDATE answers SET upvotes = ?, downvotes = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";

    private QuestionRepository() {
        try {
            initializeTables();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to initialise question tables", ex);
        }
    }

    public static QuestionRepository getInstance() {
        return INSTANCE;
    }

    public Question save(Question question) throws SQLException {
        validateQuestionForSave(question);
        JdbcConnectionProvider provider = JdbcConnectionProvider.getInstance();
        Connection connection = provider.getConnection();
        PreparedStatement insertStatement = null;
        ResultSet keys = null;
        try {
            connection.setAutoCommit(false);
            insertStatement = connection.prepareStatement(INSERT_QUESTION_SQL, Statement.RETURN_GENERATED_KEYS);
            insertStatement.setLong(1, question.getUserId());
            if (question.getSubjectId() != null) {
                insertStatement.setLong(2, question.getSubjectId());
            } else {
                insertStatement.setNull(2, Types.BIGINT);
            }
            insertStatement.setString(3, question.getTitle());
            insertStatement.setString(4, question.getContent());
            if (insertStatement.executeUpdate() == 0) {
                throw new SQLException("Inserting question returned zero affected rows");
            }
            keys = insertStatement.getGeneratedKeys();
            if (keys.next()) {
                question.setId(keys.getLong(1));
            } else {
                throw new SQLException("Inserting question failed to return generated id");
            }
            LocalDateTime now = LocalDateTime.now();
            question.setCreatedAt(now);
            question.setUpdatedAt(now);
            replaceTags(connection, question.getId(), question.getTags());
            connection.commit();
            return question;
        } catch (SQLException ex) {
            safeRollback(connection);
            throw ex;
        } finally {
            closeQuietly(keys);
            closeQuietly(insertStatement);
            resetAndRelease(provider, connection);
        }
    }

    public Optional<Question> findById(Long id) throws SQLException {
        JdbcConnectionProvider provider = JdbcConnectionProvider.getInstance();
        Connection connection = provider.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                Question question = mapResult(rs);
                loadTags(connection, question);
                return Optional.of(question);
            }
        } finally {
            provider.releaseConnection(connection);
        }
    }

    public List<Question> findPage(int limit, int offset) throws SQLException {
        JdbcConnectionProvider provider = JdbcConnectionProvider.getInstance();
        Connection connection = provider.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(FIND_PAGE_SQL)) {
            statement.setInt(1, Math.max(1, limit));
            statement.setInt(2, Math.max(0, offset));
            try (ResultSet rs = statement.executeQuery()) {
                List<Question> questions = new ArrayList<>();
                while (rs.next()) {
                    questions.add(mapResult(rs));
                }
                for (Question question : questions) {
                    loadTags(connection, question);
                }
                return questions;
            }
        } finally {
            provider.releaseConnection(connection);
        }
    }

    public List<Question> findByUserId(Long userId, int limit, int offset) throws SQLException {
        JdbcConnectionProvider provider = JdbcConnectionProvider.getInstance();
        Connection connection = provider.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_USER_SQL)) {
            statement.setLong(1, userId);
            statement.setInt(2, Math.max(1, limit));
            statement.setInt(3, Math.max(0, offset));
            try (ResultSet rs = statement.executeQuery()) {
                List<Question> questions = new ArrayList<>();
                while (rs.next()) {
                    questions.add(mapResult(rs));
                }
                for (Question question : questions) {
                    loadTags(connection, question);
                }
                return questions;
            }
        } finally {
            provider.releaseConnection(connection);
        }
    }

    public boolean update(Question question) throws SQLException {
        Objects.requireNonNull(question, "question");
        if (question.getId() == null) {
            throw new IllegalArgumentException("Question id must be set for update");
        }
        JdbcConnectionProvider provider = JdbcConnectionProvider.getInstance();
        Connection connection = provider.getConnection();
        PreparedStatement updateStatement = null;
        try {
            connection.setAutoCommit(false);
            updateStatement = connection.prepareStatement(UPDATE_QUESTION_SQL);
            updateStatement.setString(1, question.getTitle());
            updateStatement.setString(2, question.getContent());
            if (question.getSubjectId() != null) {
                updateStatement.setLong(3, question.getSubjectId());
            } else {
                updateStatement.setNull(3, Types.BIGINT);
            }
            updateStatement.setLong(4, question.getId());
            int affected = updateStatement.executeUpdate();
            if (affected > 0) {
                question.setUpdatedAt(LocalDateTime.now());
                replaceTags(connection, question.getId(), question.getTags());
                connection.commit();
                return true;
            }
            connection.rollback();
            return false;
        } catch (SQLException ex) {
            safeRollback(connection);
            throw ex;
        } finally {
            closeQuietly(updateStatement);
            resetAndRelease(provider, connection);
        }
    }

    public boolean delete(Long id) throws SQLException {
        JdbcConnectionProvider provider = JdbcConnectionProvider.getInstance();
        Connection connection = provider.getConnection();
        PreparedStatement deleteStatement = null;
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement clearTags = connection.prepareStatement(CLEAR_TAGS_SQL)) {
                clearTags.setLong(1, id);
                clearTags.executeUpdate();
            }
            deleteStatement = connection.prepareStatement(DELETE_QUESTION_SQL);
            deleteStatement.setLong(1, id);
            boolean removed = deleteStatement.executeUpdate() > 0;
            connection.commit();
            return removed;
        } catch (SQLException ex) {
            safeRollback(connection);
            throw ex;
        } finally {
            closeQuietly(deleteStatement);
            resetAndRelease(provider, connection);
        }
    }

    public boolean updateVoteCounts(Long questionId, int upvotes, int downvotes) throws SQLException {
        return runSimpleUpdate(questionId, ps -> {
            ps.setInt(1, upvotes);
            ps.setInt(2, downvotes);
            return 3;
        }, UPDATE_VOTES_SQL);
    }

    public boolean updateAnswerCount(Long questionId, int answerCount) throws SQLException {
        return runSimpleUpdate(questionId, ps -> {
            ps.setInt(1, answerCount);
            return 2;
        }, UPDATE_ANSWER_COUNT_SQL);
    }

    public boolean updateSolvedStatus(Long questionId, boolean solved) throws SQLException {
        return runSimpleUpdate(questionId, ps -> {
            ps.setBoolean(1, solved);
            return 2;
        }, UPDATE_SOLVED_SQL);
    }

    public List<Question> search(QuestionSearchCriteria criteria) throws SQLException {
        if (criteria == null) {
            return Collections.emptyList();
        }
        SearchQueryBuilder builder = new SearchQueryBuilder(criteria);
        JdbcConnectionProvider provider = JdbcConnectionProvider.getInstance();
        Connection connection = provider.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(builder.build())) {
            builder.apply(statement);
            try (ResultSet rs = statement.executeQuery()) {
                List<Question> questions = new ArrayList<>();
                while (rs.next()) {
                    questions.add(mapResult(rs));
                }
                for (Question question : questions) {
                    loadTags(connection, question);
                }
                return questions;
            }
        } finally {
            provider.releaseConnection(connection);
        }
    }

    /**
     * Increments the view count for a question.
     * 
     * @param questionId The ID of the question
     * @return true if successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    public boolean incrementViewCount(Long questionId) throws SQLException {
        return runSimpleUpdate(questionId, ps -> 1, UPDATE_VIEW_COUNT_SQL);
    }
    
    /**
     * Marks a question as solved.
     * 
     * @param questionId The ID of the question to mark as solved
     * @return true if successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    public boolean markAsSolved(Long questionId) throws SQLException {
        return updateSolvedStatus(questionId, true);
    }
    
    /**
     * Finds answers for a specific question.
     * 
     * @param questionId The question ID
     * @return A list of answers
     * @throws SQLException If a database error occurs
     */
    public List<Answer> findAnswersForQuestion(Long questionId) throws SQLException {
        JdbcConnectionProvider provider = JdbcConnectionProvider.getInstance();
        Connection connection = provider.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(FIND_ANSWERS_SQL)) {
            statement.setLong(1, questionId);
            
            List<Answer> answers = new ArrayList<>();
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Answer answer = new Answer();
                    answer.setId(rs.getLong("id"));
                    answer.setQuestionId(rs.getLong("question_id"));
                    answer.setUserId(rs.getLong("user_id"));
                    answer.setContent(rs.getString("content"));
                    answer.setUpvotes(rs.getInt("upvotes"));
                    answer.setDownvotes(rs.getInt("downvotes"));
                    answer.setAccepted(rs.getBoolean("is_accepted"));
                    
                    // Get user name if available
                    try {
                        String userName = rs.getString("user_name");
                        if (userName != null) {
                            answer.setUserName(userName);
                        }
                    } catch (SQLException e) {
                        // Column might not exist
                    }
                    
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    if (createdAt != null) {
                        answer.setCreatedAt(createdAt.toLocalDateTime());
                    }
                    
                    Timestamp updatedAt = rs.getTimestamp("updated_at");
                    if (updatedAt != null) {
                        answer.setUpdatedAt(updatedAt.toLocalDateTime());
                    }
                    
                    answers.add(answer);
                }
            }
            
            return answers;
        } finally {
            provider.releaseConnection(connection);
        }
    }
    
    /**
     * Saves a new answer to a question.
     * 
     * @param answer The answer to save
     * @return The saved answer with ID populated
     * @throws SQLException If a database error occurs
     */
    public Answer saveAnswer(Answer answer) throws SQLException {
        Objects.requireNonNull(answer, "answer");
        if (answer.getQuestionId() == null) {
            throw new IllegalArgumentException("Answer question ID must not be null");
        }
        if (answer.getUserId() == null) {
            throw new IllegalArgumentException("Answer user ID must not be null");
        }
        if (answer.getContent() == null || answer.getContent().isBlank()) {
            throw new IllegalArgumentException("Answer content must not be blank");
        }
        
        JdbcConnectionProvider provider = JdbcConnectionProvider.getInstance();
        Connection connection = provider.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(INSERT_ANSWER_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, answer.getQuestionId());
            statement.setLong(2, answer.getUserId());
            statement.setString(3, answer.getContent());
            
            LocalDateTime now = LocalDateTime.now();
            statement.setTimestamp(4, Timestamp.valueOf(now));
            
            int rowsAffected = statement.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        long id = generatedKeys.getLong(1);
                        answer.setId(id);
                        
                        // Increment answer count for the question
                        updateAnswerCount(answer.getQuestionId(), getAnswerCount(connection, answer.getQuestionId()) + 1);
                        
                        return answer;
                    }
                }
            }
            
            throw new SQLException("Failed to save answer, no ID obtained.");
        } finally {
            provider.releaseConnection(connection);
        }
    }
    
    /**
     * Gets the current answer count for a question.
     * 
     * @param connection The database connection
     * @param questionId The question ID
     * @return The number of answers for the question
     * @throws SQLException If a database error occurs
     */
    private int getAnswerCount(Connection connection, Long questionId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM answers WHERE question_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
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
     * Updates the vote counts for an answer.
     * 
     * @param answerId The answer ID
     * @param upvotes The new upvote count
     * @param downvotes The new downvote count
     * @return true if successful, false otherwise
     * @throws SQLException If a database error occurs
     */
    public boolean updateAnswerVoteCounts(Long answerId, int upvotes, int downvotes) throws SQLException {
        if (answerId == null) {
            throw new IllegalArgumentException("Answer id must not be null");
        }
        
        JdbcConnectionProvider provider = JdbcConnectionProvider.getInstance();
        Connection connection = provider.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_ANSWER_VOTES_SQL)) {
            statement.setInt(1, upvotes);
            statement.setInt(2, downvotes);
            statement.setLong(3, answerId);
            
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } finally {
            provider.releaseConnection(connection);
        }
    }
    
    private void initializeTables() throws SQLException {
        JdbcConnectionProvider provider = JdbcConnectionProvider.getInstance();
        Connection connection = provider.getConnection();
        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.execute(CREATE_SUBJECTS_TABLE_SQL);
            statement.execute(CREATE_QUESTIONS_TABLE_SQL);
            statement.execute(CREATE_TAGS_TABLE_SQL);
            statement.execute(CREATE_QUESTION_TAGS_TABLE_SQL);
            statement.execute(CREATE_ANSWERS_TABLE_SQL);
        } finally {
            closeQuietly(statement);
            provider.releaseConnection(connection);
        }
    }

    private void replaceTags(Connection connection, Long questionId, List<String> tags) throws SQLException {
        List<String> safeTags = tags == null ? Collections.emptyList() : tags;
        try (PreparedStatement delete = connection.prepareStatement(CLEAR_TAGS_SQL)) {
            delete.setLong(1, questionId);
            delete.executeUpdate();
        }
        if (safeTags.isEmpty()) {
            return;
        }
        try (PreparedStatement upsert = connection.prepareStatement(UPSERT_TAG_SQL);
             PreparedStatement findId = connection.prepareStatement(FIND_TAG_ID_SQL);
             PreparedStatement link = connection.prepareStatement(LINK_TAG_SQL)) {
            for (String raw : safeTags) {
                String tag = raw == null ? null : raw.trim();
                if (tag == null || tag.isEmpty()) {
                    continue;
                }
                upsert.setString(1, tag);
                upsert.executeUpdate();
                findId.setString(1, tag);
                try (ResultSet rs = findId.executeQuery()) {
                    if (rs.next()) {
                        link.setLong(1, questionId);
                        link.setLong(2, rs.getLong(1));
                        link.executeUpdate();
                    }
                }
            }
        }
    }

    private void loadTags(Connection connection, Question question) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(FIND_TAGS_SQL)) {
            statement.setLong(1, question.getId());
            try (ResultSet rs = statement.executeQuery()) {
                List<String> tags = new ArrayList<>();
                while (rs.next()) {
                    tags.add(rs.getString("name"));
                }
                question.setTags(tags);
            }
        }
    }

    private Question mapResult(ResultSet rs) throws SQLException {
        Question question = new Question();
        question.setId(rs.getLong("id"));
        question.setUserId(rs.getLong("user_id"));
        question.setTitle(rs.getString("title"));
        question.setContent(rs.getString("content"));
        long subjectId = rs.getLong("subject_id");
        if (!rs.wasNull()) {
            question.setSubjectId(subjectId);
        }
        question.setSubjectName(rs.getString("subject_name"));
        question.setUpvotes(rs.getInt("upvotes"));
        question.setDownvotes(rs.getInt("downvotes"));
        question.setAnswerCount(rs.getInt("answer_count"));
        question.setSolved(rs.getBoolean("is_solved"));
        
        // Get view count
        try {
            question.setViewCount(rs.getInt("view_count"));
        } catch (SQLException e) {
            // View count might not exist in older DB schema
            question.setViewCount(0);
        }
        
        Timestamp created = rs.getTimestamp("created_at");
        Timestamp updated = rs.getTimestamp("updated_at");
        if (created != null) {
            question.setCreatedAt(created.toLocalDateTime());
        }
        if (updated != null) {
            question.setUpdatedAt(updated.toLocalDateTime());
        }
        
        // Try to get user name (might be included in a join)
        try {
            String userName = rs.getString("user_name");
            if (userName != null) {
                question.setUserName(userName);
            }
        } catch (SQLException e) {
            // User name column might not exist in the result set
        }
        
        return question;
    }

    private void validateQuestionForSave(Question question) {
        Objects.requireNonNull(question, "question");
        if (question.getUserId() == null) {
            throw new IllegalArgumentException("Question user id must not be null");
        }
        if (question.getTitle() == null || question.getTitle().isBlank()) {
            throw new IllegalArgumentException("Question title must not be blank");
        }
        if (question.getContent() == null || question.getContent().isBlank()) {
            throw new IllegalArgumentException("Question content must not be blank");
        }
    }

    private boolean runSimpleUpdate(Long questionId, ParameterBinder binder, String sql) throws SQLException {
        if (questionId == null) {
            throw new IllegalArgumentException("Question id must not be null");
        }
        JdbcConnectionProvider provider = JdbcConnectionProvider.getInstance();
        Connection connection = provider.getConnection();
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            int nextIndex = binder.bind(statement);
            statement.setLong(nextIndex, questionId);
            return statement.executeUpdate() > 0;
        } finally {
            closeQuietly(statement);
            provider.releaseConnection(connection);
        }
    }

    private void safeRollback(Connection connection) {
        if (connection == null) {
            return;
        }
        try {
            connection.rollback();
        } catch (SQLException ex) {
            LOGGER.logException("Failed to rollback question transaction", ex);
        }
    }

    private void resetAndRelease(JdbcConnectionProvider provider, Connection connection) {
        if (connection == null) {
            return;
        }
        try {
            connection.setAutoCommit(true);
        } catch (SQLException ex) {
            LOGGER.logException("Failed to reset auto-commit", ex);
        }
        provider.releaseConnection(connection);
    }

    private void closeQuietly(AutoCloseable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception ignore) {
            // Best effort close; nothing actionable for callers.
        }
    }

    @FunctionalInterface
    private interface ParameterBinder {
        int bind(PreparedStatement ps) throws SQLException;
    }

    private static final class SearchQueryBuilder {
        private final QuestionSearchCriteria criteria;
        private final StringBuilder sql = new StringBuilder();
        private final List<Object> parameters = new ArrayList<>();

        private SearchQueryBuilder(QuestionSearchCriteria criteria) {
            this.criteria = criteria;
        }

        private String build() {
            sql.append("SELECT DISTINCT q.*, s.name AS subject_name FROM questions q ");
            sql.append("LEFT JOIN subjects s ON q.subject_id = s.id ");
            boolean filterByTags = criteria.getTags() != null && !criteria.getTags().isEmpty();
            if (filterByTags) {
                sql.append("INNER JOIN question_tags qt ON q.id = qt.question_id ");
                sql.append("INNER JOIN tags t ON qt.tag_id = t.id ");
            }
            List<String> clauses = new ArrayList<>();
            if (hasText(criteria.getSearchText())) {
                clauses.add("(q.title LIKE ? OR q.content LIKE ?)");
                String likeValue = '%' + criteria.getSearchText() + '%';
                parameters.add(likeValue);
                parameters.add(likeValue);
            }
            if (criteria.getSubjectId() != null) {
                clauses.add("q.subject_id = ?");
                parameters.add(criteria.getSubjectId());
            }
            if (criteria.getUserId() != null) {
                clauses.add("q.user_id = ?");
                parameters.add(criteria.getUserId());
            }
            if (criteria.isOnlyUnanswered()) {
                clauses.add("q.answer_count = 0");
            }
            if (criteria.isOnlySolved()) {
                clauses.add("q.is_solved = TRUE");
            }
            if (filterByTags) {
                StringBuilder tagPlaceholders = new StringBuilder("t.name IN (");
                for (int i = 0; i < criteria.getTags().size(); i++) {
                    if (i > 0) {
                        tagPlaceholders.append(", ");
                    }
                    tagPlaceholders.append('?');
                    parameters.add(criteria.getTags().get(i));
                }
                tagPlaceholders.append(')');
                clauses.add(tagPlaceholders.toString());
            }
            if (!clauses.isEmpty()) {
                sql.append("WHERE ");
                for (int i = 0; i < clauses.size(); i++) {
                    if (i > 0) {
                        sql.append(" AND ");
                    }
                    sql.append(clauses.get(i));
                }
                sql.append(' ');
            }
            if (filterByTags) {
                sql.append("GROUP BY q.id HAVING COUNT(DISTINCT t.id) = ? ");
                parameters.add(criteria.getTags().size());
            }
            sql.append("ORDER BY ").append(criteria.getSortOption().getSqlOrderBy()).append(' ');
            sql.append("LIMIT ? OFFSET ?");
            parameters.add(criteria.getLimit());
            parameters.add(criteria.getOffset());
            return sql.toString();
        }

        private void apply(PreparedStatement statement) throws SQLException {
            int index = 1;
            for (Object parameter : parameters) {
                if (parameter instanceof String) {
                    statement.setString(index++, (String) parameter);
                } else if (parameter instanceof Long) {
                    statement.setLong(index++, (Long) parameter);
                } else if (parameter instanceof Integer) {
                    statement.setInt(index++, (Integer) parameter);
                } else if (parameter instanceof Boolean) {
                    statement.setBoolean(index++, (Boolean) parameter);
                } else {
                    throw new SQLException("Unsupported parameter type: " + parameter.getClass().getName());
                }
            }
        }

        private boolean hasText(String value) {
            return value != null && !value.isBlank();
        }
    }
}
