package com.upnext.app.service;

import com.upnext.app.core.Logger;
import com.upnext.app.data.SkillRepository;
import com.upnext.app.domain.Skill;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for user skill operations.
 * Handles adding, retrieving, updating, and removing skills.
 */
public class SkillService {
    // Singleton instance
    private static SkillService instance;
    
    // Dependencies
    private final SkillRepository skillRepository;
    private final Logger logger = Logger.getInstance();
    
    /**
     * Private constructor to enforce singleton pattern.
     */
    private SkillService() {
        this.skillRepository = SkillRepository.getInstance();
    }
    
    /**
     * Gets the singleton instance of the service.
     * 
     * @return The service instance
     */
    public static synchronized SkillService getInstance() {
        if (instance == null) {
            instance = new SkillService();
        }
        return instance;
    }
    
    /**
     * Adds a new skill for a user.
     * 
     * @param userId The ID of the user
     * @param skillName The name of the skill
     * @param description The description of the skill
     * @param proficiencyLevel The proficiency level (1-10)
     * @return The newly created skill
     * @throws SkillException If there's an error adding the skill
     */
    public Skill addSkill(Long userId, String skillName, String description, int proficiencyLevel) throws SkillException {
        if (userId == null) {
            throw new SkillException("User ID cannot be null");
        }
        
        if (skillName == null || skillName.trim().isEmpty()) {
            throw new SkillException("Skill name is required");
        }
        
        // Validate proficiency level
        if (proficiencyLevel < 1 || proficiencyLevel > 10) {
            proficiencyLevel = Math.max(1, Math.min(10, proficiencyLevel)); // Constrain to 1-10
        }
        
        try {
            logger.info("Adding skill for user ID: " + userId + ", skill: " + skillName);
            
            Skill skill = new Skill();
            skill.setUserId(userId);
            skill.setSkillName(skillName.trim());
            skill.setDescription(description != null ? description.trim() : "");
            skill.setProficiencyLevel(proficiencyLevel);
            
            Skill savedSkill = skillRepository.save(skill);
            logger.info("Skill added successfully: " + savedSkill.getSkillId());
            return savedSkill;
        } catch (SQLException e) {
            logger.logException("Database error while adding skill", e);
            throw new SkillException("Failed to add skill: " + e.getMessage(), e);
        }
    }
    
    /**
     * Adds a skill directly from a Skill object.
     * 
     * @param skill The skill to add
     * @return The saved skill
     * @throws SkillException If there's an error adding the skill
     */
    public Skill addSkill(Skill skill) throws SkillException {
        if (skill == null) {
            throw new SkillException("Skill cannot be null");
        }
        
        return addSkill(skill.getUserId(), skill.getSkillName(), skill.getDescription(), skill.getProficiencyLevel());
    }
    
    /**
     * Retrieves all skills for a user.
     * 
     * @param userId The ID of the user
     * @return A list of skills for the user
     * @throws SkillException If there's an error retrieving the skills
     */
    public List<Skill> getUserSkills(Long userId) throws SkillException {
        if (userId == null) {
            throw new SkillException("User ID cannot be null");
        }
        
        try {
            logger.info("Retrieving skills for user ID: " + userId);
            return skillRepository.findByUserId(userId);
        } catch (SQLException e) {
            logger.logException("Database error while retrieving skills", e);
            throw new SkillException("Failed to retrieve skills: " + e.getMessage(), e);
        }
    }
    
    /**
     * Gets a skill by its ID.
     * 
     * @param skillId The ID of the skill
     * @return The skill if found
     * @throws SkillException If the skill is not found or there's an error retrieving it
     */
    public Skill getSkill(Long skillId) throws SkillException {
        if (skillId == null) {
            throw new SkillException("Skill ID cannot be null");
        }
        
        try {
            logger.info("Retrieving skill with ID: " + skillId);
            Optional<Skill> skill = skillRepository.findById(skillId);
            
            if (skill.isPresent()) {
                return skill.get();
            } else {
                logger.warning("Skill not found with ID: " + skillId);
                throw new SkillException("Skill not found");
            }
        } catch (SQLException e) {
            logger.logException("Database error while retrieving skill", e);
            throw new SkillException("Failed to retrieve skill: " + e.getMessage(), e);
        }
    }
    
