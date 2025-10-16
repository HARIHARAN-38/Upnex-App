package com.upnext.app.data;

import com.upnext.app.domain.Skill;
import com.upnext.app.domain.User;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Tests for the SkillRepository class.
 */
public class SkillRepositoryTest {
    private static final SkillRepository skillRepository = SkillRepository.getInstance();
    private static final UserRepository userRepository = UserRepository.getInstance();
    
    /**
     * Main method to run the tests.
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        try {
            System.out.println("Starting SkillRepository tests...");
            
            // Create a test user for skills
            User testUser = createTestUser();
            System.out.println("Created test user with ID: " + testUser.getId());
            
            // Test skill creation
            Skill createdSkill = testSkillCreation(testUser.getId());
            System.out.println("Created test skill with ID: " + createdSkill.getSkillId());
            
            // Test finding skill by ID
            testFindById(createdSkill.getSkillId());
            
            // Create more skills for the user
            Skill skill2 = createSkill(testUser.getId(), "Java Programming", "Experience with Java SE and EE", 8);
            Skill skill3 = createSkill(testUser.getId(), "Database Design", "SQL and NoSQL database experience", 7);
            
            // Test finding skills by user ID
            testFindByUserId(testUser.getId());
            
            // Test updating a skill
            testUpdateSkill(skill2);
            
            // Test deleting a skill
            testDeleteSkill(skill3.getSkillId());
            
            // Test deleting all user skills
            testDeleteUserSkills(testUser.getId());
            
            // Clean up - remove test user
            userRepository.delete(testUser.getId());
            System.out.println("Deleted test user with ID: " + testUser.getId());
            
            System.out.println("All tests completed successfully!");
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
        user.setName("Test User");
        user.setEmail("skilltest_" + System.currentTimeMillis() + "@example.com");
        user.setPasswordHash("hashedpassword");
        user.setSalt("salt123");
        
        return userRepository.save(user);
    }
    
    /**
     * Tests skill creation.
     * 
     * @param userId The user ID to associate with the skill
     * @return The created skill
     * @throws SQLException If there's an error creating the skill
     */
    private static Skill testSkillCreation(Long userId) throws SQLException {
        Skill skill = new Skill();
        skill.setUserId(userId);
        skill.setSkillName("Web Development");
        skill.setDescription("Experience with HTML, CSS, and JavaScript");
        skill.setProficiencyLevel(9);
        
        Skill savedSkill = skillRepository.save(skill);
        
        // Verify skill was saved correctly
        assert savedSkill.getSkillId() != null : "Skill ID should not be null";
        assert savedSkill.getUserId().equals(userId) : "User ID does not match";
        assert savedSkill.getSkillName().equals("Web Development") : "Skill name does not match";
        assert savedSkill.getDescription().equals("Experience with HTML, CSS, and JavaScript") : "Description does not match";
        assert savedSkill.getProficiencyLevel() == 9 : "Proficiency level does not match";
        
        System.out.println("✅ Skill creation test passed");
        return savedSkill;
    }
    
    /**
     * Helper method to create a skill.
     * 
     * @param userId The user ID to associate with the skill
     * @param name The skill name
     * @param description The skill description
     * @param proficiency The proficiency level
     * @return The created skill
     * @throws SQLException If there's an error creating the skill
     */
    private static Skill createSkill(Long userId, String name, String description, int proficiency) throws SQLException {
        Skill skill = new Skill();
        skill.setUserId(userId);
        skill.setSkillName(name);
        skill.setDescription(description);
        skill.setProficiencyLevel(proficiency);
        
        return skillRepository.save(skill);
    }
    
    /**
     * Tests finding a skill by ID.
     * 
     * @param skillId The ID of the skill to find
     * @throws SQLException If there's an error finding the skill
     */
    private static void testFindById(Long skillId) throws SQLException {
        Optional<Skill> foundSkill = skillRepository.findById(skillId);
        
        assert foundSkill.isPresent() : "Skill should be found";
        assert foundSkill.get().getSkillId().equals(skillId) : "Skill ID does not match";
        
        System.out.println("✅ Find skill by ID test passed");
    }
    
    /**
     * Tests finding skills by user ID.
     * 
     * @param userId The ID of the user to find skills for
     * @throws SQLException If there's an error finding the skills
     */
    private static void testFindByUserId(Long userId) throws SQLException {
        List<Skill> userSkills = skillRepository.findByUserId(userId);
        
        assert userSkills != null : "Skills list should not be null";
        assert !userSkills.isEmpty() : "User should have skills";
        
        // All skills should belong to the user
        for (Skill skill : userSkills) {
            assert skill.getUserId().equals(userId) : "Skill does not belong to the correct user";
        }
        
        System.out.println("✅ Find skills by user ID test passed - found " + userSkills.size() + " skills");
    }
    
    /**
     * Tests updating a skill.
     * 
     * @param skill The skill to update
     * @throws SQLException If there's an error updating the skill
     */
    private static void testUpdateSkill(Skill skill) throws SQLException {
        // Change skill properties
        skill.setSkillName(skill.getSkillName() + " (Updated)");
        skill.setDescription(skill.getDescription() + " - Updated description");
        skill.setProficiencyLevel(skill.getProficiencyLevel() + 1 > 10 ? 10 : skill.getProficiencyLevel() + 1);
        
        boolean updated = skillRepository.update(skill);
        assert updated : "Skill update should succeed";
        
        // Verify the update
        Optional<Skill> updatedSkill = skillRepository.findById(skill.getSkillId());
        assert updatedSkill.isPresent() : "Updated skill should be found";
        assert updatedSkill.get().getSkillName().equals(skill.getSkillName()) : "Skill name was not updated";
        assert updatedSkill.get().getDescription().equals(skill.getDescription()) : "Skill description was not updated";
        assert updatedSkill.get().getProficiencyLevel() == skill.getProficiencyLevel() : "Skill proficiency level was not updated";
        
        System.out.println("✅ Update skill test passed");
    }
    
    /**
     * Tests deleting a skill.
     * 
     * @param skillId The ID of the skill to delete
     * @throws SQLException If there's an error deleting the skill
     */
    private static void testDeleteSkill(Long skillId) throws SQLException {
        boolean deleted = skillRepository.deleteById(skillId);
        assert deleted : "Skill deletion should succeed";
        
        // Verify the skill was deleted
        Optional<Skill> deletedSkill = skillRepository.findById(skillId);
        assert deletedSkill.isEmpty() : "Deleted skill should not be found";
        
        System.out.println("✅ Delete skill test passed");
    }
    
    /**
     * Tests deleting all skills for a user.
     * 
     * @param userId The ID of the user whose skills to delete
     * @throws SQLException If there's an error deleting the skills
     */
    private static void testDeleteUserSkills(Long userId) throws SQLException {
        // First verify the user has skills
        List<Skill> userSkillsBefore = skillRepository.findByUserId(userId);
        assert !userSkillsBefore.isEmpty() : "User should have skills before deletion";
        
        // Delete all user skills
        int deletedCount = skillRepository.deleteByUserId(userId);
        assert deletedCount > 0 : "Some skills should be deleted";
        
        // Verify skills were deleted
        List<Skill> userSkillsAfter = skillRepository.findByUserId(userId);
        assert userSkillsAfter.isEmpty() : "User should have no skills after deletion";
        
        System.out.println("✅ Delete user skills test passed - deleted " + deletedCount + " skills");
    }
}