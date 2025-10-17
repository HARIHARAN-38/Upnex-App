package com.upnext.app.domain.question;

/**
 * Domain entity representing a subject category for questions.
 * Subjects provide high-level categorization for questions.
 */
public class Subject {
    private Long id;
    private String name;
    private String description;
    
    // Default constructor
    public Subject() {
    }
    
    // Constructor with essential fields
    public Subject(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * Gets the subject ID.
     * 
     * @return The unique identifier for the subject
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the subject ID.
     * 
     * @param id The unique identifier for the subject
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the subject name.
     * 
     * @return The subject name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the subject name.
     * 
     * @param name The subject name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the subject description.
     * 
     * @return The subject description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the subject description.
     * 
     * @param description The subject description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Subject{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}