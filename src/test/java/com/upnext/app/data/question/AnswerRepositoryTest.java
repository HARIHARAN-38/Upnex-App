package com.upnext.app.data.question;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.upnext.app.core.Logger;
import com.upnext.app.data.JdbcConnectionProvider;
import com.upnext.app.data.UserRepository;
import com.upnext.app.data.question.AnswerRepository.VoteResult;
import com.upnext.app.domain.User;
import com.upnext.app.domain.question.Answer;
import com.upnext.app.domain.question.Question;

/**
 * Test class for AnswerRepository voting functionality.
 * Tests answer persistence, voting system, and verified answer logic.
 */
public class AnswerRepositoryTest {
    private static final Logger LOGGER = Logger.getInstance();
    
    private AnswerRepository answerRepository;
    private QuestionRepository questionRepository;
    private UserRepository userRepository;
    
    private User testUser1;
    private User testUser2;
    private Question testQuestion;
    private Answer testAnswer;
    
    @BeforeEach
    public void setUp() throws SQLException {
        LOGGER.info("Setting up AnswerRepository test");
        
        answerRepository = AnswerRepository.getInstance();
        questionRepository = QuestionRepository.getInstance();
        userRepository = UserRepository.getInstance();
        
        // Clean up existing test data
        cleanupTestData();
        
        // Create test users with unique emails to avoid conflicts
        long timestamp = System.currentTimeMillis();
        testUser1 = createTestUser("testuser1_" + timestamp + "@example.com", "Test User 1");
        testUser2 = createTestUser("testuser2_" + timestamp + "@example.com", "Test User 2");
        
        // Create test question
        testQuestion = createTestQuestion();
        
        // Create test answer
        testAnswer = createTestAnswer();
        
        LOGGER.info("Test setup completed");
    }
    
    @Test
    public void testSaveAnswer() throws SQLException {
        LOGGER.info("Testing answer save functionality");
        
        Answer answer = new Answer();
        answer.setQuestionId(testQuestion.getId());
        answer.setUserId(testUser1.getId());
        answer.setContent("This is a test answer for save functionality.");
        
        Answer savedAnswer = answerRepository.save(answer);
        
        assertNotNull(savedAnswer.getId(), "Saved answer should have an ID");
        assertEquals(testQuestion.getId(), savedAnswer.getQuestionId());
        assertEquals(testUser1.getId(), savedAnswer.getUserId());
        assertEquals("This is a test answer for save functionality.", savedAnswer.getContent());
        assertEquals(0, savedAnswer.getUpvotes(), "New answer should have 0 upvotes");
        assertEquals(0, savedAnswer.getDownvotes(), "New answer should have 0 downvotes");
        assertFalse(savedAnswer.isAccepted(), "New answer should not be accepted");
        
        LOGGER.info("Answer save test passed");
    }
    
    @Test
    public void testFindAnswersByQuestionId() throws SQLException {
        LOGGER.info("Testing find answers by question ID");
        
        List<Answer> answers = answerRepository.findByQuestionId(testQuestion.getId());
        
        assertNotNull(answers);
        assertEquals(1, answers.size(), "Should find exactly one answer");
        
        Answer foundAnswer = answers.get(0);
        assertEquals(testAnswer.getId(), foundAnswer.getId());
        assertEquals(testAnswer.getContent(), foundAnswer.getContent());
        
        LOGGER.info("Find answers by question ID test passed");
    }
    
    @Test
    public void testVoteAnswerUpvote() throws SQLException {
        LOGGER.info("Testing answer upvote functionality");
        
        VoteResult result = answerRepository.voteAnswer(testAnswer.getId(), testUser1.getId(), true);
        
        assertNotNull(result);
        assertEquals(1, result.getUpvotes(), "Should have 1 upvote");
        assertEquals(0, result.getDownvotes(), "Should have 0 downvotes");
        assertEquals(1, result.getNetVotes(), "Net votes should be 1");
        assertFalse(result.isVerified(), "Answer should not be verified with only 1 upvote");
        
        LOGGER.info("Answer upvote test passed");
    }
    
    @Test
    public void testVoteAnswerDownvote() throws SQLException {
        LOGGER.info("Testing answer downvote functionality");
        
        VoteResult result = answerRepository.voteAnswer(testAnswer.getId(), testUser1.getId(), false);
        
        assertNotNull(result);
        assertEquals(0, result.getUpvotes(), "Should have 0 upvotes");
        assertEquals(1, result.getDownvotes(), "Should have 1 downvote");
        assertEquals(-1, result.getNetVotes(), "Net votes should be -1");
        assertFalse(result.isVerified(), "Answer should not be verified with downvotes");
        
        LOGGER.info("Answer downvote test passed");
    }
    
    @Test
    public void testVoteAnswerChangeVote() throws SQLException {
        LOGGER.info("Testing vote change functionality");
        
        // First vote upvote
        VoteResult result1 = answerRepository.voteAnswer(testAnswer.getId(), testUser1.getId(), true);
        assertEquals(1, result1.getUpvotes());
        assertEquals(0, result1.getDownvotes());
        
        // Change to downvote
        VoteResult result2 = answerRepository.voteAnswer(testAnswer.getId(), testUser1.getId(), false);
        assertEquals(0, result2.getUpvotes(), "Upvotes should be 0 after change");
        assertEquals(1, result2.getDownvotes(), "Downvotes should be 1 after change");
        
        LOGGER.info("Vote change test passed");
    }
    
