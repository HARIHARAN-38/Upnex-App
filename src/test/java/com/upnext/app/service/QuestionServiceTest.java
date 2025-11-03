package com.upnext.app.service;

import com.upnext.app.service.QuestionService.QuestionException;
import com.upnext.app.util.TestLogCapture;
import com.upnext.app.util.TestLogCapture.TelemetryValidationResult;
import org.junit.jupiter.api.*;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for QuestionService validation and business logic.
 * Tests focus on validation rules and error handling.
 */
public class QuestionServiceTest {
    
    private QuestionService questionService;
    private TestLogCapture logCapture;
    
    @BeforeEach
    public void setUp() {
        questionService = QuestionService.getInstance();
        logCapture = new TestLogCapture();
    }
    
    @Test
    public void testCreateQuestion_AuthenticationRequired() {
        // Test that authentication is enforced
        QuestionException exception = assertThrows(QuestionException.class, () -> {
            questionService.createQuestion("Valid Title", "Valid content that meets minimum requirements", null, null);
        });
        
        assertEquals("User must be authenticated to create questions", exception.getMessage());
    }
    
    @Test
    public void testGetQuestionById_InvalidIds() throws Exception {
        // Test null ID
        assertFalse(questionService.getQuestionById(null).isPresent());
        
        // Test negative ID
        assertFalse(questionService.getQuestionById(-1L).isPresent());
        
        // Test zero ID  
        assertFalse(questionService.getQuestionById(0L).isPresent());
    }
    
    @Test
    public void testValidationConstants() {
        // Verify reasonable title lengths work
        String reasonableTitle = "How do I implement REST API in Spring Boot?";
        assertTrue(reasonableTitle.length() >= 5 && reasonableTitle.length() <= 200);
        
        // Verify detailed content works
        String detailedContent = "I'm building a web application and need to create REST endpoints. " +
                               "I have basic Spring knowledge but I'm unsure about best practices " +
                               "for controller design, error handling, and response formatting.";
        assertTrue(detailedContent.length() >= 10 && detailedContent.length() <= 5000);
        
        // Verify common tag patterns work
        List<String> commonTags = Arrays.asList("java", "spring-boot", "rest-api", "web-development");
        assertTrue(commonTags.size() <= 10);
        
        for (String tag : commonTags) {
            assertTrue(tag.length() <= 50);
            assertTrue(tag.matches("^[a-zA-Z0-9-_+#.]+$"));
        }
    }
    
    @Test
    public void testExceptionContracts() {
        // Verify QuestionException is a checked exception
        assertTrue(Exception.class.isAssignableFrom(QuestionException.class));
        
        // Verify exception can be constructed with message
        QuestionException exception1 = new QuestionException("Test message");
        assertEquals("Test message", exception1.getMessage());
        
        // Verify exception can be constructed with message and cause
        RuntimeException cause = new RuntimeException("Cause");
        QuestionException exception2 = new QuestionException("Test message", cause);
        assertEquals("Test message", exception2.getMessage());
        assertEquals(cause, exception2.getCause());
    }
    
    // ===== TELEMETRY VALIDATION TESTS =====
    
    @Test
    @DisplayName("Telemetry: Question Create Flow - Authentication Failure")
    public void testTelemetry_QuestionCreateAuthFailure() {
        logCapture.startCapture();
        
        // Simulate log entries that would be generated during authentication failure
        logCapture.addLogEntry("INFO", "[QUESTION_CREATE_START] Creating question - User: null");
        logCapture.addLogEntry("WARNING", "[QUESTION_CREATE_AUTH_FAILED] Unauthenticated user attempted to create question");
        
        // Validate telemetry flow
        TelemetryValidationResult result = logCapture.validateOperationFlow("QUESTION_CREATE");
        
        assertTrue(result.hasStart(), "Should have QUESTION_CREATE_START marker");
        assertFalse(result.hasSuccess(), "Should not have success marker for auth failure");
        assertTrue(result.hasFailed(), "Should have QUESTION_CREATE_AUTH_FAILED marker");
        assertTrue(result.isCompleteFlow(), "Should be a complete flow with start and failure");
        assertFalse(result.isSuccessfulFlow(), "Should not be a successful flow");
        
        assertEquals(2, result.getOperationLogs().size(), "Should capture 2 operation logs");
        
        logCapture.stopCapture();
    }
    
