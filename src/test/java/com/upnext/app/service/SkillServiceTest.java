package com.upnext.app.service;

import com.upnext.app.domain.Skill;
import com.upnext.app.domain.User;
import com.upnext.app.data.UserRepository;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests for the SkillService class.
 */
public class SkillServiceTest {
    private static final SkillService skillService = SkillService.getInstance();
    private static final UserRepository userRepository = UserRepository.getInstance();
    private static User testUser;
    
    /**
     * Main method to run the tests.
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        try {
            System.out.println("Starting SkillService tests...");
            
            // Create a test user for skills
            testUser = createTestUser();
            System.out.println("Created test user with ID: " + testUser.getId());
            
            // Test adding a skill
            Skill addedSkill = testAddSkill();
            System.out.println("Added test skill with ID: " + addedSkill.getSkillId());
            
            // Test retrieving user skills
            testGetUserSkills();
            
            // Test adding multiple skills
            List<Skill> addedSkills = testAddSkills();
            System.out.println("Added " + addedSkills.size() + " additional skills");
            
            // Test updating skill
            testUpdateSkill(addedSkill);
            
            // Test updating skill proficiency
            testUpdateSkillProficiency(addedSkills.get(0));
            
            // Test skill validation
            testSkillValidation();
            
            // Test deleting a skill
            testDeleteSkill(addedSkills.get(1));
            
            // Test deleting all user skills
            testDeleteUserSkills();
            
            // Clean up - remove test user
            userRepository.delete(testUser.getId());
            System.out.println("Deleted test user with ID: " + testUser.getId());
            
            System.out.println("All SkillService tests completed successfully!");
        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Creates a test user for skill tests.
     * 
     * @return The created user
     * @throws SQLException If there's an error creating the user
     */
    private static User createTestUser() throws SQLException {
        User user = new User();
        user.setName("Service Test User");
        user.setEmail("skillservice_" + System.currentTimeMillis() + "@example.com");
        user.setPasswordHash("hashedpassword");
        user.setSalt("salt123");
        
        return userRepository.save(user);
    }
    
    /**
     * Tests adding a skill through the service.
     * 
     * @return The added skill
     * @throws SkillException If there's an error adding the skill
     */
    private static Skill testAddSkill() throws SkillException {
        Skill skill = skillService.addSkill(
            testUser.getId(),
            "Java Development",
            "Core Java and frameworks",
            8
        );
        
        // Verify skill was added correctly
        assert skill.getSkillId() != null : "Skill ID should not be null";
        assert skill.getUserId().equals(testUser.getId()) : "User ID does not match";
        assert skill.getSkillName().equals("Java Development") : "Skill name does not match";
        assert skill.getDescription().equals("Core Java and frameworks") : "Description does not match";
        assert skill.getProficiencyLevel() == 8 : "Proficiency level does not match";
        
        System.out.println("✅ Add skill test passed");
        return skill;
    }
    
    /**
     * Tests getting skills for a user through the service.
     * 
     * @throws SkillException If there's an error getting the skills
     */
    private static void testGetUserSkills() throws SkillException {
        List<Skill> userSkills = skillService.getUserSkills(testUser.getId());
        
        assert userSkills != null : "Skills list should not be null";
        assert !userSkills.isEmpty() : "User should have skills";
        
        // All skills should belong to the user
        for (Skill skill : userSkills) {
            assert skill.getUserId().equals(testUser.getId()) : "Skill does not belong to the correct user";
        }
        
        System.out.println("✅ Get user skills test passed - found " + userSkills.size() + " skills");
    }
    
    /**
     * Tests adding multiple skills at once.
     * 
     * @return The list of added skills
     * @throws SkillException If there's an error adding the skills
     */
    private static List<Skill> testAddSkills() throws SkillException {
        List<Skill> skillsToAdd = new ArrayList<>();
        
        // Create first skill
        Skill skill1 = new Skill();
        skill1.setUserId(testUser.getId());
        skill1.setSkillName("Database Design");
        skill1.setDescription("Expertise in SQL and NoSQL");
        skill1.setProficiencyLevel(7);
        skillsToAdd.add(skill1);
        
        // Create second skill
        Skill skill2 = new Skill();
        skill2.setUserId(testUser.getId());
        skill2.setSkillName("UI/UX Design");
        skill2.setDescription("User interface and experience design");
        skill2.setProficiencyLevel(9);
        skillsToAdd.add(skill2);
        
        // Add the skills
        List<Skill> addedSkills = skillService.addSkills(testUser.getId(), skillsToAdd);
        
        assert addedSkills != null : "Added skills list should not be null";
        assert addedSkills.size() == skillsToAdd.size() : "Number of added skills does not match";
        
        // Verify all skills were added with correct properties
        for (int i = 0; i < skillsToAdd.size(); i++) {
            Skill original = skillsToAdd.get(i);
            Skill added = addedSkills.get(i);
            
            assert added.getSkillId() != null : "Skill ID should not be null";
            assert added.getUserId().equals(testUser.getId()) : "User ID does not match";
            assert added.getSkillName().equals(original.getSkillName()) : "Skill name does not match";
            assert added.getDescription().equals(original.getDescription()) : "Description does not match";
            assert added.getProficiencyLevel() == original.getProficiencyLevel() : "Proficiency level does not match";
        }
        
        System.out.println("✅ Add multiple skills test passed");
        return addedSkills;
    }
    
