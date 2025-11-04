package com.upnext.app.domain.question;

import java.time.LocalDateTime;

/**
 * Domain entity representing an answer to a question in the Q&A feature.
 * Answers are created by users in response to questions.
 */
public class Answer {
    private Long id;
    private Long questionId;
    private Long userId;
    private String content;
    private int upvotes;
    private int downvotes;
    private boolean isAccepted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String userName; // Denormalized field for the user's name
    
    // Default constructor
    public Answer() {
    }
    
    // Constructor with essential fields
    public Answer(Long questionId, Long userId, String content) {
        this.questionId = questionId;
        this.userId = userId;
        this.content = content;
        this.upvotes = 0;
        this.downvotes = 0;
        this.isAccepted = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Gets the answer ID.
     * 
     * @return The unique identifier for the answer
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the answer ID.
     * 
     * @param id The unique identifier for the answer
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the ID of the question this answer belongs to.
     * 
     * @return The question ID
     */
    public Long getQuestionId() {
        return questionId;
    }

    /**
     * Sets the ID of the question this answer belongs to.
     * 
     * @param questionId The question ID
     */
    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    /**
     * Gets the ID of the user who created this answer.
     * 
     * @return The user ID
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Sets the ID of the user who created this answer.
     * 
     * @param userId The user ID
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * Gets the answer content.
     * 
     * @return The detailed content of the answer
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the answer content.
     * 
     * @param content The detailed content of the answer
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Gets the number of upvotes for the answer.
     * 
     * @return The upvote count
     */
    public int getUpvotes() {
        return upvotes;
    }

    /**
     * Sets the number of upvotes for the answer.
     * 
     * @param upvotes The upvote count
     */
    public void setUpvotes(int upvotes) {
        this.upvotes = upvotes;
    }

    /**
     * Gets the number of downvotes for the answer.
     * 
     * @return The downvote count
     */
    public int getDownvotes() {
        return downvotes;
    }

    /**
     * Sets the number of downvotes for the answer.
     * 
     * @param downvotes The downvote count
     */
    public void setDownvotes(int downvotes) {
        this.downvotes = downvotes;
    }

    /**
     * Checks if this answer has been marked as accepted.
     * 
     * @return true if the answer is accepted, false otherwise
     */
    public boolean isAccepted() {
        return isAccepted;
    }

    /**
     * Sets whether this answer has been accepted.
     * 
     * @param accepted true if the answer is accepted, false otherwise
     */
    public void setAccepted(boolean accepted) {
        this.isAccepted = accepted;
    }

    /**
     * Gets the timestamp when the answer was created.
     * 
     * @return The creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the timestamp when the answer was created.
     * 
     * @param createdAt The creation timestamp
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets the timestamp when the answer was last updated.
     * 
     * @return The update timestamp
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the timestamp when the answer was last updated.
     * 
     * @param updatedAt The update timestamp
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    /**
     * Increments the upvote count by one.
     * 
     * @return The new upvote count
     */
    public int incrementUpvotes() {
        return ++this.upvotes;
    }
    
    /**
     * Decrements the upvote count by one, ensuring it doesn't go below zero.
     * 
     * @return The new upvote count
     */
    public int decrementUpvotes() {
        if (this.upvotes > 0) {
            return --this.upvotes;
        }
        return this.upvotes;
    }
    
    /**
     * Increments the downvote count by one.
     * 
     * @return The new downvote count
     */
    public int incrementDownvotes() {
        return ++this.downvotes;
    }
    
    /**
     * Decrements the downvote count by one, ensuring it doesn't go below zero.
     * 
     * @return The new downvote count
     */
    public int decrementDownvotes() {
        if (this.downvotes > 0) {
            return --this.downvotes;
        }
        return this.downvotes;
    }
    
    /**
     * Gets the user name who posted this answer (denormalized field).
     * 
     * @return The user name
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets the user name who posted this answer (denormalized field).
     * 
     * @param userName The user name
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    /**
     * Updates the timestamp to the current time.
     */
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Checks if this answer is verified (has 10 or more upvotes).
     * Verified answers are considered high-quality and trustworthy.
     * 
     * @return True if the answer has 10 or more upvotes, false otherwise
     */
    public boolean isVerified() {
        return this.upvotes >= 10;
    }
    
    /**
     * Gets the net vote score (upvotes - downvotes).
     * 
     * @return The net vote score
     */
    public int getNetVotes() {
        return this.upvotes - this.downvotes;
    }

    @Override
    public String toString() {
        return "Answer{" +
                "id=" + id +
                ", questionId=" + questionId +
                ", userId=" + userId +
                ", upvotes=" + upvotes +
                ", downvotes=" + downvotes +
                ", isAccepted=" + isAccepted +
                ", createdAt=" + createdAt +
                '}';
    }
}