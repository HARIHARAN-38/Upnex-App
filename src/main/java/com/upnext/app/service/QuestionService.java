package com.upnext.app.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.upnext.app.core.Logger;
import com.upnext.app.data.UserRepository;
import com.upnext.app.data.question.QuestionRepository;
import com.upnext.app.domain.User;
import com.upnext.app.domain.question.Question;

/**
 * Service for question-related operations.
 * Handles validation, business logic, and coordination with repositories.
 */
public final class QuestionService {
    private static final Logger LOGGER = Logger.getInstance();
    private static final QuestionService INSTANCE = new QuestionService();
    
    // Dependencies
    private final QuestionRepository questionRepository;
    private final AuthService authService;
    private final UserRepository userRepository;
    
    // Business rules - constants for validation
    private static final int MIN_TITLE_LENGTH = 5;
    private static final int MAX_TITLE_LENGTH = 200;
    private static final int MIN_CONTENT_LENGTH = 10;
    private static final int MAX_CONTENT_LENGTH = 5000;
    private static final int MAX_CONTEXT_LENGTH = 1000;
    private static final int MAX_TAGS_PER_QUESTION = 10;
    private static final int MAX_TAG_LENGTH = 50;
    
    /**
     * Private constructor to enforce singleton pattern.
     */
    private QuestionService() {
        this.questionRepository = QuestionRepository.getInstance();
        this.authService = AuthService.getInstance();
        this.userRepository = UserRepository.getInstance();
    }
    
    /**
     * Gets the singleton instance of the service.
     * 
     * @return The service instance
     */
    public static QuestionService getInstance() {
        return INSTANCE;
    }
    
    /**
     * Creates a new question with tags after comprehensive validation.
     * 
     * @param title The question title (required, 5-200 characters)
     * @param content The question content (required, 10-5000 characters) 
     * @param context Optional context information (max 1000 characters)
     * @param tags List of tags (optional, max 10 tags, each max 50 characters)
     * @return The created question with generated ID and timestamps
     * @throws QuestionException If validation fails or user not authenticated
     * @throws SQLException If database operation fails
     */
    public Question createQuestion(String title, String content, String context, List<String> tags) 
            throws QuestionException, SQLException {
        return createQuestion(title, content, context, tags, null);
    }
    
    /**
     * Creates a new question with tags and subject after comprehensive validation.
     * 
     * @param title The question title (required, 5-200 characters)
     * @param content The question content (required, 10-5000 characters) 
     * @param context Optional context information (max 1000 characters)
     * @param tags List of tags (optional, max 10 tags, each max 50 characters)
     * @param subjectId The ID of the subject category (optional, defaults to General if null)
     * @return The created question with generated ID and timestamps
     * @throws QuestionException If validation fails or user not authenticated
     * @throws SQLException If database operation fails
     */
    public Question createQuestion(String title, String content, String context, List<String> tags, Long subjectId) 
            throws QuestionException, SQLException {
        
        long startTime = System.currentTimeMillis();
        String titlePreview = title != null ? title.substring(0, Math.min(title.length(), 30)) + "..." : "null";
        
        LOGGER.info("[QUESTION_CREATE_START] User initiating question creation - Title: " + titlePreview + 
                   ", Tags count: " + (tags != null ? tags.size() : 0));
        
        // Get current authenticated user
        Optional<Long> currentUserId = getCurrentUserId();
        if (!currentUserId.isPresent()) {
            LOGGER.warning("[QUESTION_CREATE_FAILED] Authentication failure - No authenticated user for question creation");
            throw new QuestionException("User must be authenticated to create questions");
        }
        
        Long userId = currentUserId.get();
        LOGGER.info("[QUESTION_CREATE_AUTH] Authenticated user ID: " + userId + " creating question");
        
        // Validate input parameters with detailed logging
        try {
            validateQuestionInput(title, content, context, tags);
            LOGGER.info("[QUESTION_CREATE_VALIDATION] Input validation passed for user " + userId);
        } catch (QuestionException e) {
            LOGGER.warning("[QUESTION_CREATE_VALIDATION_FAILED] User " + userId + " - " + e.getMessage());
            throw e;
        }
        
        // Log tag analytics for insights
        if (tags != null && !tags.isEmpty()) {
            LOGGER.info("[QUESTION_TAG_ANALYTICS] User " + userId + " using tags: [" + String.join(", ", tags) + "]");
        }
        
        // Create question object (validation ensures title and content are non-null)
        // Defensive null checks to satisfy compiler analysis
        String safeTitle = (title != null) ? title.trim() : "";
        String safeContent = (content != null) ? content.trim() : "";
        String safeContext = (context != null) ? context.trim() : null;
        
        // Use provided subject ID or default to General category (assuming it's the last one added)
        Long finalSubjectId = subjectId != null ? subjectId : 21L; // General subject from our migration
        
        Question question = new Question(
            userId,
            safeTitle,
            safeContent,
            safeContext,
            finalSubjectId
        );
        
        try {
            // Use repository's transactional saveWithTags method
            Question savedQuestion = questionRepository.saveWithTags(question, tags);
            
            // Update user metrics - increment questions_asked counter
            try {
                updateUserQuestionMetrics(userId);
                LOGGER.info("[USER_METRICS_UPDATE_SUCCESS] Incremented questions_asked for user " + userId);
            } catch (Exception e) {
                // Log the error but don't fail the question creation
                LOGGER.warning("[USER_METRICS_UPDATE_FAILED] Failed to update metrics for user " + userId + " - " + e.getMessage());
            }
            
            long duration = System.currentTimeMillis() - startTime;
            LOGGER.info("[QUESTION_CREATE_SUCCESS] Question created successfully - ID: " + savedQuestion.getId() + 
                       ", User: " + userId + ", Tags: " + (savedQuestion.getTags() != null ? savedQuestion.getTags().size() : 0) + 
                       ", Duration: " + duration + "ms");
            
            return savedQuestion;
            
        } catch (IllegalArgumentException e) {
            LOGGER.error("[QUESTION_CREATE_FAILED] Repository validation failed for user " + userId + " - " + e.getMessage());
            throw new QuestionException("Question creation failed: " + e.getMessage(), e);
        } catch (SQLException e) {
            LOGGER.error("[QUESTION_CREATE_FAILED] Database error for user " + userId + " - " + e.getMessage());
            throw e; // Re-throw SQL exceptions as-is for transaction handling
        }
    }
    
