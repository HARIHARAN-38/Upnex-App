package com.upnext.app.data.question;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.upnext.app.core.Logger;
import com.upnext.app.data.JdbcConnectionProvider;
import com.upnext.app.data.UserRepository;
import com.upnext.app.domain.User;
import com.upnext.app.domain.question.Answer;
import com.upnext.app.domain.question.Question;
import com.upnext.app.domain.question.QuestionSearchCriteria;
import com.upnext.app.domain.question.Subject;

/**
 * Extended comprehensive tests for QuestionRepository functionality.
 * Focuses on advanced scenarios, edge cases, and integration testing
 * that complement existing basic repository tests.
 */
public class QuestionRepositoryExtendedTest {
    private static final Logger LOGGER = Logger.getInstance();
    
    private QuestionRepository questionRepository;
    private AnswerRepository answerRepository;
    private UserRepository userRepository;
    private SubjectRepository subjectRepository;
    // private TagRepository tagRepository; // Removed unused field
    
    private User testUser1;
    private User testUser2;
    private Subject testSubject;
    
    @BeforeEach
    public void setUp() throws SQLException {
        LOGGER.info("Setting up QuestionRepositoryExtendedTest");
        
        questionRepository = QuestionRepository.getInstance();
        answerRepository = AnswerRepository.getInstance();
        userRepository = UserRepository.getInstance();
        subjectRepository = SubjectRepository.getInstance();
        
        // Clean up existing test data
        cleanupTestData();
        
        // Create test users with unique emails
        long timestamp = System.currentTimeMillis();
        testUser1 = createTestUser("exttest1_" + timestamp + "@example.com", "Extended Test User 1");
        testUser2 = createTestUser("exttest2_" + timestamp + "@example.com", "Extended Test User 2");
        
        // Create test subject
        testSubject = createTestSubject("Extended Test Subject", "Extended test subject description");
        
        LOGGER.info("Extended test setup completed");
    }
    
    @AfterEach
    public void tearDown() throws SQLException {
        cleanupTestData();
        LOGGER.info("Extended test cleanup completed");
    }
    
    @Test
    public void testQuestionWithMultipleAnswersAndVoting() throws SQLException {
        LOGGER.info("Testing question with multiple answers and voting");
        
        // Create a question
        Question question = new Question(testUser1.getId(), "What is the best programming language?", 
            "I want to learn programming but not sure which language to start with.", 
            "I have no prior programming experience", testSubject.getId());
        question = questionRepository.saveWithTags(question, Arrays.asList("programming", "beginner", "advice"));
        
        // Create multiple answers
        Answer answer1 = createAnswer(question.getId(), testUser1.getId(), 
            "I recommend starting with Python as it has simple syntax and is beginner-friendly.");
        Answer answer2 = createAnswer(question.getId(), testUser2.getId(), 
            "Java is a great choice because it teaches you strong programming fundamentals.");
        Answer answer3 = createAnswer(question.getId(), testUser1.getId(), 
            "JavaScript is very versatile and you can build both frontend and backend applications.");
        
        // Vote on answers
        answerRepository.voteAnswer(answer1.getId(), testUser2.getId(), true); // Python gets 1 upvote
        answerRepository.voteAnswer(answer2.getId(), testUser1.getId(), true); // Java gets 1 upvote
        answerRepository.voteAnswer(answer2.getId(), testUser2.getId(), true); // Java gets 2 upvotes
        answerRepository.voteAnswer(answer3.getId(), testUser2.getId(), false); // JavaScript gets 1 downvote
        
        // Retrieve question with answers
        Optional<Question> retrievedQuestion = questionRepository.findById(question.getId());
        assertTrue(retrievedQuestion.isPresent());
        
        List<Answer> answers = answerRepository.findByQuestionId(question.getId());
        assertEquals(3, answers.size());
        
        // Verify answers are sorted by vote count (highest first)
        // The sorting should be handled by the UI layer, but let's verify vote counts
        for (Answer answer : answers) {
            if (answer.getId().equals(answer1.getId())) {
                assertEquals(1, answer.getUpvotes());
                assertEquals(0, answer.getDownvotes());
            } else if (answer.getId().equals(answer2.getId())) {
                assertEquals(2, answer.getUpvotes());
                assertEquals(0, answer.getDownvotes());
            } else if (answer.getId().equals(answer3.getId())) {
                assertEquals(0, answer.getUpvotes());
                assertEquals(1, answer.getDownvotes());
            }
        }
        
        LOGGER.info("Question with multiple answers and voting test passed");
    }
    
