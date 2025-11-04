package com.upnext.app.data.question;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.upnext.app.data.JdbcConnectionProvider;
import com.upnext.app.data.SchemaInitializer;
import com.upnext.app.data.UserRepository;
import com.upnext.app.domain.User;
import com.upnext.app.domain.question.Question;
import com.upnext.app.domain.question.QuestionSearchCriteria;
import com.upnext.app.domain.question.Subject;

/**
 * Test class for QuestionRepository.
 * Tests CRUD operations and search functionality for questions.
 */
public class QuestionRepositoryTest {
    private QuestionRepository questionRepository;
    private SubjectRepository subjectRepository;
    private UserRepository userRepository;
    private Long userOneId;
    private Long userTwoId;
    private Long subjectId;
    
    private static final String CLEANUP_SQL = 
            "DELETE FROM answer_votes; " +
            "DELETE FROM question_tags; " +
            "DELETE FROM tags; " +
            "DELETE FROM answers; " +
            "DELETE FROM questions; " +
            "DELETE FROM users WHERE email LIKE 'test-user-%@example.com'; " +
            "DELETE FROM subjects; " +
            "ALTER TABLE questions AUTO_INCREMENT = 1; " +
            "ALTER TABLE answers AUTO_INCREMENT = 1; " +
            "ALTER TABLE subjects AUTO_INCREMENT = 1; " +
            "ALTER TABLE users AUTO_INCREMENT = 1; " +
            "ALTER TABLE tags AUTO_INCREMENT = 1;";
    
    @BeforeEach
    public void setUp() throws SQLException {
        // Initialize schema if needed
        SchemaInitializer.initialize();
        
        // Clean up existing data (execute each statement separately because
        // drivers typically don't allow multiple statements in a single
        // PreparedStatement unless special flags are enabled)
        JdbcConnectionProvider provider = JdbcConnectionProvider.getInstance();
        Connection connection = provider.getConnection();
        try (Statement statement = connection.createStatement()) {
            for (String sql : CLEANUP_SQL.split(";")) {
                sql = sql.trim();
                if (sql.isEmpty()) continue;
                statement.executeUpdate(sql);
            }
        } finally {
            provider.releaseConnection(connection);
        }
        
        // Initialize repositories
        questionRepository = QuestionRepository.getInstance();
        subjectRepository = SubjectRepository.getInstance();
        userRepository = UserRepository.getInstance();
        
        // Create test users
        userOneId = createTestUser("test-user-1@example.com", "Test User One");
        userTwoId = createTestUser("test-user-2@example.com", "Test User Two");
        
        // Create test subject and capture ID
        Subject subject = new Subject("Test Subject", "Test Subject Description");
        subjectId = subjectRepository.save(subject).getId();
    }
    
    @Test
    public void testSaveQuestion() throws SQLException {
        // Create a test question
    Question question = new Question(userOneId, "Test Question", "Test Content", subjectId);
        question.addTag("test-tag");
        
        // Save the question
        Question savedQuestion = questionRepository.save(question);
        
        // Verify the question was saved
        assertNotNull(savedQuestion.getId());
        
        // Retrieve the saved question
        Optional<Question> retrievedQuestion = questionRepository.findById(savedQuestion.getId());
        
        // Verify the retrieved question matches the saved one
        assertTrue(retrievedQuestion.isPresent());
        assertEquals("Test Question", retrievedQuestion.get().getTitle());
        assertEquals("Test Content", retrievedQuestion.get().getContent());
        assertEquals(1, retrievedQuestion.get().getTags().size());
        assertEquals("test-tag", retrievedQuestion.get().getTags().get(0));
    }
    
    @Test
    public void testUpdateQuestion() throws SQLException {
        // Create and save a test question
    Question question = new Question(userOneId, "Original Title", "Original Content", subjectId);
        Question savedQuestion = questionRepository.save(question);
        
        // Update the question
        savedQuestion.setTitle("Updated Title");
        savedQuestion.setContent("Updated Content");
        savedQuestion.addTag("update-tag");
        
        // Save the update
        boolean updateResult = questionRepository.update(savedQuestion);
        
        // Verify the update was successful
        assertTrue(updateResult);
        
        // Retrieve the updated question
        Optional<Question> retrievedQuestion = questionRepository.findById(savedQuestion.getId());
        
        // Verify the retrieved question has the updated values
        assertTrue(retrievedQuestion.isPresent());
        assertEquals("Updated Title", retrievedQuestion.get().getTitle());
        assertEquals("Updated Content", retrievedQuestion.get().getContent());
        assertEquals(1, retrievedQuestion.get().getTags().size());
        assertEquals("update-tag", retrievedQuestion.get().getTags().get(0));
    }
    
