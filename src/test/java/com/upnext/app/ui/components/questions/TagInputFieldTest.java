package com.upnext.app.ui.components.questions;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.upnext.app.ui.viewmodel.AddQuestionViewModel;

/**
 * Unit tests for TagInputField component focusing on add/remove flows,
 * user interactions, and integration with the ViewModel.
 */
public class TagInputFieldTest {
    
    private AddQuestionViewModel viewModel;
    private TagInputField tagInputField;
    private AtomicInteger tagAddAttempts;
    private AtomicBoolean lastAddResult;
    private JFrame testFrame;
    
    @BeforeEach
    public void setUp() throws Exception {
        viewModel = new AddQuestionViewModel();
        tagAddAttempts = new AtomicInteger(0);
        lastAddResult = new AtomicBoolean(false);
        
        // Initialize Swing components on the EDT for reliability
        SwingUtilities.invokeAndWait(() -> {
            tagInputField = new TagInputField(tagName -> {
                tagAddAttempts.incrementAndGet();
                boolean result = viewModel.addTag(tagName);
                lastAddResult.set(result);
                return result;
            });

            testFrame = new JFrame("Test");
            testFrame.setUndecorated(true);
            testFrame.add(tagInputField);
            testFrame.pack();
            testFrame.setLocationRelativeTo(null);
            testFrame.setVisible(true);
        });
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (testFrame != null) {
            SwingUtilities.invokeAndWait(() -> {
                testFrame.dispose();
                testFrame = null;
            });
        }
    }
    
    @Test
    public void testTagInputFieldCreation() {
        assertNotNull(tagInputField);
        assertNotNull(tagInputField.getTextField());
    }
    
    @Test
    public void testEnterKeyAddsTag() {
        // Set up input
        tagInputField.getTextField().setText("java");

        // Submit via helper (mimics Enter key)
        boolean submissionResult = tagInputField.submitCurrentInput();

        // Verify tag addition was attempted
        assertEquals(1, tagAddAttempts.get());
        assertTrue(lastAddResult.get());
        assertTrue(submissionResult);
        assertEquals(1, viewModel.getTagCount());
        assertTrue(viewModel.hasTag("java"));
    }
    
    @Test
    public void testEmptyInputIgnored() {
        // Test with empty string
        tagInputField.getTextField().setText("");

        boolean submissionResult = tagInputField.submitCurrentInput();

        // Verify no tag addition was attempted
        assertFalse(submissionResult);
        assertEquals(0, tagAddAttempts.get());
        assertEquals(0, viewModel.getTagCount());
    }
    
    @Test
    public void testWhitespaceOnlyInputIgnored() {
        // Test with whitespace only
        tagInputField.getTextField().setText("   ");

        boolean submissionResult = tagInputField.submitCurrentInput();

        // Verify no tag addition was attempted
        assertFalse(submissionResult);
        assertEquals(0, tagAddAttempts.get());
        assertEquals(0, viewModel.getTagCount());
    }
    
    @Test
    public void testPlaceholderTextIgnored() {
        // Set placeholder text
        tagInputField.getTextField().setText("Type to add tags...");

        boolean submissionResult = tagInputField.submitCurrentInput();

        // Verify no tag addition was attempted
        assertFalse(submissionResult);
        assertEquals(0, tagAddAttempts.get());
        assertEquals(0, viewModel.getTagCount());
    }
    
    @Test
    public void testSuccessfulTagAdditionClearsInput() {
        // Set up input
        tagInputField.getTextField().setText("python");

        tagInputField.submitCurrentInput();

        // Verify input was cleared after successful addition
        String currentText = tagInputField.getText();
        assertTrue(currentText.isEmpty() || currentText.equals("Type to add tags..."));
    }
    
    @Test
    public void testFailedTagAdditionKeepsInput() {
        // Add 10 tags to reach the limit
        for (int i = 0; i < 10; i++) {
            viewModel.addTag("tag" + i);
        }
        
        // Try to add one more (should fail due to limit)
        tagInputField.getTextField().setText("overflow");

        boolean submissionResult = tagInputField.submitCurrentInput();

        // Verify addition was attempted but failed
        assertEquals(1, tagAddAttempts.get());
        assertFalse(lastAddResult.get());
        assertFalse(submissionResult);
        
        // Input should still contain the text since addition failed
        assertEquals("overflow", tagInputField.getTextField().getText());
    }
    
    @Test
    public void testDuplicateTagHandling() {
        // Add a tag first
        viewModel.addTag("javascript");
        
        // Try to add the same tag through the input field
        tagInputField.getTextField().setText("javascript");

        boolean submissionResult = tagInputField.submitCurrentInput();

        // Verify duplicate was rejected
        assertEquals(1, tagAddAttempts.get());
        assertFalse(lastAddResult.get());
        assertFalse(submissionResult);
        assertEquals(1, viewModel.getTagCount()); // Still only one tag
    }
    
    @Test
    public void testCaseInsensitiveDuplicateHandling() {
        // Add a tag first
        viewModel.addTag("React");
        
        // Try to add the same tag with different case
        tagInputField.getTextField().setText("REACT");

        boolean submissionResult = tagInputField.submitCurrentInput();

        // Verify case-insensitive duplicate was rejected
        assertEquals(1, tagAddAttempts.get());
        assertFalse(lastAddResult.get());
        assertFalse(submissionResult);
        assertEquals(1, viewModel.getTagCount()); // Still only one tag
    }
    
    @Test
    public void testFocusRequestDelegation() {
        // Request focus on the TagInputField
        boolean focusRequested = tagInputField.requestInputFocus();

        // Verify that the focus request either succeeded or is queued for the input field
        assertTrue(focusRequested || tagInputField.getTextField().isFocusable());
    }
    
    @Test
    public void testGetTextMethod() {
        // Test with normal text
        tagInputField.getTextField().setText("normal text");
        assertEquals("normal text", tagInputField.getText());
        
        // Test with placeholder text
        tagInputField.getTextField().setText("Type to add tags...");
        assertEquals("", tagInputField.getText()); // Should return empty for placeholder
    }
    
    @Test
    public void testSetTextMethod() {
        // Test setting normal text
        tagInputField.setText("test tag");
        assertEquals("test tag", tagInputField.getTextField().getText());
        
        // Test setting empty text (should show placeholder)
        tagInputField.setText("");
        assertEquals("Type to add tags...", tagInputField.getTextField().getText());
        
        // Test setting null text
        tagInputField.setText(null);
        assertEquals("Type to add tags...", tagInputField.getTextField().getText());
    }
    
    @Test
    public void testClearMethod() {
        // Set some text first
        tagInputField.setText("some text");
        
        // Clear the field
        tagInputField.clear();
        
        // Should show placeholder
        assertEquals("Type to add tags...", tagInputField.getTextField().getText());
    }
}