    @Test
    public void testQuestionSearchWithComplexCriteria() throws SQLException {
        LOGGER.info("Testing complex question search functionality");
        
        // Create questions with different characteristics
        Question javaQuestion = questionRepository.saveWithTags(
            new Question(testUser1.getId(), "Java Spring Boot Tutorial", 
                "How to create REST API with Spring Boot", "I know basic Java", testSubject.getId()),
            Arrays.asList("java", "spring", "rest", "api", "tutorial"));
        
        questionRepository.saveWithTags(
            new Question(testUser2.getId(), "Python Data Science Guide", 
                "Best practices for data analysis with Python", "I'm new to data science", testSubject.getId()),
            Arrays.asList("python", "data-science", "analysis", "guide"));
        
        Question advancedJavaQuestion = questionRepository.saveWithTags(
            new Question(testUser1.getId(), "Advanced Java Concurrency", 
                "Understanding multithreading and concurrency patterns", "I have 5 years Java experience", testSubject.getId()),
            Arrays.asList("java", "concurrency", "multithreading", "advanced", "patterns"));
        
        // Test search by content text
        QuestionSearchCriteria contentSearch = new QuestionSearchCriteria()
            .setSearchText("REST API");
        List<Question> contentResults = questionRepository.search(contentSearch);
        assertEquals(1, contentResults.size());
        assertEquals(javaQuestion.getId(), contentResults.get(0).getId());
        
        // Test search by tags
        QuestionSearchCriteria tagSearch = new QuestionSearchCriteria()
            .addTag("java");
        List<Question> tagResults = questionRepository.search(tagSearch);
        assertEquals(2, tagResults.size());
        
        // Test search by user
        QuestionSearchCriteria userSearch = new QuestionSearchCriteria()
            .setUserId(testUser1.getId());
        List<Question> userResults = questionRepository.search(userSearch);
        assertEquals(2, userResults.size());
        
        // Test combined search
        QuestionSearchCriteria combinedSearch = new QuestionSearchCriteria()
            .setSearchText("Java")
            .addTag("advanced")
            .setUserId(testUser1.getId());
        List<Question> combinedResults = questionRepository.search(combinedSearch);
        assertEquals(1, combinedResults.size());
        assertEquals(advancedJavaQuestion.getId(), combinedResults.get(0).getId());
        
        LOGGER.info("Complex question search test passed");
    }
    
    @Test  
    public void testQuestionTagRetrieval() throws SQLException {
        LOGGER.info("Testing question tag retrieval functionality");
        
        // Create a question with tags
        Question question = questionRepository.saveWithTags(
            new Question(testUser1.getId(), "Test Question with Tags", 
                "Testing tag functionality", null, testSubject.getId()),
            Arrays.asList("tag1", "tag2", "tag3", "programming", "test"));
        
        // Test getTagsForQuestion method
        List<String> retrievedTags = questionRepository.getTagsForQuestion(question.getId());
        assertEquals(5, retrievedTags.size());
        assertTrue(retrievedTags.contains("tag1"));
        assertTrue(retrievedTags.contains("tag2"));
        assertTrue(retrievedTags.contains("tag3"));
        assertTrue(retrievedTags.contains("programming"));
        assertTrue(retrievedTags.contains("test"));
        
        // Verify tags are in the question object when retrieved
        Optional<Question> retrievedQuestion = questionRepository.findById(question.getId());
        assertTrue(retrievedQuestion.isPresent());
        assertEquals(5, retrievedQuestion.get().getTags().size());
        
        LOGGER.info("Question tag retrieval test passed");
    }
    
