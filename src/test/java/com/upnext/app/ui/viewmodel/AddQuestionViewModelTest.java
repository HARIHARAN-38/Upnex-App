package com.upnext.app.ui.viewmodel;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.upnext.app.domain.question.Tag;

/**
 * Unit tests for AddQuestionViewModel focusing on tag entry logic,
 * validation, and state management.
 */
public class AddQuestionViewModelTest {
    
    private AddQuestionViewModel viewModel;
    private AtomicReference<String> lastValidationError;
    private AtomicReference<List<Tag>> lastTagsChange;
    
    @BeforeEach
    public void setUp() {
        viewModel = new AddQuestionViewModel();
        lastValidationError = new AtomicReference<>();
        lastTagsChange = new AtomicReference<>();
        
        // Set up listeners
        viewModel.setOnValidationError(lastValidationError::set);
        viewModel.setOnTagsChanged(lastTagsChange::set);
    }
    
    @Test
    public void testAddTagWithValidInput() {
        boolean result = viewModel.addTag("Java");
        
        assertTrue(result);
        assertEquals(1, viewModel.getTagCount());
        assertTrue(viewModel.hasTag("java")); // Should be normalized to lowercase
        assertNotNull(lastTagsChange.get());
        assertEquals(1, lastTagsChange.get().size());
    }
    
    @Test
    public void testAddTagWithNormalization() {
        boolean result = viewModel.addTag("  JavaScript  ");
        
        assertTrue(result);
        assertEquals(1, viewModel.getTagCount());
        assertTrue(viewModel.hasTag("javascript")); // Should be normalized
        assertFalse(viewModel.hasTag("JavaScript")); // Original case should not match
        assertFalse(viewModel.hasTag("  JavaScript  ")); // With spaces should not match
    }
    
    @Test
    public void testAddDuplicateTag() {
        // Add first tag
        viewModel.addTag("Python");
        
        // Try to add duplicate
        boolean result = viewModel.addTag("python");
        
        assertFalse(result);
        assertEquals(1, viewModel.getTagCount()); // Should still be 1
        assertNotNull(lastValidationError.get());
        assertTrue(lastValidationError.get().contains("already added"));
    }
    
    @Test
    public void testAddTagWithCaseDuplicates() {
        // Add first tag
        viewModel.addTag("React");
        
        // Try various case combinations
        assertFalse(viewModel.addTag("REACT"));
        assertFalse(viewModel.addTag("react"));
        assertFalse(viewModel.addTag("ReAcT"));
        
        assertEquals(1, viewModel.getTagCount());
    }
    
    @Test
    public void testAddEmptyTag() {
        boolean result = viewModel.addTag("");
        
        assertFalse(result);
        assertEquals(0, viewModel.getTagCount());
        assertNotNull(lastValidationError.get());
        assertTrue(lastValidationError.get().contains("cannot be empty"));
    }
    
    @Test
    public void testAddNullTag() {
        boolean result = viewModel.addTag(null);
        
        assertFalse(result);
        assertEquals(0, viewModel.getTagCount());
        assertNotNull(lastValidationError.get());
        assertTrue(lastValidationError.get().contains("cannot be empty"));
    }
    
    @Test
    public void testAddWhitespaceOnlyTag() {
        boolean result = viewModel.addTag("   ");
        
        assertFalse(result);
        assertEquals(0, viewModel.getTagCount());
        assertNotNull(lastValidationError.get());
        assertTrue(lastValidationError.get().contains("cannot be empty"));
    }
    
    @Test
    public void testTagLengthLimit() {
        String longTag = "a".repeat(51); // Exceeds 50 character limit
        
        boolean result = viewModel.addTag(longTag);
        
        assertFalse(result);
        assertEquals(0, viewModel.getTagCount());
        assertNotNull(lastValidationError.get());
        assertTrue(lastValidationError.get().contains("cannot exceed"));
    }
    
    @Test
    public void testTagCountLimit() {
        // Add maximum number of tags (10)
        for (int i = 0; i < 10; i++) {
            boolean result = viewModel.addTag("tag" + i);
            assertTrue(result);
        }
        
        // Try to add one more
        boolean result = viewModel.addTag("overflow");
        
        assertFalse(result);
        assertEquals(10, viewModel.getTagCount());
        assertNotNull(lastValidationError.get());
        assertTrue(lastValidationError.get().contains("Cannot add more than"));
    }
    
