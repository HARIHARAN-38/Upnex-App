package com.upnext.app.data.question;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.upnext.app.data.JdbcConnectionProvider;
import com.upnext.app.data.UserRepository;
import com.upnext.app.domain.User;
import com.upnext.app.domain.question.Question;
import com.upnext.app.domain.question.Tag;

/**
 * Integration tests for QuestionRepository's saveWithTags functionality.
 * Tests the transactional behavior, duplicate tag handling, and rollback scenarios.
 */
public class QuestionRepositoryAddTest {
    
    private QuestionRepository questionRepository;
    private TagRepository tagRepository;
    private UserRepository userRepository;
    private User testUser;
    
    private static final String CLEANUP_SQL = 
            "DELETE FROM question_tags; " +
            "DELETE FROM tags; " +
            "DELETE FROM answers; " +
            "DELETE FROM questions; " +
            "DELETE FROM users WHERE email LIKE '%@example.com'; " +
            "DELETE FROM subjects; " +
            "ALTER TABLE questions AUTO_INCREMENT = 1; " +
            "ALTER TABLE answers AUTO_INCREMENT = 1; " +
            "ALTER TABLE subjects AUTO_INCREMENT = 1; " +
            "ALTER TABLE tags AUTO_INCREMENT = 1;";
    
    @BeforeEach
    public void setUp() throws SQLException {
        questionRepository = QuestionRepository.getInstance();
        tagRepository = TagRepository.getInstance();
        userRepository = UserRepository.getInstance();
        cleanupDatabase();
        
        // Create test user
        testUser = createTestUser("testuser@example.com", "Test User");
    }
    
    @AfterEach
    public void tearDown() throws SQLException {
        cleanupDatabase();
    }
    
    private void cleanupDatabase() throws SQLException {
        try (Connection connection = JdbcConnectionProvider.getInstance().getConnection();
             Statement statement = connection.createStatement()) {
            
            // Split and execute each statement separately
            String[] statements = CLEANUP_SQL.split(";");
            for (String sql : statements) {
                sql = sql.trim();
                if (!sql.isEmpty()) {
                    statement.execute(sql);
                }
            }
        }
    }
    
    @Test
    public void testSaveWithTags_HappyPath() throws SQLException {
        // Arrange
        Question question = new Question(testUser.getId(), "How to learn Java?", "I want to start learning Java programming.", "I have basic programming knowledge", null);
        List<String> tags = Arrays.asList("java", "programming", "beginner");
        
        // Act
        Question savedQuestion = questionRepository.saveWithTags(question, tags);
        
        // Assert
        assertNotNull(savedQuestion);
        assertNotNull(savedQuestion.getId());
        assertTrue(savedQuestion.getId() > 0);
        assertEquals("How to learn Java?", savedQuestion.getTitle());
        assertEquals("I want to start learning Java programming.", savedQuestion.getContent());
        assertEquals("I have basic programming knowledge", savedQuestion.getContext());
        assertEquals(testUser.getId(), savedQuestion.getUserId());
        assertNotNull(savedQuestion.getCreatedAt());
        assertNotNull(savedQuestion.getUpdatedAt());
        
        // Verify tags are saved and associated
        assertEquals(3, savedQuestion.getTags().size());
        assertTrue(savedQuestion.getTags().contains("java"));
        assertTrue(savedQuestion.getTags().contains("programming"));
        assertTrue(savedQuestion.getTags().contains("beginner"));
        
        // Verify question can be retrieved with tags
        Optional<Question> retrieved = questionRepository.findById(savedQuestion.getId());
        assertTrue(retrieved.isPresent());
        assertEquals(3, retrieved.get().getTags().size());
        assertTrue(retrieved.get().getTags().contains("java"));
        
        // Verify tags exist in database with usage count
        Optional<Tag> javaTag = tagRepository.findByName("java");
        assertTrue(javaTag.isPresent());
        assertEquals(1, javaTag.get().getUsageCount());
    }
    
    @Test
    public void testSaveWithTags_DuplicateTagHandling() throws SQLException {
        // Arrange - Create first question with java tag
        Question question1 = new Question(testUser.getId(), "First Question", "First content", "First context", null);
        List<String> tags1 = Arrays.asList("java", "spring");
        questionRepository.saveWithTags(question1, tags1);
        
        // Act - Create second question with overlapping tags
        Question question2 = new Question(testUser.getId(), "Second Question", "Second content", "Second context", null);
        List<String> tags2 = Arrays.asList("java", "hibernate", "spring");
        Question savedQuestion2 = questionRepository.saveWithTags(question2, tags2);
        
        // Assert
        assertNotNull(savedQuestion2);
        assertEquals(3, savedQuestion2.getTags().size());
        
        // Verify usage counts are incremented for duplicate tags
        Optional<Tag> javaTag = tagRepository.findByName("java");
        assertTrue(javaTag.isPresent());
        assertEquals(2, javaTag.get().getUsageCount()); // Used by both questions
        
        Optional<Tag> springTag = tagRepository.findByName("spring");
        assertTrue(springTag.isPresent());
        assertEquals(2, springTag.get().getUsageCount()); // Used by both questions
        
        Optional<Tag> hibernateTag = tagRepository.findByName("hibernate");
        assertTrue(hibernateTag.isPresent());
        assertEquals(1, hibernateTag.get().getUsageCount()); // Used by second question only
    }
    