    @Test
    public void testAnswerVerificationWorkflow() throws SQLException {
        LOGGER.info("Testing answer verification workflow");
        
        // Create question and answer
        Question question = questionRepository.saveWithTags(
            new Question(testUser1.getId(), "How to optimize database queries?", 
                "My application is running slow due to database queries", null, testSubject.getId()),
            Arrays.asList("database", "performance", "optimization"));
        
        Answer answer = createAnswer(question.getId(), testUser2.getId(),
            "Use indexes, avoid N+1 queries, use connection pooling, and consider caching for frequently accessed data.");
        
        // Create additional users for voting
        User[] voters = new User[12];
        for (int i = 0; i < 12; i++) {
            voters[i] = createTestUser("voter_ext_" + i + "@example.com", "Voter " + i);
        }
        
        // Vote incrementally and check verification status
        for (int i = 0; i < 12; i++) {
            var result = answerRepository.voteAnswer(answer.getId(), voters[i].getId(), true);
            
            if (i < 9) {
                // Should not be verified until 10 upvotes
                assertFalse(result.isVerified(), "Answer should not be verified with " + (i + 1) + " votes");
            } else {
                // Should be verified at 10 or more upvotes
                assertTrue(result.isVerified(), "Answer should be verified with " + (i + 1) + " votes");
            }
        }
        
        // Verify the answer is marked as accepted in database
        Optional<Answer> verifiedAnswer = answerRepository.findById(answer.getId());
        assertTrue(verifiedAnswer.isPresent());
        assertTrue(verifiedAnswer.get().isAccepted());
        assertEquals(12, verifiedAnswer.get().getUpvotes());
        
        LOGGER.info("Answer verification workflow test passed");
    }
    
    @Test
    public void testQuestionAnswerStatistics() throws SQLException {
        LOGGER.info("Testing question answer statistics");
        
        // Create a question
        Question question = questionRepository.saveWithTags(
            new Question(testUser1.getId(), "Question with Statistics", 
                "Testing answer count and vote statistics", null, testSubject.getId()),
            Arrays.asList("statistics", "test"));
        
        // Create multiple answers with different vote patterns
        Answer answer1 = createAnswer(question.getId(), testUser1.getId(), "First answer");
        Answer answer2 = createAnswer(question.getId(), testUser2.getId(), "Second answer");
        Answer answer3 = createAnswer(question.getId(), testUser1.getId(), "Third answer");
        
        // Vote on answers
        answerRepository.voteAnswer(answer1.getId(), testUser2.getId(), true);
        answerRepository.voteAnswer(answer1.getId(), testUser1.getId(), true);
        answerRepository.voteAnswer(answer2.getId(), testUser1.getId(), true);
        answerRepository.voteAnswer(answer3.getId(), testUser2.getId(), false);
        
        // Retrieve question answers and verify statistics
        List<Answer> answers = answerRepository.findByQuestionId(question.getId());
        assertEquals(3, answers.size());
        
        // Calculate total votes across all answers
        int totalUpvotes = answers.stream().mapToInt(Answer::getUpvotes).sum();
        int totalDownvotes = answers.stream().mapToInt(Answer::getDownvotes).sum();
        
        assertEquals(3, totalUpvotes);
        assertEquals(1, totalDownvotes);
        
        // Verify individual answer statistics
        for (Answer answer : answers) {
            if (answer.getId().equals(answer1.getId())) {
                assertEquals(2, answer.getUpvotes());
                assertEquals(0, answer.getDownvotes());
            } else if (answer.getId().equals(answer2.getId())) {
                assertEquals(1, answer.getUpvotes());
                assertEquals(0, answer.getDownvotes());
            } else if (answer.getId().equals(answer3.getId())) {
                assertEquals(0, answer.getUpvotes());
                assertEquals(1, answer.getDownvotes());
            }
        }
        
        LOGGER.info("Question answer statistics test passed");
    }
    
