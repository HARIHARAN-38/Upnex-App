package com.upnext.app.service;

import com.upnext.app.domain.Skill;
import com.upnext.app.domain.User;
import com.upnext.app.data.UserRepository;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests for the AuthService class focusing on skill integration.
 */
public class AuthServiceSkillTest {
    private static final AuthService authService = AuthService.getInstance();
    private static final UserRepository userRepository = UserRepository.getInstance();
    
    /**
     * Main method to run the tests.
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        try {
            System.out.println("Starting AuthService Skill Integration tests...");
            
            // Test registering a user with skills
            testRegisterUserWithSkills();
            
            // Test retrieving user with skills
            testGetUserWithSkills();
            
            System.out.println("All AuthService Skill Integration tests completed successfully!");
        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Tests registering a user with initial skills.
     * 
     * @throws AuthService.AuthException If there's an error during registration
     */
    private static void testRegisterUserWithSkills() throws AuthService.AuthException {
        String name = "Test User";
        String email = "skilltest_" + System.currentTimeMillis() + "@example.com";
        String password = "password123";
        
        // Create skills
        List<Skill> initialSkills = new ArrayList<>();
        
        Skill skill1 = new Skill();
        skill1.setSkillName("Java Programming");
        skill1.setDescription("Core Java development");
        skill1.setProficiencyLevel(8);
        initialSkills.add(skill1);
        
        Skill skill2 = new Skill();
        skill2.setSkillName("SQL Database");
        skill2.setDescription("SQL query optimization");
        skill2.setProficiencyLevel(7);
        initialSkills.add(skill2);
        
        // Register user with skills
        User registeredUser = authService.signUp(name, email, password, initialSkills);
        
        // Verify user registration
        assert registeredUser != null : "Registered user should not be null";
        assert registeredUser.getId() != null : "User ID should not be null";
        assert registeredUser.getName().equals(name) : "User name does not match";
        assert registeredUser.getEmail().equals(email) : "User email does not match";
        
        System.out.println("✅ Register user with skills test passed");
    }
    
    /**
     * Tests retrieving a user with their associated skills.
     * 
     * @throws AuthService.AuthException If there's an error during sign-in
     */
    private static void testGetUserWithSkills() throws AuthService.AuthException {
        // Create test user with skills
        String name = "Skill Test User";
        String email = "skilltest_get_" + System.currentTimeMillis() + "@example.com";
        String password = "password123";
        
        // Create skills
        List<Skill> initialSkills = new ArrayList<>();
        
        Skill skill1 = new Skill();
        skill1.setSkillName("Web Development");
        skill1.setDescription("HTML, CSS, JavaScript");
        skill1.setProficiencyLevel(9);
        initialSkills.add(skill1);
        
        Skill skill2 = new Skill();
        skill2.setSkillName("UI Design");
        skill2.setDescription("User interface design principles");
        skill2.setProficiencyLevel(8);
        initialSkills.add(skill2);
        
        // Register user with skills
        authService.signUp(name, email, password, initialSkills);
        
        // Sign in with the user
        User signedInUser = authService.signIn(email, password);
        
        // Verify user has skills
        List<Skill> userSkills = signedInUser.getSkills();
        assert userSkills != null : "User skills should not be null";
        assert userSkills.size() == initialSkills.size() : "User should have the correct number of skills";
        
        // Verify skills have correct data
        boolean foundWebDev = false;
        boolean foundUIDesign = false;
        
        for (Skill skill : userSkills) {
            if ("Web Development".equals(skill.getSkillName())) {
                foundWebDev = true;
                assert skill.getDescription().equals("HTML, CSS, JavaScript") : "Skill description does not match";
                assert skill.getProficiencyLevel() == 9 : "Skill proficiency level does not match";
            } else if ("UI Design".equals(skill.getSkillName())) {
                foundUIDesign = true;
                assert skill.getDescription().equals("User interface design principles") : "Skill description does not match";
                assert skill.getProficiencyLevel() == 8 : "Skill proficiency level does not match";
            }
        }
        
        assert foundWebDev : "Web Development skill not found";
        assert foundUIDesign : "UI Design skill not found";
        
        // Clean up - sign out
        authService.signOut();
        
        System.out.println("✅ Get user with skills test passed");
        
        // Cleanup the test users (would normally be in a tearDown method)
        try {
            System.out.println("Cleaning up test users...");
            List<User> users = userRepository.findAll();
            for (User user : users) {
                if (user.getEmail().startsWith("skilltest_")) {
                    userRepository.delete(user.getId());
                    System.out.println("Deleted test user: " + user.getEmail());
                }
            }
        } catch (SQLException e) {
            System.err.println("Error during cleanup: " + e.getMessage());
        }
    }
}