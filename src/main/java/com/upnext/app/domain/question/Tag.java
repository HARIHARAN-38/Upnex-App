package com.upnext.app.domain.question;

/**
 * Domain entity representing a tag for questions.
 * Tags provide additional metadata and search optimization for questions.
 */
public class Tag {
    private Long id;
    private String name;
    private int usageCount;
    
    // Default constructor
    public Tag() {
    }
    
    // Constructor with essential fields
    public Tag(String name) {
        this.name = name;
        this.usageCount = 0;
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
     * @return The tag name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the tag name.
     * 
     * @param name The tag name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the number of times this tag has been used.
     * 
     * @return The usage count
     */
    public int getUsageCount() {
        return usageCount;
    }

    /**
     * Sets the number of times this tag has been used.
     * 
     * @param usageCount The usage count
     */
    public void setUsageCount(int usageCount) {
        this.usageCount = usageCount;
    }
    
    /**
     * Increments the usage count by one.
     */
    public void incrementUsageCount() {
        this.usageCount++;
    }
    
    /**
     * Decrements the usage count by one, ensuring it doesn't go below zero.
     */
    public void decrementUsageCount() {
        if (this.usageCount > 0) {
            this.usageCount--;
        }
    }

    @Override
    public String toString() {
        return "Tag{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", usageCount=" + usageCount +
                '}';
    }
}