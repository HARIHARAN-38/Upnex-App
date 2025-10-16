package com.upnext.app.domain;

/**
 * Represents a user skill in the UpNext application.
 * Contains information about a skill including name, description, and proficiency level.
 */
public class Skill {
    private Long skillId;
    private Long userId;
    private String skillName;
    private String description;
    private int proficiencyLevel;
    private String createdAt;
    private String updatedAt;
    
    /**
     * Creates a new skill with default values.
     */
    public Skill() {
        this.proficiencyLevel = 1;
    }
    
    /**
     * Creates a new skill with the specified details.
     * 
     * @param skillName The name of the skill
     * @param description A brief description of the skill
     * @param proficiencyLevel The user's proficiency level (1-10)
     */
    public Skill(String skillName, String description, int proficiencyLevel) {
        this.skillName = skillName;
        this.description = description;
        this.proficiencyLevel = validateProficiencyLevel(proficiencyLevel);
    }
    
    /**
     * Creates a new skill with the specified details.
     * 
     * @param userId The ID of the user who owns this skill
     * @param skillName The name of the skill
     * @param description A brief description of the skill
     * @param proficiencyLevel The user's proficiency level (1-10)
     */
    public Skill(Long userId, String skillName, String description, int proficiencyLevel) {
        this.userId = userId;
        this.skillName = skillName;
        this.description = description;
        this.proficiencyLevel = validateProficiencyLevel(proficiencyLevel);
    }
    
    /**
     * Creates a new skill with all details.
     * 
     * @param skillId The unique identifier for this skill
     * @param userId The ID of the user who owns this skill
     * @param skillName The name of the skill
     * @param description A brief description of the skill
     * @param proficiencyLevel The user's proficiency level (1-10)
     * @param createdAt When the skill was created
     * @param updatedAt When the skill was last updated
     */
    public Skill(Long skillId, Long userId, String skillName, String description, int proficiencyLevel, 
                 String createdAt, String updatedAt) {
        this.skillId = skillId;
        this.userId = userId;
        this.skillName = skillName;
        this.description = description;
        this.proficiencyLevel = validateProficiencyLevel(proficiencyLevel);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    /**
     * Gets the skill ID.
     * 
     * @return The skill ID
     */
    public Long getSkillId() {
        return skillId;
    }
    
    /**
     * Sets the skill ID.
     * 
     * @param skillId The skill ID
     */
    public void setSkillId(Long skillId) {
        this.skillId = skillId;
    }
    
    /**
     * Gets the user ID associated with this skill.
     * 
     * @return The user ID
     */
    public Long getUserId() {
        return userId;
    }
    
    /**
     * Sets the user ID associated with this skill.
     * 
     * @param userId The user ID
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    /**
     * Gets the name of the skill.
     * 
     * @return The skill name
     */
    public String getSkillName() {
        return skillName;
    }
    
    /**
     * Sets the name of the skill.
     * 
     * @param skillName The skill name
     */
    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }
    
    /**
     * Gets the description of the skill.
     * 
     * @return The skill description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Sets the description of the skill.
     * 
     * @param description The skill description
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Gets the proficiency level of the skill.
     * 
     * @return The proficiency level (1-10)
     */
    public int getProficiencyLevel() {
        return proficiencyLevel;
    }
    
    /**
     * Sets the proficiency level of the skill.
     * Ensures the level is within the valid range of 1-10.
     * 
     * @param proficiencyLevel The proficiency level
     */
    public void setProficiencyLevel(int proficiencyLevel) {
        this.proficiencyLevel = validateProficiencyLevel(proficiencyLevel);
    }
    
    /**
     * Gets the creation timestamp.
     * 
     * @return When the skill was created
     */
    public String getCreatedAt() {
        return createdAt;
    }
    
    /**
     * Sets the creation timestamp.
     * 
     * @param createdAt When the skill was created
     */
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    /**
     * Gets the last update timestamp.
     * 
     * @return When the skill was last updated
     */
    public String getUpdatedAt() {
        return updatedAt;
    }
    
    /**
     * Sets the last update timestamp.
     * 
     * @param updatedAt When the skill was last updated
     */
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    /**
     * Validates and constrains the proficiency level to be between 1 and 10.
     * 
     * @param level The proficiency level to validate
     * @return A valid proficiency level (between 1 and 10)
     */
    private int validateProficiencyLevel(int level) {
        if (level < 1) {
            return 1;
        } else if (level > 10) {
            return 10;
        }
        return level;
    }
    
    @Override
    public String toString() {
        return "Skill{" +
                "skillId=" + skillId +
                ", userId=" + userId +
                ", skillName='" + skillName + '\'' +
                ", description='" + description + '\'' +
                ", proficiencyLevel=" + proficiencyLevel +
                '}';
    }
}