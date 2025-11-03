package com.upnext.app.ui.components.questions;

import java.awt.event.KeyEvent;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JFrame;

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
    
    @BeforeEach
    public void setUp() {
        viewModel = new AddQuestionViewModel();
        tagAddAttempts = new AtomicInteger(0);
        lastAddResult = new AtomicBoolean(false);
        
        // Create TagInputField with callback that tracks attempts and delegates to ViewModel
        tagInputField = new TagInputField(tagName -> {
            tagAddAttempts.incrementAndGet();
            boolean result = viewModel.addTag(tagName);
            lastAddResult.set(result);
            return result;
        });
        
        // Create a frame to make the component displayable for testing
        JFrame testFrame = new JFrame("Test");
        testFrame.add(tagInputField);
        testFrame.pack();
        testFrame.setVisible(false); // Don't actually show the window
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
        
        // Simulate Enter key press
        KeyEvent enterEvent = new KeyEvent(
            tagInputField.getTextField(), 
            KeyEvent.KEY_PRESSED, 
            System.currentTimeMillis(), 
            0, 
            KeyEvent.VK_ENTER, 
            KeyEvent.CHAR_UNDEFINED
        );
        
        // Dispatch the event
        tagInputField.getTextField().dispatchEvent(enterEvent);
        
        // Verify tag addition was attempted
        assertEquals(1, tagAddAttempts.get());
        assertTrue(lastAddResult.get());
        assertEquals(1, viewModel.getTagCount());
        assertTrue(viewModel.hasTag("java"));
    }
    
    @Test
    public void testEmptyInputIgnored() {
        // Test with empty string
        tagInputField.getTextField().setText("");
        
        KeyEvent enterEvent = new KeyEvent(
            tagInputField.getTextField(), 
            KeyEvent.KEY_PRESSED, 
            System.currentTimeMillis(), 
            0, 
            KeyEvent.VK_ENTER, 
            KeyEvent.CHAR_UNDEFINED
        );
        
        tagInputField.getTextField().dispatchEvent(enterEvent);
        
        // Verify no tag addition was attempted
        assertEquals(0, tagAddAttempts.get());
        assertEquals(0, viewModel.getTagCount());
    }
    
    @Test
    public void testWhitespaceOnlyInputIgnored() {
        // Test with whitespace only
        tagInputField.getTextField().setText("   ");
        
        KeyEvent enterEvent = new KeyEvent(
            tagInputField.getTextField(), 
            KeyEvent.KEY_PRESSED, 
            System.currentTimeMillis(), 
            0, 
            KeyEvent.VK_ENTER, 
            KeyEvent.CHAR_UNDEFINED
        );
        
        tagInputField.getTextField().dispatchEvent(enterEvent);
        
        // Verify no tag addition was attempted
        assertEquals(0, tagAddAttempts.get());
        assertEquals(0, viewModel.getTagCount());
    }
    
    @Test
    public void testPlaceholderTextIgnored() {
        // Set placeholder text
        tagInputField.getTextField().setText("Type to add tags...");
        
        KeyEvent enterEvent = new KeyEvent(
            tagInputField.getTextField(), 
            KeyEvent.KEY_PRESSED, 
            System.currentTimeMillis(), 
            0, 
            KeyEvent.VK_ENTER, 
            KeyEvent.CHAR_UNDEFINED
        );
        
        tagInputField.getTextField().dispatchEvent(enterEvent);
        
        // Verify no tag addition was attempted
        assertEquals(0, tagAddAttempts.get());
        assertEquals(0, viewModel.getTagCount());
    }
    
    @Test
    public void testSuccessfulTagAdditionClearsInput() {
        // Set up input
        tagInputField.getTextField().setText("python");
        
        // Simulate Enter key press
        KeyEvent enterEvent = new KeyEvent(
            tagInputField.getTextField(), 
            KeyEvent.KEY_PRESSED, 
            System.currentTimeMillis(), 
            0, 
            KeyEvent.VK_ENTER, 
            KeyEvent.CHAR_UNDEFINED
        );
        
        tagInputField.getTextField().dispatchEvent(enterEvent);
        
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
        
        KeyEvent enterEvent = new KeyEvent(
            tagInputField.getTextField(), 
            KeyEvent.KEY_PRESSED, 
            System.currentTimeMillis(), 
            0, 
            KeyEvent.VK_ENTER, 
            KeyEvent.CHAR_UNDEFINED
        );
        
        tagInputField.getTextField().dispatchEvent(enterEvent);
        
        // Verify addition was attempted but failed
        assertEquals(1, tagAddAttempts.get());
        assertFalse(lastAddResult.get());
        
        // Input should still contain the text since addition failed
        assertEquals("overflow", tagInputField.getTextField().getText());
    }
    
    @Test
    public void testDuplicateTagHandling() {
        // Add a tag first
        viewModel.addTag("javascript");
        
        // Try to add the same tag through the input field
        tagInputField.getTextField().setText("javascript");
        
        KeyEvent enterEvent = new KeyEvent(
            tagInputField.getTextField(), 
            KeyEvent.KEY_PRESSED, 
            System.currentTimeMillis(), 
            0, 
            KeyEvent.VK_ENTER, 
            KeyEvent.CHAR_UNDEFINED
        );
        
        tagInputField.getTextField().dispatchEvent(enterEvent);
        
        // Verify duplicate was rejected
        assertEquals(1, tagAddAttempts.get());
        assertFalse(lastAddResult.get());
        assertEquals(1, viewModel.getTagCount()); // Still only one tag
    }
    
    @Test
    public void testCaseInsensitiveDuplicateHandling() {
        // Add a tag first
        viewModel.addTag("React");
        
        // Try to add the same tag with different case
        tagInputField.getTextField().setText("REACT");
        
        KeyEvent enterEvent = new KeyEvent(
            tagInputField.getTextField(), 
            KeyEvent.KEY_PRESSED, 
            System.currentTimeMillis(), 
            0, 
            KeyEvent.VK_ENTER, 
            KeyEvent.CHAR_UNDEFINED
        );
        
        tagInputField.getTextField().dispatchEvent(enterEvent);
        
        // Verify case-insensitive duplicate was rejected
        assertEquals(1, tagAddAttempts.get());
        assertFalse(lastAddResult.get());
        assertEquals(1, viewModel.getTagCount()); // Still only one tag
    }
    
    @Test
    public void testFocusRequestDelegation() {
        // Request focus on the TagInputField
        tagInputField.requestFocus();
        
        // Verify that the underlying text field receives focus
        assertTrue(tagInputField.getTextField().hasFocus() || 
                  tagInputField.getTextField().isFocusOwner());
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