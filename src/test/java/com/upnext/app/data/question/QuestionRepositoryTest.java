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
    
    private static final String CLEANUP_SQL = 
            "DELETE FROM question_tags; " +
            "DELETE FROM tags; " +
            "DELETE FROM answers; " +
            "DELETE FROM questions; " +
            "DELETE FROM subjects; " +
            "ALTER TABLE questions AUTO_INCREMENT = 1; " +
            "ALTER TABLE answers AUTO_INCREMENT = 1; " +
            "ALTER TABLE subjects AUTO_INCREMENT = 1; " +
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
        
        // Create test subject
        Subject subject = new Subject("Test Subject", "Test Subject Description");
        subjectRepository.save(subject);
    }
    
    @Test
    public void testSaveQuestion() throws SQLException {
        // Create a test question
        Question question = new Question(1L, "Test Question", "Test Content", 1L);
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
        Question question = new Question(1L, "Original Title", "Original Content", 1L);
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
        Question question = new Question(1L, "Delete Test", "Delete Content", 1L);
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
        Question question1 = new Question(1L, "User 1 Question", "User 1 Content", 1L);
        Question question2 = new Question(2L, "User 2 Question", "User 2 Content", 1L);
        Question question3 = new Question(1L, "Another User 1 Question", "Another User 1 Content", 1L);
        
        questionRepository.save(question1);
        questionRepository.save(question2);
        questionRepository.save(question3);
        
        // Find questions for user 1
        List<Question> user1Questions = questionRepository.findByUserId(1L, 10, 0);
        
        // Verify the correct questions were found
        assertEquals(2, user1Questions.size());
        assertEquals(1L, user1Questions.get(0).getUserId().longValue());
        assertEquals(1L, user1Questions.get(1).getUserId().longValue());
    }
    
    @Test
    public void testUpdateVoteCounts() throws SQLException {
        // Create and save a test question
        Question question = new Question(1L, "Vote Test", "Vote Content", 1L);
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
        Question question1 = new Question(1L, "Java Programming", "Content about Java", 1L);
        question1.addTag("java");
        question1.addTag("programming");
        questionRepository.save(question1);
        
        Question question2 = new Question(2L, "Python Basics", "Introduction to Python", 1L);
        question2.addTag("python");
        question2.addTag("programming");
        questionRepository.save(question2);
        
        Question question3 = new Question(1L, "Advanced Java", "Advanced topics in Java", 1L);
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
                .setUserId(1L);
        List<Question> userQuestions = questionRepository.search(userCriteria);
        
        // Verify the correct questions were found
        assertEquals(2, userQuestions.size());
        
        // Combined search: Java questions with advanced tag
        QuestionSearchCriteria combinedCriteria = new QuestionSearchCriteria()
                .setSearchText("Java")
                .addTag("advanced");
        List<Question> combinedQuestions = questionRepository.search(combinedCriteria);
        
        // Verify the correct questions were found
        assertEquals(1, combinedQuestions.size());
        assertEquals("Advanced Java", combinedQuestions.get(0).getTitle());
    }
}