package com.upnext.app.domain.question;

/**
 * Represents a user's vote on a question.
 * Implements Reddit-like voting where each user can vote only once per question.
 */
public class QuestionVote {
    private Long id;
    private Long userId;
    private Long questionId;
    private VoteType voteType;
    private String createdAt;
    private String updatedAt;
    
    public enum VoteType {
        UPVOTE("upvote"),
        DOWNVOTE("downvote");
        
        private final String value;
        
        VoteType(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
        
        public static VoteType fromString(String value) {
            for (VoteType type : VoteType.values()) {
                if (type.value.equals(value)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown vote type: " + value);
        }
    }
    
    // Constructors
    public QuestionVote() {}
    
    public QuestionVote(Long userId, Long questionId, VoteType voteType) {
        this.userId = userId;
        this.questionId = questionId;
        this.voteType = voteType;
    }
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Long getQuestionId() {
        return questionId;
    }
    
    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }
    
    public VoteType getVoteType() {
        return voteType;
    }
    
    public void setVoteType(VoteType voteType) {
        this.voteType = voteType;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        QuestionVote that = (QuestionVote) obj;
        return userId.equals(that.userId) && questionId.equals(that.questionId);
    }
    
    @Override
    public int hashCode() {
        return userId.hashCode() * 31 + questionId.hashCode();
    }
    
    @Override
    public String toString() {
        return "QuestionVote{" +
                "id=" + id +
                ", userId=" + userId +
                ", questionId=" + questionId +
                ", voteType=" + voteType +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}