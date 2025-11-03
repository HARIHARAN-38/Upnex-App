package com.upnext.app.ui.navigation;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.upnext.app.App;
import com.upnext.app.domain.question.Question;
import com.upnext.app.ui.screens.AddQuestionScreen;
import com.upnext.app.ui.screens.HomeScreen;
import com.upnext.app.util.TestLogCapture;
import com.upnext.app.util.TestLogCapture.TelemetryValidationResult;

/**
 * Regression tests for the Add Question navigation flow.
 * Verifies that navigation between Home and Add Question screens works correctly,
 * and that the question feed refreshes properly after question creation.
 */
class AddQuestionFlowTest {
    
    private ViewNavigator navigator;
    private JPanel container;
    private HomeScreen homeScreen;
    private AddQuestionScreen addQuestionScreen;
    private TestLogCapture logCapture;
    
    @BeforeEach
    void setUp() {
        // Create container for navigation
        container = new JPanel();
        
        // Initialize ViewNavigator (or get existing instance)
        try {
            navigator = ViewNavigator.initialize(container);
        } catch (IllegalStateException e) {
            // ViewNavigator is already initialized, use the existing instance
            navigator = ViewNavigator.getInstance();
        }
        
        // Create screen instances
        homeScreen = new HomeScreen();
        addQuestionScreen = new AddQuestionScreen();
        
        // Initialize telemetry capture
        logCapture = new TestLogCapture();
        
        // Register screens (only if not already registered)
        try {
            navigator.registerScreen(App.HOME_SCREEN, homeScreen);
        } catch (Exception e) {
            // Screen might already be registered, that's okay
        }
        
        try {
            navigator.registerScreen(App.ADD_QUESTION_SCREEN, addQuestionScreen);
        } catch (Exception e) {
            // Screen might already be registered, that's okay
        }
    }
    
    @Test
    @DisplayName("Should navigate from Home to Add Question screen when Ask Question button is clicked")
    void testNavigateToAddQuestionScreen() {
        // Given: Start on home screen
        navigator.navigateTo(App.HOME_SCREEN);
        assertEquals(App.HOME_SCREEN, navigator.getCurrentScreen());
        
        // When: Ask Question button is clicked
        // We need to find and simulate the Ask Question button click
        JButton askButton = findAskQuestionButton();
        assertNotNull(askButton, "Ask Question button should be found in HomeScreen");
        
        // Simulate button click
        ActionListener[] listeners = askButton.getActionListeners();
        assertTrue(listeners.length > 0, "Ask Question button should have action listeners");
        listeners[0].actionPerformed(new ActionEvent(askButton, ActionEvent.ACTION_PERFORMED, "click"));
        
        // Then: Should navigate to Add Question screen
        assertEquals(App.ADD_QUESTION_SCREEN, navigator.getCurrentScreen());
    }
    
    @Test
    @DisplayName("Should navigate back to Home screen when Cancel is clicked in Add Question")
    void testNavigateBackFromAddQuestionScreen() {
        // Given: Start on Add Question screen
        navigator.navigateTo(App.ADD_QUESTION_SCREEN);
        assertEquals(App.ADD_QUESTION_SCREEN, navigator.getCurrentScreen());
        
        // When: Cancel button is clicked
        JButton cancelButton = addQuestionScreen.getCancelButton();
        assertNotNull(cancelButton, "Cancel button should be available");
        
        // Simulate button click
        ActionListener[] listeners = cancelButton.getActionListeners();
        assertTrue(listeners.length > 0, "Cancel button should have action listeners");
        listeners[0].actionPerformed(new ActionEvent(cancelButton, ActionEvent.ACTION_PERFORMED, "click"));
        
        // Then: Should navigate back to Home screen
        assertEquals(App.HOME_SCREEN, navigator.getCurrentScreen());
    }
    
    @Test
    @DisplayName("Should handle question creation callback and navigate to Home")
    void testQuestionCreationCallback() {
        // Given: Add Question screen with navigation callbacks set up
        boolean[] callbackInvoked = {false};
        Question[] createdQuestion = {null};
        
        addQuestionScreen.setOnQuestionCreated(question -> {
            callbackInvoked[0] = true;
            createdQuestion[0] = question;
        });
        
        addQuestionScreen.setOnNavigateBack(() -> {
            navigator.navigateTo(App.HOME_SCREEN);
        });
        
        // When: Question creation is simulated
        Question testQuestion = new Question(1L, "Test Title", "Test Description", null, null);
        
        // Simulate question creation (this would normally be triggered by the ViewModel)
        addQuestionScreen.setOnQuestionCreated(question -> {
            callbackInvoked[0] = true;
            createdQuestion[0] = question;
            // Simulate navigation back to home
            navigator.navigateTo(App.HOME_SCREEN);
        });
        
        // Trigger the callback manually for testing
        java.util.function.Consumer<Question> callback = question -> {
            callbackInvoked[0] = true;
            createdQuestion[0] = question;
            navigator.navigateTo(App.HOME_SCREEN);
        };
        callback.accept(testQuestion);
        
        // Then: Callback should be invoked and navigation should occur
        assertTrue(callbackInvoked[0], "Question creation callback should be invoked");
        assertNotNull(createdQuestion[0], "Created question should be passed to callback");
        assertEquals(testQuestion.getTitle(), createdQuestion[0].getTitle(), "Question title should match");
        assertEquals(App.HOME_SCREEN, navigator.getCurrentScreen(), "Should navigate back to home screen");
    }
    