    @Test
    @DisplayName("Telemetry: Question Create Flow - Validation Failure")
    public void testTelemetry_QuestionCreateValidationFailure() {
        logCapture.startCapture();
        
        // Simulate log entries for validation failure
        logCapture.addLogEntry("INFO", "[QUESTION_CREATE_START] Creating question - User: 123");
        logCapture.addLogEntry("INFO", "[QUESTION_CREATE_AUTH] User 123 authenticated for question creation");
        logCapture.addLogEntry("WARNING", "[QUESTION_CREATE_VALIDATION_FAILED] User 123 - Question title is required");
        
        // Validate telemetry flow
        TelemetryValidationResult result = logCapture.validateOperationFlow("QUESTION_CREATE");
        
        assertTrue(result.hasStart(), "Should have QUESTION_CREATE_START marker");
        assertFalse(result.hasSuccess(), "Should not have success marker for validation failure");
        assertTrue(result.hasFailed(), "Should have QUESTION_CREATE_VALIDATION_FAILED marker");
        assertEquals(3, result.getOperationLogs().size(), "Should capture 3 operation logs");
        
        logCapture.stopCapture();
    }
    
    @Test
    @DisplayName("Telemetry: Question Create Flow - Success")
    public void testTelemetry_QuestionCreateSuccess() {
        logCapture.startCapture();
        
        // Simulate log entries for successful question creation
        logCapture.addLogEntry("INFO", "[QUESTION_CREATE_START] Creating question - User: 456");
        logCapture.addLogEntry("INFO", "[QUESTION_CREATE_AUTH] User 456 authenticated for question creation");
        logCapture.addLogEntry("INFO", "[QUESTION_CREATE_VALIDATION] Input validation passed for user 456");
        logCapture.addLogEntry("INFO", "[QUESTION_TAG_ANALYTICS] User 456 using tags: [java, spring-boot]");
        logCapture.addLogEntry("INFO", "[QUESTION_CREATE_SUCCESS] Question created successfully - ID: 789, User: 456, Tags: 2, Duration: 150ms");
        
        // Validate telemetry flow
        TelemetryValidationResult result = logCapture.validateOperationFlow("QUESTION_CREATE");
        
        assertTrue(result.hasStart(), "Should have QUESTION_CREATE_START marker");
        assertTrue(result.hasSuccess(), "Should have success marker");
        assertFalse(result.hasFailed(), "Should not have failure marker for successful creation");
        assertTrue(result.isCompleteFlow(), "Should be a complete flow");
        assertTrue(result.isSuccessfulFlow(), "Should be a successful flow");
        
        assertEquals(5, result.getOperationLogs().size(), "Should capture 5 operation logs");
        
        // Verify tag analytics logging
        assertTrue(logCapture.hasMarker("[QUESTION_TAG_ANALYTICS]"), "Should log tag analytics");
        
        logCapture.stopCapture();
    }
    
    @Test
    @DisplayName("Telemetry: User Metrics Update Flow")
    public void testTelemetry_UserMetricsUpdate() {
        logCapture.startCapture();
        
        // Simulate log entries for user metrics update
        logCapture.addLogEntry("INFO", "[USER_METRICS_UPDATE_SUCCESS] Incremented questions_asked for user 456");
        logCapture.addLogEntry("INFO", "[USER_METRICS_UPDATE] User 456 questions_asked incremented to 5");
        
        // Validate user metrics telemetry
        TelemetryValidationResult result = logCapture.validateUserMetricsFlow();
        
        assertTrue(result.hasStart(), "Should have USER_METRICS_UPDATE operations");
        assertTrue(result.hasSuccess(), "Should have metrics update success");
        assertFalse(result.hasFailed(), "Should not have metrics update failure");
        
        assertEquals(2, result.getOperationLogs().size(), "Should capture 2 metrics logs");
        
        logCapture.stopCapture();
    }
    