    @Test
    public void testVoteAnswerRemoveVote() throws SQLException {
        LOGGER.info("Testing vote removal functionality");
        
        // First vote upvote
        VoteResult result1 = answerRepository.voteAnswer(testAnswer.getId(), testUser1.getId(), true);
        assertEquals(1, result1.getUpvotes());
        
        // Vote same way again to remove
        VoteResult result2 = answerRepository.voteAnswer(testAnswer.getId(), testUser1.getId(), true);
        assertEquals(0, result2.getUpvotes(), "Upvotes should be 0 after removal");
        assertEquals(0, result2.getDownvotes(), "Downvotes should remain 0");
        
        LOGGER.info("Vote removal test passed");
    }
    
    @Test
    public void testMultipleUsersVoting() throws SQLException {
        LOGGER.info("Testing multiple users voting");
        
        // User 1 upvotes
        VoteResult result1 = answerRepository.voteAnswer(testAnswer.getId(), testUser1.getId(), true);
        assertEquals(1, result1.getUpvotes());
        assertEquals(0, result1.getDownvotes());
        
        // User 2 upvotes
        VoteResult result2 = answerRepository.voteAnswer(testAnswer.getId(), testUser2.getId(), true);
        assertEquals(2, result2.getUpvotes());
        assertEquals(0, result2.getDownvotes());
        
        // User 2 changes to downvote
        VoteResult result3 = answerRepository.voteAnswer(testAnswer.getId(), testUser2.getId(), false);
        assertEquals(1, result3.getUpvotes(), "Should have 1 upvote after user 2 changes");
        assertEquals(1, result3.getDownvotes(), "Should have 1 downvote after user 2 changes");
        
        LOGGER.info("Multiple users voting test passed");
    }
    
    @Test
    public void testVerifiedAnswerLogic() throws SQLException {
        LOGGER.info("Testing verified answer logic");
        
        // Create 10 additional test users to reach verification threshold
        for (int i = 0; i < 10; i++) {
            User user = createTestUser("voter" + i + "@example.com", "Voter " + i);
            answerRepository.voteAnswer(testAnswer.getId(), user.getId(), true);
        }
        
        // Get the final vote result
        VoteResult result = answerRepository.voteAnswer(testAnswer.getId(), testUser1.getId(), true);
        
        assertTrue(result.getUpvotes() >= 10, "Should have at least 10 upvotes");
        assertTrue(result.isVerified(), "Answer should be verified with 10+ upvotes");
        
        // Verify the answer is marked as accepted in the database
        Optional<Answer> updatedAnswer = answerRepository.findById(testAnswer.getId());
        assertTrue(updatedAnswer.isPresent());
        assertTrue(updatedAnswer.get().isAccepted(), "Answer should be marked as accepted");
        
        LOGGER.info("Verified answer logic test passed");
    }
    
    @Test
    public void testInvalidVoteOperations() throws SQLException {
        LOGGER.info("Testing invalid vote operations");
        
        // Test null answer ID
        assertThrows(IllegalArgumentException.class, () -> {
            answerRepository.voteAnswer(null, testUser1.getId(), true);
        }, "Should throw exception for null answer ID");
        
        // Test null user ID
        assertThrows(IllegalArgumentException.class, () -> {
            answerRepository.voteAnswer(testAnswer.getId(), null, true);
        }, "Should throw exception for null user ID");
        
        LOGGER.info("Invalid vote operations test passed");
    }
    
    // Helper methods
    
    private User createTestUser(String email, String name) throws SQLException {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash("test_hash");
        user.setSalt("test_salt");
        return userRepository.save(user);
    }
    
    private Question createTestQuestion() throws SQLException {
        Question question = new Question();
        question.setUserId(testUser1.getId());
        question.setTitle("Test Question for Answer Voting");
        question.setContent("This is a test question to test answer voting functionality.");
        return questionRepository.save(question);
    }
    
    private Answer createTestAnswer() throws SQLException {
        Answer answer = new Answer();
        answer.setQuestionId(testQuestion.getId());
        answer.setUserId(testUser2.getId());
        answer.setContent("This is a test answer for voting tests.");
        return answerRepository.save(answer);
    }
    
    private void cleanupTestData() {
        try (Connection connection = JdbcConnectionProvider.getInstance().getConnection()) {
            // Clean up in proper order to respect foreign key constraints
            String[] cleanupQueries = {
                "DELETE FROM answer_votes WHERE answer_id IN (SELECT id FROM answers WHERE content LIKE 'This is a test answer%')",
                "DELETE FROM answers WHERE content LIKE 'This is a test answer%'",
                "DELETE FROM questions WHERE title LIKE 'Test Question%'",
                "DELETE FROM users WHERE email LIKE '%@example.com'"
            };
            
            for (String query : cleanupQueries) {
                try (PreparedStatement stmt = connection.prepareStatement(query)) {
                    stmt.executeUpdate();
                }
            }
            
            LOGGER.info("Test data cleanup completed");
        } catch (Exception e) {
            LOGGER.error("Error during test cleanup: " + e.getMessage());
        }
    }
}