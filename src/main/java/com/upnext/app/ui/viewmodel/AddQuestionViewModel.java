package com.upnext.app.ui.viewmodel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import com.upnext.app.core.Logger;
import com.upnext.app.data.question.TagRepository;
import com.upnext.app.domain.question.Question;
import com.upnext.app.domain.question.Tag;
import com.upnext.app.service.QuestionService;

/**
 * ViewModel for the Add Question screen that manages transient state,
 * tag entry logic, and form validation. This separates business logic
 * from UI components for better testability and future undo/redo support.
 */
public class AddQuestionViewModel {
    // Constants
    private static final int MAX_TAGS = 10;
    private static final int MAX_TAG_LENGTH = 50;
    private static final int MIN_TITLE_LENGTH = 5;
    private static final int MAX_TITLE_LENGTH = 200;
    private static final int MIN_DESCRIPTION_LENGTH = 10;
    private static final int MAX_DESCRIPTION_LENGTH = 5000;
    
    // Services
    private final QuestionService questionService;
    private final TagRepository tagRepository;
    
    // State
    private String title;
    private String description;
    private String context;
    private final List<Tag> selectedTags;
    private final Set<String> normalizedTagNames; // For duplicate prevention
    
    // Event listeners
    private Consumer<List<Tag>> onTagsChanged;
    private Consumer<String> onValidationError;
    private Consumer<String> onValidationCleared;
    private Consumer<Question> onQuestionCreated;
    private Runnable onQuestionCreateFailed;
    
    /**
     * Creates a new AddQuestionViewModel.
     */
    public AddQuestionViewModel() {
        this.questionService = QuestionService.getInstance();
        this.tagRepository = TagRepository.getInstance();
        this.selectedTags = new ArrayList<>();
        this.normalizedTagNames = new HashSet<>();
        this.title = "";
        this.description = "";
        this.context = null;
    }
    
    /**
     * Sets the title with validation.
     *
     * @param title The question title
     */
    public void setTitle(String title) {
        this.title = title != null ? title.trim() : "";
    }
    
    /**
     * Gets the current title.
     *
     * @return The question title
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * Sets the description with validation.
     *
     * @param description The question description
     */
    public void setDescription(String description) {
        this.description = description != null ? description.trim() : "";
    }
    
    /**
     * Gets the current description.
     *
     * @return The question description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Sets the context (optional field).
     *
     * @param context The question context
     */
    public void setContext(String context) {
        this.context = context != null && !context.trim().isEmpty() ? context.trim() : null;
    }
    
    /**
     * Gets the current context.
     *
     * @return The question context
     */
    public String getContext() {
        return context;
    }
    
