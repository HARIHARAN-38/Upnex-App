package com.upnext.app.domain.tag;

import java.time.LocalDateTime;

/**
 * Domain entity representing a tag that can be associated with questions.
 * Tags help categorize and organize questions for better discoverability.
 */
public class Tag {
    private Long id;
    private String name;
    private int usageCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Default constructor
    public Tag() {
    }
    
    // Constructor with essential fields
    public Tag(String name) {
        this.name = name;
        this.usageCount = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Gets the tag ID.
     * 
     * @return The unique identifier for the tag
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the tag ID.
     * 
     * @param id The unique identifier for the tag
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the tag name.
     * 
     * @return The name of the tag
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the tag name.
     * 
     * @param name The name of the tag
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the usage count of this tag.
     * 
     * @return The number of times this tag has been used
     */
    public int getUsageCount() {
        return usageCount;
    }

    /**
     * Sets the usage count of this tag.
     * 
     * @param usageCount The number of times this tag has been used
     */
    public void setUsageCount(int usageCount) {
        this.usageCount = usageCount;
    }

    /**
     * Increments the usage count by 1.
     * 
     * @return The new usage count
     */
    public int incrementUsageCount() {
        return ++usageCount;
    }

    /**
     * Decrements the usage count by 1, ensuring it doesn't go below zero.
     * 
     * @return The new usage count
     */
    public int decrementUsageCount() {
        if (usageCount > 0) {
            usageCount--;
        }
        return usageCount;
    }

    /**
     * Gets the timestamp when the tag was created.
     * 
     * @return The creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the timestamp when the tag was created.
     * 
     * @param createdAt The creation timestamp
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets the timestamp when the tag was last updated.
     * 
     * @return The update timestamp
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the timestamp when the tag was last updated.
     * 
     * @param updatedAt The update timestamp
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Updates the timestamp to the current time.
     */
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Tag{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", usageCount=" + usageCount +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Tag tag = (Tag) obj;
        return name != null ? name.equals(tag.name) : tag.name == null;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}