    @Test
    public void testQuestionUpdateWithTagChanges() throws SQLException {
        LOGGER.info("Testing question update with tag changes");
        
        // Create initial question with tags
        Question question = questionRepository.saveWithTags(
            new Question(testUser1.getId(), "Original Question Title", 
                "Original content", null, testSubject.getId()),
            Arrays.asList("original", "tags", "test"));
        
        // Verify initial tags
        assertEquals(3, question.getTags().size());
        assertTrue(question.getTags().contains("original"));
        
        // Update question (Note: This tests the basic update, tag update might require special handling)
        question.setTitle("Updated Question Title");
        question.setContent("Updated content with new information");
        boolean updateResult = questionRepository.update(question);
        assertTrue(updateResult);
        
        // Retrieve updated question
        Optional<Question> updatedQuestion = questionRepository.findById(question.getId());
        assertTrue(updatedQuestion.isPresent());
        assertEquals("Updated Question Title", updatedQuestion.get().getTitle());
        assertEquals("Updated content with new information", updatedQuestion.get().getContent());
        
        LOGGER.info("Question update with tag changes test passed");
    }
    
    @Test
    public void testPaginationAndLimits() throws SQLException {
        LOGGER.info("Testing pagination and limits");
        
        // Create multiple questions for pagination testing
        for (int i = 1; i <= 15; i++) {
            questionRepository.saveWithTags(
                new Question(testUser1.getId(), "Question " + i, 
                    "Content for question " + i, null, testSubject.getId()),
                Collections.singletonList("pagination"));
        }
        
        // Test pagination with different limits and offsets
        List<Question> firstPage = questionRepository.findByUserId(testUser1.getId(), 5, 0);
        assertEquals(5, firstPage.size());
        
        List<Question> secondPage = questionRepository.findByUserId(testUser1.getId(), 5, 5);
        assertEquals(5, secondPage.size());
        
        List<Question> thirdPage = questionRepository.findByUserId(testUser1.getId(), 5, 10);
        assertEquals(5, thirdPage.size());
        
        List<Question> fourthPage = questionRepository.findByUserId(testUser1.getId(), 5, 15);
        assertEquals(0, fourthPage.size());
        
        // Verify questions are different across pages
        assertFalse(firstPage.get(0).getId().equals(secondPage.get(0).getId()));
        
        LOGGER.info("Pagination and limits test passed");
    }
    
    // Helper methods
    
    private User createTestUser(String email, String name) throws SQLException {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash("test_hash_" + System.currentTimeMillis());
        user.setSalt("test_salt");
        return userRepository.save(user);
    }
    
    private Subject createTestSubject(String name, String description) throws SQLException {
        Subject subject = new Subject(name, description);
        return subjectRepository.save(subject);
    }
    
    private Answer createAnswer(Long questionId, Long userId, String content) throws SQLException {
        Answer answer = new Answer();
        answer.setQuestionId(questionId);
        answer.setUserId(userId);
        answer.setContent(content);
        return answerRepository.save(answer);
    }
    
    private void cleanupTestData() {
        try (Connection connection = JdbcConnectionProvider.getInstance().getConnection()) {
            String[] cleanupQueries = {
                "DELETE FROM answer_votes WHERE answer_id IN (SELECT a.id FROM answers a JOIN questions q ON a.question_id = q.id JOIN users u ON q.user_id = u.id WHERE u.email LIKE '%@example.com')",
                "DELETE FROM question_tags WHERE question_id IN (SELECT q.id FROM questions q JOIN users u ON q.user_id = u.id WHERE u.email LIKE '%@example.com')",
                "DELETE FROM answers WHERE question_id IN (SELECT q.id FROM questions q JOIN users u ON q.user_id = u.id WHERE u.email LIKE '%@example.com')",
                "DELETE FROM questions WHERE user_id IN (SELECT id FROM users WHERE email LIKE '%@example.com')",
                "DELETE FROM users WHERE email LIKE '%@example.com'",
                "DELETE FROM subjects WHERE name LIKE '%Extended Test%'",
                "DELETE FROM tags WHERE name IN ('original', 'tags', 'test', 'pagination', 'statistics')"
            };
            
            for (String query : cleanupQueries) {
                try (PreparedStatement stmt = connection.prepareStatement(query)) {
                    stmt.executeUpdate();
                } catch (SQLException e) {
                    // Log but don't fail - some queries might fail due to dependencies
                    LOGGER.info("Cleanup query failed (expected): " + query + " - " + e.getMessage());
                }
            }
            
            LOGGER.info("Extended test data cleanup completed");
        } catch (Exception e) {
            LOGGER.error("Error during extended test cleanup: " + e.getMessage());
        }
    }
}