    @Test
    public void testRemoveTag() {
        // Add a tag first
        viewModel.addTag("TypeScript");
        List<Tag> tags = viewModel.getSelectedTags();
        Tag tagToRemove = tags.get(0);
        
        // Remove the tag
        boolean result = viewModel.removeTag(tagToRemove);
        
        assertTrue(result);
        assertEquals(0, viewModel.getTagCount());
        assertFalse(viewModel.hasTag("typescript"));
        assertNotNull(lastTagsChange.get());
        assertEquals(0, lastTagsChange.get().size());
    }
    
    @Test
    public void testRemoveNonExistentTag() {
        Tag nonExistentTag = new Tag();
        nonExistentTag.setId(999L);
        nonExistentTag.setName("nonexistent");
        
        boolean result = viewModel.removeTag(nonExistentTag);
        
        assertFalse(result);
        assertEquals(0, viewModel.getTagCount());
    }
    
    @Test
    public void testRemoveNullTag() {
        boolean result = viewModel.removeTag(null);
        
        assertFalse(result);
    }
    
    @Test
    public void testFormValidation() {
        // Test empty title
        String error = viewModel.validateForm();
        assertNotNull(error);
        assertTrue(error.contains("title is required"));
        
        // Set title
        viewModel.setTitle("Valid Title");
        
        // Test empty description
        error = viewModel.validateForm();
        assertNotNull(error);
        assertTrue(error.contains("description is required"));
        
        // Set description
        viewModel.setDescription("Valid description with enough characters");
        
        // Test missing tags
        error = viewModel.validateForm();
        assertNotNull(error);
        assertTrue(error.contains("At least one tag"));
        
        // Add tag
        viewModel.addTag("java");
        
        // Now should be valid
        error = viewModel.validateForm();
        assertNull(error);
    }
    
    @Test
    public void testTitleValidation() {
        // Too short title
        viewModel.setTitle("Hi");
        String error = viewModel.validateForm();
        assertNotNull(error);
        assertTrue(error.contains("at least 5 characters"));
        
        // Too long title
        viewModel.setTitle("a".repeat(201));
        error = viewModel.validateForm();
        assertNotNull(error);
        assertTrue(error.contains("cannot exceed 200 characters"));
    }
    
    @Test
    public void testDescriptionValidation() {
        viewModel.setTitle("Valid Title");
        
        // Too short description
        viewModel.setDescription("Short");
        String error = viewModel.validateForm();
        assertNotNull(error);
        assertTrue(error.contains("at least 10 characters"));
        
        // Too long description
        viewModel.setDescription("a".repeat(5001));
        error = viewModel.validateForm();
        assertNotNull(error);
        assertTrue(error.contains("cannot exceed 5000 characters"));
    }
    
    @Test
    public void testClearAll() {
        // Set up some data
        viewModel.setTitle("Test Title");
        viewModel.setDescription("Test Description");
        viewModel.setContext("Test Context");
        viewModel.addTag("tag1");
        viewModel.addTag("tag2");
        
        // Clear all
        viewModel.clearAll();
        
        // Verify everything is cleared
        assertEquals("", viewModel.getTitle());
        assertEquals("", viewModel.getDescription());
        assertNull(viewModel.getContext());
        assertEquals(0, viewModel.getTagCount());
        assertNotNull(lastTagsChange.get());
        assertEquals(0, lastTagsChange.get().size());
    }
    
    @Test
    public void testContextHandling() {
        // Test setting context
        viewModel.setContext("Some context");
        assertEquals("Some context", viewModel.getContext());
        
        // Test setting empty context
        viewModel.setContext("");
        assertNull(viewModel.getContext());
        
        // Test setting null context
        viewModel.setContext(null);
        assertNull(viewModel.getContext());
        
        // Test setting whitespace-only context
        viewModel.setContext("   ");
        assertNull(viewModel.getContext());
    }
    
    @Test
    public void testGetSelectedTagsReturnsImmutableCopy() {
        viewModel.addTag("tag1");
        viewModel.addTag("tag2");
        
        List<Tag> tags = viewModel.getSelectedTags();
        assertEquals(2, tags.size());
        
        // Modifying the returned list should not affect the internal state
        tags.clear();
        assertEquals(2, viewModel.getTagCount()); // Internal state should be unchanged
    }
}