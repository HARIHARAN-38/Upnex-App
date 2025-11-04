package com.upnext.app.domain.question;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Domain model representing a vote on an answer.
 * Tracks which user voted on which answer and whether it was an upvote or downvote.
 * Prevents duplicate voting by the same user on the same answer.
 */
public class AnswerVote {
    private Long id;
    private Long answerId;
    private Long userId;
    private boolean isUpvote;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Default constructor for framework use.
     */
    public AnswerVote() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Creates a new answer vote.
     * 
     * @param answerId The ID of the answer being voted on
     * @param userId The ID of the user casting the vote
     * @param isUpvote True for upvote, false for downvote
     */
    public AnswerVote(Long answerId, Long userId, boolean isUpvote) {
        this();
        this.answerId = answerId;
        this.userId = userId;
        this.isUpvote = isUpvote;
    }
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getAnswerId() {
        return answerId;
    }
    
    public void setAnswerId(Long answerId) {
        this.answerId = answerId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public boolean isUpvote() {
        return isUpvote;
    }
    
    public void setUpvote(boolean upvote) {
        this.isUpvote = upvote;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    /**
     * Updates the vote type and timestamp.
     * 
     * @param isUpvote The new vote type
     */
    public void updateVote(boolean isUpvote) {
        this.isUpvote = isUpvote;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Checks if this is a downvote.
     * 
     * @return True if this is a downvote, false if upvote
     */
    public boolean isDownvote() {
        return !isUpvote;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnswerVote that = (AnswerVote) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(answerId, that.answerId) &&
               Objects.equals(userId, that.userId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, answerId, userId);
    }
    
    @Override
    public String toString() {
        return "AnswerVote{" +
                "id=" + id +
                ", answerId=" + answerId +
                ", userId=" + userId +
                ", isUpvote=" + isUpvote +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}