    /**
     * Attempts to add a tag with proper normalization and validation.
     *
     * @param tagName The tag name to add
     * @return True if the tag was added successfully, false otherwise
     */
    public boolean addTag(String tagName) {
        if (tagName == null || tagName.trim().isEmpty()) {
            notifyValidationError("Tag name cannot be empty.");
            return false;
        }
        
        // Normalize the tag name (lowercase, trim)
        String normalized = tagName.trim().toLowerCase();
        
        // Validate tag length
        if (normalized.length() > MAX_TAG_LENGTH) {
            Logger.getInstance().warning("[UI_TAG_VALIDATION_FAILED] Tag too long - Name: '" + normalized + "', Length: " + normalized.length() + ", Max: " + MAX_TAG_LENGTH);
            notifyValidationError("Tag name cannot exceed " + MAX_TAG_LENGTH + " characters.");
            return false;
        }
        
        // Check tag limit
        if (selectedTags.size() >= MAX_TAGS) {
            Logger.getInstance().warning("[UI_TAG_VALIDATION_FAILED] Too many tags - Current: " + selectedTags.size() + ", Max: " + MAX_TAGS);
            notifyValidationError("Cannot add more than " + MAX_TAGS + " tags.");
            return false;
        }
        
        // Check for duplicates
        if (normalizedTagNames.contains(normalized)) {
            Logger.getInstance().warning("[UI_TAG_VALIDATION_FAILED] Duplicate tag - Name: '" + normalized + "'");
            notifyValidationError("Tag '" + normalized + "' is already added.");
            return false;
        }
        
        try {
            // Try to find existing tag first
            Tag existingTag = tagRepository.findByName(normalized).orElse(null);
            
            Tag tagToAdd;
            if (existingTag != null) {
                tagToAdd = existingTag;
            } else {
                // Create new tag with normalized name
                Tag newTag = new Tag();
                newTag.setName(normalized);
                tagToAdd = tagRepository.save(newTag);
            }
            
            // Add to collections
            selectedTags.add(tagToAdd);
            normalizedTagNames.add(normalized);
            
            // Clear any validation errors
            notifyValidationCleared();
            
            // Notify listeners
            notifyTagsChanged();
            
            Logger.getInstance().info("[UI_TAG_ADD_SUCCESS] Tag added - Name: '" + normalized + "', Total tags: " + selectedTags.size());
            return true;
            
        } catch (Exception e) {
            Logger.getInstance().error("[UI_TAG_ADD_FAILED] Failed to add tag '" + normalized + "' - " + e.getMessage());
            notifyValidationError("Failed to add tag: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Removes a tag from the selected tags.
     *
     * @param tag The tag to remove
     * @return True if the tag was removed, false if it wasn't found
     */
    public boolean removeTag(Tag tag) {
        if (tag == null) {
            return false;
        }
        
        boolean removed = selectedTags.remove(tag);
        if (removed) {
            normalizedTagNames.remove(tag.getName().toLowerCase());
            notifyTagsChanged();
            Logger.getInstance().info("[UI_TAG_REMOVE_SUCCESS] Tag removed - Name: '" + tag.getName() + "', Remaining tags: " + selectedTags.size());
        } else {
            Logger.getInstance().warning("[UI_TAG_REMOVE_FAILED] Tag not found for removal: '" + tag.getName() + "'");
        }
        
        return removed;
    }
    
    /**
     * Gets a copy of the currently selected tags.
     *
     * @return List of selected tags
     */
    public List<Tag> getSelectedTags() {
        return new ArrayList<>(selectedTags);
    }
    
    /**
     * Gets the number of selected tags.
     *
     * @return The tag count
     */
    public int getTagCount() {
        return selectedTags.size();
    }
    
    /**
     * Checks if a tag name is already selected (case-insensitive).
     *
     * @param tagName The tag name to check
     * @return True if the tag is already selected
     */
    public boolean hasTag(String tagName) {
        if (tagName == null) {
            return false;
        }
        String candidate = tagName.trim();
        if (candidate.isEmpty()) {
            return false;
        }
        return normalizedTagNames.contains(candidate);
    }
    
    /**
     * Validates the current form state.
     *
     * @return Validation error message, or null if valid
     */
    public String validateForm() {
        if (title.isEmpty()) {
            return "Question title is required.";
        }
        
        if (title.length() < MIN_TITLE_LENGTH) {
            return "Title must be at least " + MIN_TITLE_LENGTH + " characters long.";
        }
        
        if (title.length() > MAX_TITLE_LENGTH) {
            return "Title cannot exceed " + MAX_TITLE_LENGTH + " characters.";
        }
        
        if (description.isEmpty()) {
            return "Question description is required.";
        }
        
        if (description.length() < MIN_DESCRIPTION_LENGTH) {
            return "Description must be at least " + MIN_DESCRIPTION_LENGTH + " characters long.";
        }
        
        if (description.length() > MAX_DESCRIPTION_LENGTH) {
            return "Description cannot exceed " + MAX_DESCRIPTION_LENGTH + " characters.";
        }
        
        if (selectedTags.isEmpty()) {
            return "At least one tag is required.";
        }
        
        return null; // No validation errors
    }
    
    /**
     * Attempts to create the question with the current form data.
     */
    public void createQuestion() {
        Logger.getInstance().info("[UI_FORM_VALIDATION_START] Validating question form");
        
        String validationError = validateForm();
        if (validationError != null) {
            Logger.getInstance().warning("[UI_FORM_VALIDATION_FAILED] Form validation failed - " + validationError);
            notifyValidationError(validationError);
            return;
        }
        
        Logger.getInstance().info("[UI_FORM_VALIDATION_SUCCESS] Form validation passed");
        
        try {
            // Convert tags to string list for service
            List<String> tagNames = new ArrayList<>();
            for (Tag tag : selectedTags) {
                tagNames.add(tag.getName());
            }
            
            Logger.getInstance().info("[UI_QUESTION_CREATE_START] Creating question - Title length: " + title.length() + 
                                   ", Description length: " + description.length() + 
                                   ", Context: " + (context != null ? "provided" : "none") + 
                                   ", Tags: " + tagNames.size());
            
            // Create question via service
            Question createdQuestion = questionService.createQuestion(title, description, context, tagNames);
            
            Logger.getInstance().info("[UI_QUESTION_CREATE_SUCCESS] Question created successfully - ID: " + createdQuestion.getId() + 
                                   ", Tags: " + (createdQuestion.getTags() != null ? createdQuestion.getTags().size() : 0));
            notifyQuestionCreated(createdQuestion);
            
        } catch (Exception e) {
            Logger.getInstance().error("[UI_QUESTION_CREATE_FAILED] Question creation failed - " + e.getMessage());
            notifyValidationError("Failed to create question: " + e.getMessage());
            notifyQuestionCreateFailed();
        }
    }
    
    /**
     * Clears all form data and selected tags.
     */
    public void clearAll() {
        title = "";
        description = "";
        context = null;
        selectedTags.clear();
        normalizedTagNames.clear();
        notifyTagsChanged();
        notifyValidationCleared();
    }
    
    /**
     * Sets the listener for tag changes.
     *
     * @param listener The listener to call when tags change
     */
    public void setOnTagsChanged(Consumer<List<Tag>> listener) {
        this.onTagsChanged = listener;
    }
    
    /**
     * Sets the listener for validation errors.
     *
     * @param listener The listener to call when validation errors occur
     */
    public void setOnValidationError(Consumer<String> listener) {
        this.onValidationError = listener;
    }
    
    /**
     * Sets the listener for validation clearing.
     *
     * @param listener The listener to call when validation is cleared
     */
    public void setOnValidationCleared(Consumer<String> listener) {
        this.onValidationCleared = listener;
    }
    
    /**
     * Sets the listener for successful question creation.
     *
     * @param listener The listener to call when question is created
     */
    public void setOnQuestionCreated(Consumer<Question> listener) {
        this.onQuestionCreated = listener;
    }
    
    /**
     * Sets the listener for failed question creation.
     *
     * @param listener The listener to call when question creation fails
     */
    public void setOnQuestionCreateFailed(Runnable listener) {
        this.onQuestionCreateFailed = listener;
    }
    
    // Private helper methods
    
    private void notifyTagsChanged() {
        if (onTagsChanged != null) {
            onTagsChanged.accept(new ArrayList<>(selectedTags));
        }
    }
    
    private void notifyValidationError(String message) {
        if (onValidationError != null) {
            onValidationError.accept(message);
        }
    }
    
    private void notifyValidationCleared() {
        if (onValidationCleared != null) {
            onValidationCleared.accept("");
        }
    }
    
    private void notifyQuestionCreated(Question question) {
        if (onQuestionCreated != null) {
            onQuestionCreated.accept(question);
        }
    }
    
    private void notifyQuestionCreateFailed() {
        if (onQuestionCreateFailed != null) {
            onQuestionCreateFailed.run();
        }
    }
}