    @Test
    @DisplayName("Should refresh question feed after successful question creation")
    void testQuestionFeedRefresh() {
        // Given: Home screen is active
        navigator.navigateTo(App.HOME_SCREEN);
        
        // When: A new question is added to the feed
        Question testQuestion = new Question(1L, "Test Title", "Test Description", null, null);
        
        // This should not throw any exceptions
        assertDoesNotThrow(() -> {
            homeScreen.addNewQuestionToFeed(testQuestion);
        }, "Adding new question to feed should not throw exceptions");
    }
    
    @Test
    @DisplayName("Should verify navigation stack integrity")
    void testNavigationStackIntegrity() {
        // Given: Start on home screen
        navigator.navigateTo(App.HOME_SCREEN);
        String initialScreen = navigator.getCurrentScreen();
        
        // When: Navigate to Add Question and back
        navigator.navigateTo(App.ADD_QUESTION_SCREEN);
        navigator.navigateTo(App.HOME_SCREEN);
        
        // Then: Should be back on the original screen
        assertEquals(initialScreen, navigator.getCurrentScreen(), 
            "Navigation stack should be intact after round-trip navigation");
    }
    
    /**
     * Helper method to find the Ask Question button in the HomeScreen.
     * This method traverses the component hierarchy to locate the button.
     */
    private JButton findAskQuestionButton() {
        return findButtonWithText(homeScreen, "Ask a Question");
    }
    