    /**
     * Updates an existing skill.
     * 
     * @param skillId The ID of the skill to update
     * @param skillName The new name for the skill
     * @param description The new description for the skill
     * @param proficiencyLevel The new proficiency level for the skill
     * @return The updated skill
     * @throws SkillException If the skill is not found or there's an error updating it
     */
    public Skill updateSkill(Long skillId, String skillName, String description, int proficiencyLevel) throws SkillException {
        if (skillId == null) {
            throw new SkillException("Skill ID cannot be null");
        }
        
        if (skillName == null || skillName.trim().isEmpty()) {
            throw new SkillException("Skill name is required");
        }
        
        // Validate proficiency level
        if (proficiencyLevel < 1 || proficiencyLevel > 10) {
            proficiencyLevel = Math.max(1, Math.min(10, proficiencyLevel)); // Constrain to 1-10
        }
        
        try {
            logger.info("Updating skill with ID: " + skillId);
            
            // Check if skill exists
            Optional<Skill> optionalSkill = skillRepository.findById(skillId);
            if (!optionalSkill.isPresent()) {
                logger.warning("Cannot update - skill not found with ID: " + skillId);
                throw new SkillException("Skill not found");
            }
            
            Skill skill = optionalSkill.get();
            skill.setSkillName(skillName.trim());
            skill.setDescription(description != null ? description.trim() : "");
            skill.setProficiencyLevel(proficiencyLevel);
            
            boolean updated = skillRepository.update(skill);
            if (updated) {
                logger.info("Skill updated successfully: " + skillId);
                return skill;
            } else {
                logger.warning("Failed to update skill: " + skillId);
                throw new SkillException("Failed to update skill");
            }
        } catch (SQLException e) {
            logger.logException("Database error while updating skill", e);
            throw new SkillException("Failed to update skill: " + e.getMessage(), e);
        }
    }
    
    /**
     * Updates the proficiency level of a skill.
     * 
     * @param skillId The ID of the skill to update
     * @param proficiencyLevel The new proficiency level (1-10)
     * @return The updated skill
     * @throws SkillException If the skill is not found or there's an error updating it
     */
    public Skill updateSkillProficiency(Long skillId, int proficiencyLevel) throws SkillException {
        if (skillId == null) {
            throw new SkillException("Skill ID cannot be null");
        }
        
        try {
            logger.info("Updating proficiency for skill ID: " + skillId);
            
            // Check if skill exists
            Optional<Skill> optionalSkill = skillRepository.findById(skillId);
            if (!optionalSkill.isPresent()) {
                logger.warning("Cannot update proficiency - skill not found with ID: " + skillId);
                throw new SkillException("Skill not found");
            }
            
            Skill skill = optionalSkill.get();
            skill.setProficiencyLevel(proficiencyLevel); // This will constrain to 1-10
            
            boolean updated = skillRepository.update(skill);
            if (updated) {
                logger.info("Skill proficiency updated successfully: " + skillId);
                return skill;
            } else {
                logger.warning("Failed to update skill proficiency: " + skillId);
                throw new SkillException("Failed to update skill proficiency");
            }
        } catch (SQLException e) {
            logger.logException("Database error while updating skill proficiency", e);
            throw new SkillException("Failed to update skill proficiency: " + e.getMessage(), e);
        }
    }
    
    /**
     * Deletes a skill.
     * 
     * @param skillId The ID of the skill to delete
     * @throws SkillException If there's an error deleting the skill
     */
    public void deleteSkill(Long skillId) throws SkillException {
        if (skillId == null) {
            throw new SkillException("Skill ID cannot be null");
        }
        
        try {
            logger.info("Deleting skill with ID: " + skillId);
            
            boolean deleted = skillRepository.deleteById(skillId);
            if (!deleted) {
                logger.warning("Failed to delete skill - skill may not exist: " + skillId);
                throw new SkillException("Failed to delete skill - skill may not exist");
            }
            
            logger.info("Skill deleted successfully: " + skillId);
        } catch (SQLException e) {
            logger.logException("Database error while deleting skill", e);
            throw new SkillException("Failed to delete skill: " + e.getMessage(), e);
        }
    }
    
    /**
     * Deletes all skills for a user.
     * 
     * @param userId The ID of the user
     * @return The number of skills deleted
     * @throws SkillException If there's an error deleting the skills
     */
    public int deleteUserSkills(Long userId) throws SkillException {
        if (userId == null) {
            throw new SkillException("User ID cannot be null");
        }
        
        try {
            logger.info("Deleting all skills for user ID: " + userId);
            
            int deletedCount = skillRepository.deleteByUserId(userId);
            logger.info("Deleted " + deletedCount + " skills for user ID: " + userId);
            
            return deletedCount;
        } catch (SQLException e) {
            logger.logException("Database error while deleting user skills", e);
            throw new SkillException("Failed to delete user skills: " + e.getMessage(), e);
        }
    }
    
    /**
     * Adds multiple skills for a user.
     * 
     * @param userId The ID of the user
     * @param skills List of skills to add
     * @return List of saved skills
     * @throws SkillException If there's an error adding the skills
     */
    public List<Skill> addSkills(Long userId, List<Skill> skills) throws SkillException {
        if (userId == null) {
            throw new SkillException("User ID cannot be null");
        }
        
        if (skills == null || skills.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Skill> savedSkills = new ArrayList<>();
        for (Skill skill : skills) {
            skill.setUserId(userId); // Ensure the user ID is set correctly
            savedSkills.add(addSkill(skill));
        }
        
        logger.info("Added " + savedSkills.size() + " skills for user ID: " + userId);
        return savedSkills;
    }
    
    /**
     * Exception for skill-related errors.
     */
    public static class SkillException extends Exception {
        public SkillException(String message) {
            super(message);
        }
        
        public SkillException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}