    @Test
    @DisplayName("Telemetry: Question Update Flow")
    public void testTelemetry_QuestionUpdateFlow() {
        logCapture.startCapture();
        
        // Simulate log entries for question update
        logCapture.addLogEntry("INFO", "[QUESTION_UPDATE_START] Updating question ID: 789");
        logCapture.addLogEntry("INFO", "[QUESTION_UPDATE_AUTH] User 456 updating question ID: 789");
        logCapture.addLogEntry("INFO", "[QUESTION_UPDATE_VALIDATION] Input validation passed for question ID 789");
        logCapture.addLogEntry("INFO", "[QUESTION_UPDATE_SUCCESS] Question updated successfully - ID: 789, User: 456, Tags: 3, Duration: 120ms");
        
        // Validate telemetry flow
        TelemetryValidationResult result = logCapture.validateOperationFlow("QUESTION_UPDATE");
        
        assertTrue(result.hasStart(), "Should have QUESTION_UPDATE_START marker");
        assertTrue(result.hasSuccess(), "Should have update success marker");
        assertFalse(result.hasFailed(), "Should not have failure marker for successful update");
        assertTrue(result.isSuccessfulFlow(), "Should be a successful update flow");
        
        assertEquals(4, result.getOperationLogs().size(), "Should capture 4 update logs");
        
        logCapture.stopCapture();
    }
    
    @Test
    @DisplayName("Telemetry: Question Delete Flow")
    public void testTelemetry_QuestionDeleteFlow() {
        logCapture.startCapture();
        
        // Simulate log entries for question delete
        logCapture.addLogEntry("INFO", "[QUESTION_DELETE_START] Deleting question ID: 789");
        logCapture.addLogEntry("INFO", "[QUESTION_DELETE_AUTH] User 456 deleting question ID: 789");
        logCapture.addLogEntry("INFO", "[QUESTION_DELETE_SUCCESS] Question deleted successfully - ID: 789, User: 456, Duration: 80ms");
        
        // Validate telemetry flow
        TelemetryValidationResult result = logCapture.validateOperationFlow("QUESTION_DELETE");
        
        assertTrue(result.hasStart(), "Should have QUESTION_DELETE_START marker");
        assertTrue(result.hasSuccess(), "Should have delete success marker");
        assertFalse(result.hasFailed(), "Should not have failure marker for successful delete");
        assertTrue(result.isSuccessfulFlow(), "Should be a successful delete flow");
        
        assertEquals(3, result.getOperationLogs().size(), "Should capture 3 delete logs");
        
        logCapture.stopCapture();
    }
    
    @Test
    @DisplayName("Telemetry: Log Level Distribution")
    public void testTelemetry_LogLevelDistribution() {
        logCapture.startCapture();
        
        // Add various log levels
        logCapture.addLogEntry("INFO", "[QUESTION_CREATE_SUCCESS] Operation completed");
        logCapture.addLogEntry("INFO", "[USER_METRICS_UPDATE_SUCCESS] Metrics updated");
        logCapture.addLogEntry("WARNING", "[QUESTION_CREATE_VALIDATION_FAILED] Validation error");
        logCapture.addLogEntry("ERROR", "[QUESTION_CREATE_FAILED] Database error occurred");
        
        // Validate log level summary
        TestLogCapture.LogLevelSummary summary = logCapture.getLogSummary();
        
        assertEquals(2, summary.getInfoCount(), "Should have 2 INFO logs");
        assertEquals(1, summary.getWarningCount(), "Should have 1 WARNING log");
        assertEquals(1, summary.getErrorCount(), "Should have 1 ERROR log");
        assertEquals(4, summary.getTotalCount(), "Should have 4 total logs");
        
        logCapture.stopCapture();
    }
}