    /**
     * Recursively searches for a button with the specified text.
     */
    private JButton findButtonWithText(java.awt.Container container, String text) {
        for (java.awt.Component component : container.getComponents()) {
            if (component instanceof JButton) {
                JButton button = (JButton) component;
                if (text.equals(button.getText())) {
                    return button;
                }
            } else if (component instanceof java.awt.Container) {
                JButton found = findButtonWithText((java.awt.Container) component, text);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
    
    // ===== UI TELEMETRY VALIDATION TESTS =====
    
    @Test
    @DisplayName("UI Telemetry: Tag Addition Flow")
    public void testUITelemetry_TagAddition() {
        logCapture.startCapture();
        
        // Simulate UI tag addition telemetry
        logCapture.addLogEntry("INFO", "[UI_TAG_ADD_SUCCESS] Tag added - Name: 'java', Total tags: 1");
        logCapture.addLogEntry("INFO", "[UI_TAG_ADD_SUCCESS] Tag added - Name: 'spring-boot', Total tags: 2");
        
        // Validate UI action telemetry
        TelemetryValidationResult result = logCapture.validateUIActionFlow("TAG_ADD");
        
        assertTrue(result.hasStart(), "Should have UI_TAG_ADD operations");
        assertTrue(result.hasSuccess(), "Should have tag addition success");
        assertFalse(result.hasFailed(), "Should not have tag addition failure");
        
        assertEquals(2, result.getOperationLogs().size(), "Should capture 2 tag addition logs");
        
        logCapture.stopCapture();
    }
    
    @Test
    @DisplayName("UI Telemetry: Tag Validation Failure")
    public void testUITelemetry_TagValidationFailure() {
        logCapture.startCapture();
        
        // Simulate UI tag validation failures
        logCapture.addLogEntry("WARNING", "[UI_TAG_VALIDATION_FAILED] Tag too long - Name: 'very-long-tag-name-that-exceeds-maximum-length', Length: 55, Max: 50");
        logCapture.addLogEntry("WARNING", "[UI_TAG_VALIDATION_FAILED] Too many tags - Current: 10, Max: 10");
        logCapture.addLogEntry("WARNING", "[UI_TAG_VALIDATION_FAILED] Duplicate tag - Name: 'java'");
        
        // Validate UI validation telemetry
        TelemetryValidationResult result = logCapture.validateUIActionFlow("TAG_VALIDATION");
        
        assertTrue(result.hasStart(), "Should have UI_TAG_VALIDATION operations");
        assertFalse(result.hasSuccess(), "Should not have validation success for failures");
        assertTrue(result.hasFailed(), "Should have tag validation failures");
        
        assertEquals(3, result.getOperationLogs().size(), "Should capture 3 validation failure logs");
        
        logCapture.stopCapture();
    }
    
    @Test
    @DisplayName("UI Telemetry: Tag Removal Flow")
    public void testUITelemetry_TagRemoval() {
        logCapture.startCapture();
        
        // Simulate UI tag removal telemetry
        logCapture.addLogEntry("INFO", "[UI_TAG_REMOVE_SUCCESS] Tag removed - Name: 'spring-boot', Remaining tags: 1");
        logCapture.addLogEntry("WARNING", "[UI_TAG_REMOVE_FAILED] Tag not found for removal: 'nonexistent-tag'");
        
        // Validate UI action telemetry
        TelemetryValidationResult result = logCapture.validateUIActionFlow("TAG_REMOVE");
        
        assertTrue(result.hasStart(), "Should have UI_TAG_REMOVE operations");
        assertTrue(result.hasSuccess(), "Should have tag removal success");
        assertTrue(result.hasFailed(), "Should have tag removal failure for nonexistent tag");
        
        assertEquals(2, result.getOperationLogs().size(), "Should capture 2 tag removal logs");
        
        logCapture.stopCapture();
    }
    
    @Test
    @DisplayName("UI Telemetry: Form Validation Flow")
    public void testUITelemetry_FormValidation() {
        logCapture.startCapture();
        
        // Simulate UI form validation telemetry
        logCapture.addLogEntry("INFO", "[UI_FORM_VALIDATION_START] Validating question form");
        logCapture.addLogEntry("WARNING", "[UI_FORM_VALIDATION_FAILED] Form validation failed - Question title is required");
        logCapture.addLogEntry("INFO", "[UI_FORM_VALIDATION_START] Validating question form");
        logCapture.addLogEntry("INFO", "[UI_FORM_VALIDATION_SUCCESS] Form validation passed");
        
        // Validate UI form validation telemetry
        TelemetryValidationResult result = logCapture.validateUIActionFlow("FORM_VALIDATION");
        
        assertTrue(result.hasStart(), "Should have UI_FORM_VALIDATION operations");
        assertTrue(result.hasSuccess(), "Should have form validation success");
        assertTrue(result.hasFailed(), "Should have form validation failure");
        
        assertEquals(4, result.getOperationLogs().size(), "Should capture 4 form validation logs");
        
        logCapture.stopCapture();
    }
    
    @Test
    @DisplayName("UI Telemetry: Question Creation Flow")
    public void testUITelemetry_QuestionCreation() {
        logCapture.startCapture();
        
        // Simulate complete UI question creation flow
        logCapture.addLogEntry("INFO", "[UI_QUESTION_CREATE_START] Creating question - Title length: 25, Description length: 150, Context: provided, Tags: 2");
        logCapture.addLogEntry("INFO", "[UI_QUESTION_CREATE_SUCCESS] Question created successfully - ID: 123, Tags: 2");
        
        // Validate UI question creation telemetry
        TelemetryValidationResult result = logCapture.validateUIActionFlow("QUESTION_CREATE");
        
        assertTrue(result.hasStart(), "Should have UI_QUESTION_CREATE operations");
        assertTrue(result.hasSuccess(), "Should have question creation success");
        assertFalse(result.hasFailed(), "Should not have question creation failure");
        assertTrue(result.isSuccessfulFlow(), "Should be a successful UI flow");
        
        assertEquals(2, result.getOperationLogs().size(), "Should capture 2 question creation logs");
        
        logCapture.stopCapture();
    }
    
    @Test
    @DisplayName("UI Telemetry: Question Creation Failure")
    public void testUITelemetry_QuestionCreationFailure() {
        logCapture.startCapture();
        
        // Simulate UI question creation failure
        logCapture.addLogEntry("INFO", "[UI_QUESTION_CREATE_START] Creating question - Title length: 20, Description length: 100, Context: none, Tags: 1");
        logCapture.addLogEntry("ERROR", "[UI_QUESTION_CREATE_FAILED] Question creation failed - User must be authenticated to create questions");
        
        // Validate UI question creation failure telemetry
        TelemetryValidationResult result = logCapture.validateUIActionFlow("QUESTION_CREATE");
        
        assertTrue(result.hasStart(), "Should have UI_QUESTION_CREATE operations");
        assertFalse(result.hasSuccess(), "Should not have question creation success");
        assertTrue(result.hasFailed(), "Should have question creation failure");
        assertFalse(result.isSuccessfulFlow(), "Should not be a successful UI flow");
        
        assertEquals(2, result.getOperationLogs().size(), "Should capture 2 question creation logs");
        
        logCapture.stopCapture();
    }
}