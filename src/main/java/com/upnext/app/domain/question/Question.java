package com.upnext.app.domain.question;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Domain entity representing a question in the Q&A feature.
 * Questions are created by users and can be categorized by subjects and tags.
 */
public class Question {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private String context; // Additional context or background information
    private Long subjectId;
    private String subjectName; // Transient field for UI display
    private int upvotes;
    private int downvotes;
    private int answerCount;
    private boolean isSolved;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> tags = new ArrayList<>();
    private int viewCount;
    private String userName; // Denormalized field for the user's name
    
    // Default constructor
    public Question() {
    }
    
    // Constructor with essential fields
    public Question(Long userId, String title, String content, String context, Long subjectId) {
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.context = context;
        this.subjectId = subjectId;
        this.upvotes = 0;
        this.downvotes = 0;
        this.answerCount = 0;
        this.isSolved = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Backward-compatible constructor without context
    public Question(Long userId, String title, String content, Long subjectId) {
        this(userId, title, content, null, subjectId);
    }

    /**
     * Gets the question ID.
     * 
     * @return The unique identifier for the question
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the question ID.
     * 
     * @param id The unique identifier for the question
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the ID of the user who created the question.
     * 
     * @return The user ID
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Sets the ID of the user who created the question.
     * 
     * @param userId The user ID
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * Gets the question title.
     * 
     * @return The question title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the question title.
     * 
     * @param title The question title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets the question content.
     * 
     * @return The detailed content of the question
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the question content.
     * 
     * @param content The detailed content of the question
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Gets the question context.
     * 
     * @return The additional context or background information for the question
     */
    public String getContext() {
        return context;
    }

    /**
     * Sets the question context.
     * 
     * @param context The additional context or background information for the question
     */
    public void setContext(String context) {
        this.context = context;
    }

    /**
     * Gets the ID of the subject category.
     * 
     * @return The subject ID
     */
    public Long getSubjectId() {
        return subjectId;
    }

    /**
     * Sets the ID of the subject category.
     * 
     * @param subjectId The subject ID
     */
    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }
    
    /**
     * Gets the name of the subject category.
     * This is a transient field used for UI display.
     * 
     * @return The subject name
     */
    public String getSubjectName() {
        return subjectName;
    }
    
    /**
     * Sets the name of the subject category.
     * This is a transient field used for UI display.
     * 
     * @param subjectName The subject name
     */
    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    /**
     * Gets the number of upvotes for the question.
     * 
     * @return The upvote count
     */
    public int getUpvotes() {
        return upvotes;
    }

    /**
     * Sets the number of upvotes for the question.
     * 
     * @param upvotes The upvote count
     */
    public void setUpvotes(int upvotes) {
        this.upvotes = upvotes;
    }

    /**
     * Gets the number of downvotes for the question.
     * 
     * @return The downvote count
     */
    public int getDownvotes() {
        return downvotes;
    }

    /**
     * Sets the number of downvotes for the question.
     * 
     * @param downvotes The downvote count
     */
    public void setDownvotes(int downvotes) {
        this.downvotes = downvotes;
    }

    /**
     * Gets the number of answers to this question.
     * 
     * @return The answer count
     */
    public int getAnswerCount() {
        return answerCount;
    }

    /**
     * Sets the number of answers to this question.
     * 
     * @param answerCount The answer count
     */
    public void setAnswerCount(int answerCount) {
        this.answerCount = answerCount;
    }

    /**
     * Checks if the question has been marked as solved.
     * 
     * @return true if the question is solved, false otherwise
     */
    public boolean isSolved() {
        return isSolved;
    }

    /**
     * Sets whether the question has been solved.
     * 
     * @param solved true if the question is solved, false otherwise
     */
    public void setSolved(boolean solved) {
        this.isSolved = solved;
    }

    /**
     * Gets the timestamp when the question was created.
     * 
     * @return The creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the timestamp when the question was created.
     * 
     * @param createdAt The creation timestamp
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets the timestamp when the question was last updated.
     * 
     * @return The update timestamp
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the timestamp when the question was last updated.
     * 
     * @param updatedAt The update timestamp
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    /**
     * Gets the list of tags associated with this question.
     * 
     * @return The list of tag names
     */
    public List<String> getTags() {
        return tags;
    }
    
    /**
     * Sets the list of tags associated with this question.
     * 
     * @param tags The list of tag names
     */
    public void setTags(List<String> tags) {
        this.tags = tags != null ? tags : new ArrayList<>();
    }
    
    /**
     * Adds a tag to this question.
     * 
     * @param tag The tag to add
     */
    public void addTag(String tag) {
        if (tag != null && !tag.trim().isEmpty()) {
            this.tags.add(tag);
        }
    }
    
    /**
     * Removes a tag from this question.
     * 
     * @param tag The tag to remove
     * @return true if the tag was removed, false if it wasn't found
     */
    public boolean removeTag(String tag) {
        return this.tags.remove(tag);
    }
    
    /**
     * Increments the upvote count by one.
     */
    public void incrementUpvotes() {
        this.upvotes++;
    }
    
    /**
     * Decrements the upvote count by one, ensuring it doesn't go below zero.
     */
    public void decrementUpvotes() {
        if (this.upvotes > 0) {
            this.upvotes--;
        }
    }
    
    /**
     * Increments the downvote count by one.
     */
    public void incrementDownvotes() {
        this.downvotes++;
    }
    
    /**
     * Decrements the downvote count by one, ensuring it doesn't go below zero.
     */
    public void decrementDownvotes() {
        if (this.downvotes > 0) {
            this.downvotes--;
        }
    }
    
    /**
     * Increments the answer count by one.
     */
    public void incrementAnswerCount() {
        this.answerCount++;
    }
    
    /**
     * Decrements the answer count by one, ensuring it doesn't go below zero.
     */
    public void decrementAnswerCount() {
        if (this.answerCount > 0) {
            this.answerCount--;
        }
    }
    
    /**
     * Updates the timestamp to the current time.
     */
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Gets the view count for this question.
     * 
     * @return The number of times this question has been viewed
     */
    public int getViewCount() {
        return viewCount;
    }

    /**
     * Sets the view count for this question.
     * 
     * @param viewCount The new view count
     */
    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    /**
     * Increments the view count by 1.
     * 
     * @return The new view count
     */
    public int incrementViewCount() {
        return ++viewCount;
    }

    /**
     * Gets the user name who posted this question (denormalized field).
     * 
     * @return The user name
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets the user name who posted this question (denormalized field).
     * 
     * @param userName The user name
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String toString() {
        return "Question{" +
                "id=" + id +
                ", userId=" + userId +
                ", title='" + title + '\'' +
                ", context='" + context + '\'' +
                ", subjectId=" + subjectId +
                ", upvotes=" + upvotes +
                ", downvotes=" + downvotes +
                ", answerCount=" + answerCount +
                ", isSolved=" + isSolved +
                ", createdAt=" + createdAt +
                ", tags=" + tags +
                '}';
    }
}