    /**
     * Tests updating a skill through the service.
     * 
     * @param skill The skill to update
     * @throws SkillException If there's an error updating the skill
     */
    private static void testUpdateSkill(Skill skill) throws SkillException {
        String newName = "Java Development (Advanced)";
        String newDescription = "Enterprise Java development with Spring";
        int newProficiency = 9;
        
        Skill updatedSkill = skillService.updateSkill(
            skill.getSkillId(),
            newName,
            newDescription,
            newProficiency
        );
        
        // Verify the update
        assert updatedSkill.getSkillName().equals(newName) : "Skill name was not updated";
        assert updatedSkill.getDescription().equals(newDescription) : "Skill description was not updated";
        assert updatedSkill.getProficiencyLevel() == newProficiency : "Skill proficiency level was not updated";
        
        // Get the skill again to verify persistence
        Skill retrievedSkill = skillService.getSkill(skill.getSkillId());
        assert retrievedSkill.getSkillName().equals(newName) : "Updated skill name not persisted";
        assert retrievedSkill.getDescription().equals(newDescription) : "Updated skill description not persisted";
        assert retrievedSkill.getProficiencyLevel() == newProficiency : "Updated skill proficiency level not persisted";
        
        System.out.println("✅ Update skill test passed");
    }
    
    /**
     * Tests updating only the proficiency level of a skill.
     * 
     * @param skill The skill to update
     * @throws SkillException If there's an error updating the skill
     */
    private static void testUpdateSkillProficiency(Skill skill) throws SkillException {
        String originalName = skill.getSkillName();
        String originalDescription = skill.getDescription();
        int newProficiency = 10;
        
        Skill updatedSkill = skillService.updateSkillProficiency(
            skill.getSkillId(),
            newProficiency
        );
        
        // Verify only proficiency was updated
        assert updatedSkill.getSkillName().equals(originalName) : "Skill name should not change";
        assert updatedSkill.getDescription().equals(originalDescription) : "Skill description should not change";
        assert updatedSkill.getProficiencyLevel() == newProficiency : "Skill proficiency level was not updated";
        
        System.out.println("✅ Update skill proficiency test passed");
    }
    
    /**
     * Tests validation rules in the skill service.
     * 
     * @throws SkillException If there's an error with skill operations
     */
    private static void testSkillValidation() throws SkillException {
        // Test null user ID
        boolean caughtNullUserException = false;
        try {
            skillService.addSkill(null, "Test Skill", "Description", 5);
        } catch (SkillException e) {
            caughtNullUserException = true;
        }
        assert caughtNullUserException : "Should throw exception for null user ID";
        
        // Test empty skill name
        boolean caughtEmptyNameException = false;
        try {
            skillService.addSkill(testUser.getId(), "", "Description", 5);
        } catch (SkillException e) {
            caughtEmptyNameException = true;
        }
        assert caughtEmptyNameException : "Should throw exception for empty skill name";
        
        // Test proficiency level constraints (too low)
        Skill skillWithLowProficiency = skillService.addSkill(
            testUser.getId(),
            "Low Proficiency Skill",
            "Testing proficiency constraints",
            -5
        );
        assert skillWithLowProficiency.getProficiencyLevel() == 1 : "Proficiency should be constrained to minimum 1";
        
        // Test proficiency level constraints (too high)
        Skill skillWithHighProficiency = skillService.addSkill(
            testUser.getId(),
            "High Proficiency Skill",
            "Testing proficiency constraints",
            15
        );
        assert skillWithHighProficiency.getProficiencyLevel() == 10 : "Proficiency should be constrained to maximum 10";
        
        System.out.println("✅ Skill validation tests passed");
    }
    
    /**
     * Tests deleting a skill through the service.
     * 
     * @param skill The skill to delete
     * @throws SkillException If there's an error deleting the skill
     */
    private static void testDeleteSkill(Skill skill) throws SkillException {
        Long skillId = skill.getSkillId();
        
        // Delete the skill
        skillService.deleteSkill(skillId);
        
        // Verify the skill was deleted
        boolean caughtNotFoundException = false;
        try {
            skillService.getSkill(skillId);
        } catch (SkillException e) {
            caughtNotFoundException = true;
        }
        assert caughtNotFoundException : "Should throw exception when getting deleted skill";
        
        System.out.println("✅ Delete skill test passed");
    }
    
    /**
     * Tests deleting all skills for a user through the service.
     * 
     * @throws SkillException If there's an error deleting the skills
     */
    private static void testDeleteUserSkills() throws SkillException {
        // First verify the user has skills
        List<Skill> userSkillsBefore = skillService.getUserSkills(testUser.getId());
        assert !userSkillsBefore.isEmpty() : "User should have skills before deletion";
        
        // Delete all user skills
        int deletedCount = skillService.deleteUserSkills(testUser.getId());
        assert deletedCount > 0 : "Some skills should be deleted";
        
        // Verify skills were deleted
        List<Skill> userSkillsAfter = skillService.getUserSkills(testUser.getId());
        assert userSkillsAfter.isEmpty() : "User should have no skills after deletion";
        
        System.out.println("✅ Delete user skills test passed - deleted " + deletedCount + " skills");
    }
}