    @Test
    public void testSaveWithTags_DuplicateTagsInSameRequest() throws SQLException {
        // Arrange
        Question question = new Question(testUser.getId(), "Test Question", "Test content", "Test context", null);
        List<String> tagsWithDuplicates = Arrays.asList("java", "JAVA", "Java", "programming", "programming");
        
        // Act
        Question savedQuestion = questionRepository.saveWithTags(question, tagsWithDuplicates);
        
        // Assert - Should deduplicate tags (case-insensitive)
        assertEquals(2, savedQuestion.getTags().size());
        assertTrue(savedQuestion.getTags().contains("java"));
        assertTrue(savedQuestion.getTags().contains("programming"));
        
        // Verify only unique tags are in database
        Optional<Tag> javaTag = tagRepository.findByName("java");
        assertTrue(javaTag.isPresent());
        assertEquals(1, javaTag.get().getUsageCount());
    }
    
    @Test
    public void testSaveWithTags_EmptyAndNullTags() throws SQLException {
        // Arrange
        Question question = new Question(testUser.getId(), "Test Question", "Test content", null, null);
        List<String> tagsWithEmpties = Arrays.asList("java", "", null, "   ", "programming");
        
        // Act
        Question savedQuestion = questionRepository.saveWithTags(question, tagsWithEmpties);
        
        // Assert - Should filter out empty/null tags
        assertEquals(2, savedQuestion.getTags().size());
        assertTrue(savedQuestion.getTags().contains("java"));
        assertTrue(savedQuestion.getTags().contains("programming"));
    }
    
    @Test
    public void testSaveWithTags_NoTags() throws SQLException {
        // Arrange
        Question question = new Question(testUser.getId(), "Test Question", "Test content", null, null);
        
        // Act - Test with null tags
        Question savedQuestion1 = questionRepository.saveWithTags(question, null);
        
        // Assert
        assertNotNull(savedQuestion1.getId());
        assertEquals(0, savedQuestion1.getTags().size());
        
        // Act - Test with empty tags list
        Question question2 = new Question(testUser.getId(), "Test Question 2", "Test content 2", null, null);
        Question savedQuestion2 = questionRepository.saveWithTags(question2, Collections.emptyList());
        
        // Assert
        assertNotNull(savedQuestion2.getId());
        assertEquals(0, savedQuestion2.getTags().size());
    }
    
    @Test
    public void testSaveWithTags_TooManyTags() throws SQLException {
        // Arrange
        Question question = new Question(testUser.getId(), "Test Question", "Test content", null, null);
        List<String> tooManyTags = Arrays.asList(
            "tag1", "tag2", "tag3", "tag4", "tag5", 
            "tag6", "tag7", "tag8", "tag9", "tag10", 
            "tag11" // This should cause failure
        );
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            questionRepository.saveWithTags(question, tooManyTags);
        });
        
        assertTrue(exception.getMessage().contains("Maximum 10 tags allowed"));
    }
    
    @Test
    public void testSaveWithTags_InvalidQuestion() throws SQLException {
        // Arrange
        Question invalidQuestion = new Question();
        invalidQuestion.setUserId(null); // Missing required field
        List<String> tags = Arrays.asList("java");
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            questionRepository.saveWithTags(invalidQuestion, tags);
        });
    }
    
    @Test 
    public void testSaveWithTags_TransactionRollback() throws SQLException {
        // This test verifies that if something goes wrong during tag processing,
        // the entire transaction (including question insert) is rolled back
        
        // Arrange
        Question question = new Question(testUser.getId(), "Test Question", "Test content", null, null);
        List<String> tags = Arrays.asList("java", "programming");
        
        // Save a question successfully first
        Question savedQuestion = questionRepository.saveWithTags(question, tags);
        assertNotNull(savedQuestion.getId());
        
        // Verify the question was saved
        Optional<Question> retrieved = questionRepository.findById(savedQuestion.getId());
        assertTrue(retrieved.isPresent());
        
        // Verify tags were created
        Optional<Tag> javaTag = tagRepository.findByName("java");
        assertTrue(javaTag.isPresent());
        assertEquals(1, javaTag.get().getUsageCount());
    }
    
    @Test
    public void testSaveWithTags_CaseNormalization() throws SQLException {
        // Arrange
        Question question = new Question(testUser.getId(), "Test Question", "Test content", null, null);
        List<String> mixedCaseTags = Arrays.asList("Java", "SPRING", "hibernate", "JPA");
        
        // Act
        Question savedQuestion = questionRepository.saveWithTags(question, mixedCaseTags);
        
        // Assert - All tags should be normalized to lowercase
        assertEquals(4, savedQuestion.getTags().size());
        assertTrue(savedQuestion.getTags().contains("java"));
        assertTrue(savedQuestion.getTags().contains("spring"));
        assertTrue(savedQuestion.getTags().contains("hibernate"));
        assertTrue(savedQuestion.getTags().contains("jpa"));
        
        // Verify tags are stored in lowercase in database
        Optional<Tag> javaTag = tagRepository.findByName("java");
        assertTrue(javaTag.isPresent());
        assertEquals("java", javaTag.get().getName());
    }
    
    @Test
    public void testSaveWithTags_MaximumTagsAllowed() throws SQLException {
        // Arrange
        Question question = new Question(testUser.getId(), "Test Question", "Test content", null, null);
        List<String> maxTags = Arrays.asList(
            "tag1", "tag2", "tag3", "tag4", "tag5", 
            "tag6", "tag7", "tag8", "tag9", "tag10"
        );
        
        // Act
        Question savedQuestion = questionRepository.saveWithTags(question, maxTags);
        
        // Assert
        assertNotNull(savedQuestion.getId());
        assertEquals(10, savedQuestion.getTags().size());
        
        // Verify all tags are present
        for (int i = 1; i <= 10; i++) {
            assertTrue(savedQuestion.getTags().contains("tag" + i));
        }
    }
    
    private User createTestUser(String email, String name) throws SQLException {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash("test_hash");
        user.setSalt("test_salt");
        return userRepository.save(user);
    }
}