    /**
     * Updates an existing question with new content and tags.
     * 
     * @param questionId The ID of the question to update
     * @param title The new question title (required, 5-200 characters)
     * @param content The new question content (required, 10-5000 characters)
     * @param context Optional context information (max 1000 characters)
     * @param tags List of tags (optional, max 10 tags, each max 50 characters)
     * @return The updated question
     * @throws QuestionException If validation fails, question not found, or user not authorized
     * @throws SQLException If database operation fails
     */
    public Question updateQuestion(Long questionId, String title, String content, String context, List<String> tags)
            throws QuestionException, SQLException {
        
        long startTime = System.currentTimeMillis();
        LOGGER.info("[QUESTION_UPDATE_START] Updating question ID: " + questionId);
        
        // Get current authenticated user
        Optional<Long> currentUserId = getCurrentUserId();
        if (!currentUserId.isPresent()) {
            LOGGER.warning("[QUESTION_UPDATE_AUTH_FAILED] Unauthenticated user attempted to update question ID: " + questionId);
            throw new QuestionException("User must be authenticated to update questions");
        }
        
        Long userId = currentUserId.get();
        LOGGER.info("[QUESTION_UPDATE_AUTH] User " + userId + " updating question ID: " + questionId);
        
        // Validate question exists and user owns it
        Optional<Question> existingQuestion = questionRepository.findById(questionId);
        if (!existingQuestion.isPresent()) {
            LOGGER.warning("[QUESTION_UPDATE_NOT_FOUND] Question ID " + questionId + " not found for user " + userId);
            throw new QuestionException("Question not found with ID: " + questionId);
        }
        
        if (!Objects.equals(existingQuestion.get().getUserId(), userId)) {
            LOGGER.warning("[QUESTION_UPDATE_UNAUTHORIZED] User " + userId + " attempted to update question ID " + questionId + " owned by user " + existingQuestion.get().getUserId());
            throw new QuestionException("User not authorized to update this question");
        }
        
        // Validate input parameters with detailed logging
        try {
            validateQuestionInput(title, content, context, tags);
            LOGGER.info("[QUESTION_UPDATE_VALIDATION] Input validation passed for question ID " + questionId);
        } catch (QuestionException e) {
            LOGGER.warning("[QUESTION_UPDATE_VALIDATION_FAILED] Question ID " + questionId + " - " + e.getMessage());
            throw e;
        }
        
        // Log tag analytics for insights
        if (tags != null && !tags.isEmpty()) {
            LOGGER.info("[QUESTION_TAG_ANALYTICS] User " + userId + " updating question ID " + questionId + " with tags: [" + String.join(", ", tags) + "]");
        }
        
        // Create updated question object with defensive null checks
        String safeTitle = (title != null) ? title.trim() : "";
        String safeContent = (content != null) ? content.trim() : "";
        String safeContext = (context != null) ? context.trim() : null;
        
        Question updatedQuestion = new Question(
            userId,
            safeTitle,
            safeContent,
            safeContext,
            existingQuestion.get().getSubjectId() // Preserve original subject
        );
        updatedQuestion.setId(existingQuestion.get().getId());
        updatedQuestion.setCreatedAt(existingQuestion.get().getCreatedAt());
        
        try {
            // Use repository's transactional saveWithTags method for updates
            Question savedQuestion = questionRepository.saveWithTags(updatedQuestion, tags);
            
            long duration = System.currentTimeMillis() - startTime;
            LOGGER.info("[QUESTION_UPDATE_SUCCESS] Question updated successfully - ID: " + savedQuestion.getId() + 
                       ", User: " + userId + ", Tags: " + (savedQuestion.getTags() != null ? savedQuestion.getTags().size() : 0) + 
                       ", Duration: " + duration + "ms");
            
            return savedQuestion;
            
        } catch (IllegalArgumentException e) {
            LOGGER.error("[QUESTION_UPDATE_FAILED] Repository validation failed for question ID " + questionId + " - " + e.getMessage());
            throw new QuestionException("Question update failed: " + e.getMessage(), e);
        } catch (SQLException e) {
            LOGGER.error("[QUESTION_UPDATE_FAILED] Database error for question ID " + questionId + " - " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Retrieves a question by ID.
     * 
     * @param questionId The question ID to retrieve
     * @return Optional containing the question if found
     * @throws SQLException If database operation fails
     */
    public Optional<Question> getQuestionById(Long questionId) throws SQLException {
        if (questionId == null || questionId <= 0) {
            return Optional.empty();
        }
        
        return questionRepository.findById(questionId);
    }
    
    /**
     * Deletes a question if the current user owns it.
     * 
     * @param questionId The ID of the question to delete
     * @throws QuestionException If user not authenticated, question not found, or not authorized
     * @throws SQLException If database operation fails
     */
    public void deleteQuestion(Long questionId) throws QuestionException, SQLException {
        long startTime = System.currentTimeMillis();
        LOGGER.info("[QUESTION_DELETE_START] Deleting question ID: " + questionId);
        
        // Get current authenticated user
        Optional<Long> currentUserId = getCurrentUserId();
        if (!currentUserId.isPresent()) {
            LOGGER.warning("[QUESTION_DELETE_AUTH_FAILED] Unauthenticated user attempted to delete question ID: " + questionId);
            throw new QuestionException("User must be authenticated to delete questions");
        }
        
        Long userId = currentUserId.get();
        LOGGER.info("[QUESTION_DELETE_AUTH] User " + userId + " deleting question ID: " + questionId);
        
        // Validate question exists and user owns it
        Optional<Question> existingQuestion = questionRepository.findById(questionId);
        if (!existingQuestion.isPresent()) {
            LOGGER.warning("[QUESTION_DELETE_NOT_FOUND] Question ID " + questionId + " not found for user " + userId);
            throw new QuestionException("Question not found with ID: " + questionId);
        }
        
        if (!Objects.equals(existingQuestion.get().getUserId(), userId)) {
            LOGGER.warning("[QUESTION_DELETE_UNAUTHORIZED] User " + userId + " attempted to delete question ID " + questionId + " owned by user " + existingQuestion.get().getUserId());
            throw new QuestionException("User not authorized to delete this question");
        }
        
        try {
            boolean deleted = questionRepository.delete(questionId);
            if (!deleted) {
                LOGGER.error("[QUESTION_DELETE_FAILED] Repository failed to delete question ID: " + questionId);
                throw new QuestionException("Failed to delete question with ID: " + questionId);
            }
            
            long duration = System.currentTimeMillis() - startTime;
            LOGGER.info("[QUESTION_DELETE_SUCCESS] Question deleted successfully - ID: " + questionId + 
                       ", User: " + userId + ", Duration: " + duration + "ms");
            
        } catch (SQLException e) {
            LOGGER.error("[QUESTION_DELETE_FAILED] Database error for question ID " + questionId + " - " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Gets the current authenticated user's ID.
     * 
     * @return Optional containing user ID if authenticated, empty otherwise
     */
    private Optional<Long> getCurrentUserId() {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser != null) {
                return Optional.of(currentUser.getId());
            }
            return Optional.empty();
        } catch (Exception e) {
            LOGGER.error("Error getting current user: " + e.getMessage());
            return Optional.empty();
        }
    }
    
    /**
     * Validates question input parameters according to business rules.
     * 
     * @param title The question title to validate
     * @param content The question content to validate  
     * @param context The optional context to validate
     * @param tags The list of tags to validate
     * @throws QuestionException If any validation rules are violated
     */
    private void validateQuestionInput(String title, String content, String context, List<String> tags) 
            throws QuestionException {
        
        // Validate title
        if (title == null || title.trim().isEmpty()) {
            throw new QuestionException("Question title is required");
        }
        
        String trimmedTitle = title.trim();
        if (trimmedTitle.length() < MIN_TITLE_LENGTH) {
            throw new QuestionException("Question title must be at least " + MIN_TITLE_LENGTH + " characters");
        }
        
        if (trimmedTitle.length() > MAX_TITLE_LENGTH) {
            throw new QuestionException("Question title cannot exceed " + MAX_TITLE_LENGTH + " characters");
        }
        
        // Validate content
        if (content == null || content.trim().isEmpty()) {
            throw new QuestionException("Question content is required");
        }
        
        String trimmedContent = content.trim();
        if (trimmedContent.length() < MIN_CONTENT_LENGTH) {
            throw new QuestionException("Question content must be at least " + MIN_CONTENT_LENGTH + " characters");
        }
        
        if (trimmedContent.length() > MAX_CONTENT_LENGTH) {
            throw new QuestionException("Question content cannot exceed " + MAX_CONTENT_LENGTH + " characters");
        }
        
        // Validate context (optional)
        if (context != null && context.trim().length() > MAX_CONTEXT_LENGTH) {
            throw new QuestionException("Question context cannot exceed " + MAX_CONTEXT_LENGTH + " characters");
        }
        
        // Validate tags (optional)
        if (tags != null) {
            if (tags.size() > MAX_TAGS_PER_QUESTION) {
                throw new QuestionException("Cannot have more than " + MAX_TAGS_PER_QUESTION + " tags per question");
            }
            
            for (String tag : tags) {
                if (tag != null && !tag.trim().isEmpty()) {
                    String trimmedTag = tag.trim();
                    if (trimmedTag.length() > MAX_TAG_LENGTH) {
                        throw new QuestionException("Tag '" + trimmedTag + "' cannot exceed " + MAX_TAG_LENGTH + " characters");
                    }
                    
                    // Check for invalid characters in tags
                    if (!trimmedTag.matches("^[a-zA-Z0-9-_+#.]+$")) {
                        throw new QuestionException("Tag '" + trimmedTag + "' contains invalid characters. Only letters, numbers, hyphens, underscores, plus, hash, and dots are allowed");
                    }
                }
            }
        }
    }
    
    /**
     * Updates user metrics when a question is created.
     * Increments the questions_asked counter for the specified user.
     * 
     * @param userId The ID of the user who created the question
     * @throws SQLException If there's an error updating user metrics
     */
    private void updateUserQuestionMetrics(Long userId) throws SQLException {
        // Get current user to access current metrics
        User currentUser = authService.getCurrentUser();
        if (currentUser == null || !Objects.equals(currentUser.getId(), userId)) {
            LOGGER.warning("[USER_METRICS_UPDATE_WARNING] Current user mismatch for metrics update - expected: " + userId + 
                          ", current: " + (currentUser != null ? currentUser.getId() : "null"));
            return;
        }
        
        // Increment questions_asked counter
        int newQuestionsAsked = currentUser.getQuestionsAsked() + 1;
        
        // Update metrics in database
        boolean updated = userRepository.updateMetrics(
            userId, 
            newQuestionsAsked, 
            currentUser.getAnswersGiven(), 
            currentUser.getTotalUpvotes()
        );
        
        if (updated) {
            // Update the current user object in memory
            currentUser.setQuestionsAsked(newQuestionsAsked);
            LOGGER.info("[USER_METRICS_UPDATE] User " + userId + " questions_asked incremented to " + newQuestionsAsked);
        } else {
            throw new SQLException("Failed to update user metrics in database for user " + userId);
        }
    }
    
    /**
     * Exception for question-related business logic errors.
     */
    public static class QuestionException extends Exception {
        public QuestionException(String message) {
            super(message);
        }
        
        public QuestionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}