    @Test
    public void testDeleteQuestion() throws SQLException {
        // Create and save a test question
    Question question = new Question(userOneId, "Delete Test", "Delete Content", subjectId);
        Question savedQuestion = questionRepository.save(question);
        
        // Verify the question exists
        assertTrue(questionRepository.findById(savedQuestion.getId()).isPresent());
        
        // Delete the question
        boolean deleteResult = questionRepository.delete(savedQuestion.getId());
        
        // Verify the deletion was successful
        assertTrue(deleteResult);
        
        // Verify the question no longer exists
        assertFalse(questionRepository.findById(savedQuestion.getId()).isPresent());
    }
    
    @Test
    public void testFindByUserId() throws SQLException {
        // Create and save questions for different users
    Question question1 = new Question(userOneId, "User 1 Question", "User 1 Content", subjectId);
    Question question2 = new Question(userTwoId, "User 2 Question", "User 2 Content", subjectId);
    Question question3 = new Question(userOneId, "Another User 1 Question", "Another User 1 Content", subjectId);
        
        questionRepository.save(question1);
        questionRepository.save(question2);
        questionRepository.save(question3);
        
        // Find questions for user 1
    List<Question> user1Questions = questionRepository.findByUserId(userOneId, 10, 0);
        
        // Verify the correct questions were found
    assertEquals(2, user1Questions.size());
    assertTrue(user1Questions.stream().allMatch(q -> q.getUserId().equals(userOneId)));
    }
    
    @Test
    public void testUpdateVoteCounts() throws SQLException {
        // Create and save a test question
    Question question = new Question(userOneId, "Vote Test", "Vote Content", subjectId);
        Question savedQuestion = questionRepository.save(question);
        
        // Update the vote counts
        boolean updateResult = questionRepository.updateVoteCounts(savedQuestion.getId(), 5, 2);
        
        // Verify the update was successful
        assertTrue(updateResult);
        
        // Retrieve the updated question
        Optional<Question> retrievedQuestion = questionRepository.findById(savedQuestion.getId());
        
        // Verify the vote counts were updated
        assertTrue(retrievedQuestion.isPresent());
        assertEquals(5, retrievedQuestion.get().getUpvotes());
        assertEquals(2, retrievedQuestion.get().getDownvotes());
    }
    
    @Test
    public void testSearch() throws SQLException {
        // Create and save test questions with different attributes
    Question question1 = new Question(userOneId, "Java Programming", "Content about Java", subjectId);
        question1.addTag("java");
    question1.addTag("programming");
    questionRepository.save(question1);
        
    Question question2 = new Question(userTwoId, "Python Basics", "Introduction to Python", subjectId);
        question2.addTag("python");
    question2.addTag("programming");
    questionRepository.save(question2);
        
    Question question3 = new Question(userOneId, "Advanced Java", "Advanced topics in Java", subjectId);
        question3.addTag("java");
    question3.addTag("advanced");
    questionRepository.save(question3);
        
        // Search for Java questions
    QuestionSearchCriteria javaCriteria = new QuestionSearchCriteria()
        .setSearchText("Java");
        List<Question> javaQuestions = questionRepository.search(javaCriteria);
        
        // Verify the correct questions were found
        assertEquals(2, javaQuestions.size());
        
        // Search for programming tag
    QuestionSearchCriteria tagCriteria = new QuestionSearchCriteria()
        .addTag("programming");
        List<Question> programmingQuestions = questionRepository.search(tagCriteria);
        
        // Verify the correct questions were found
        assertEquals(2, programmingQuestions.size());
        
        // Search for questions by user 1
    QuestionSearchCriteria userCriteria = new QuestionSearchCriteria()
        .setUserId(userOneId);
    List<Question> userQuestions = questionRepository.search(userCriteria);
        
        // Verify the correct questions were found
    assertEquals(2, userQuestions.size());
        assertTrue(userQuestions.stream().allMatch(q -> q.getUserId().equals(userOneId)));
        
        // Combined search: Java questions with advanced tag
    QuestionSearchCriteria combinedCriteria = new QuestionSearchCriteria()
        .setSearchText("Java")
        .addTag("advanced");
        List<Question> combinedQuestions = questionRepository.search(combinedCriteria);
        
        // Verify the correct questions were found
        assertEquals(1, combinedQuestions.size());
        assertEquals("Advanced Java", combinedQuestions.get(0).getTitle());
    }

    private Long createTestUser(String email, String name) throws SQLException {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash("hash" + System.nanoTime());
        user.setSalt("salt" + System.nanoTime());
        return userRepository.save(user).getId();
    }
}