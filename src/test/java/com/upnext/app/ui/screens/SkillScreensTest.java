package com.upnext.app.ui.screens;

import java.lang.reflect.Method;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.upnext.app.domain.Skill;
import com.upnext.app.ui.navigation.ViewNavigator;

/**
 * Manual UI test for the SkillsetScreen and SkillAddScreen.
 */
public class SkillScreensTest {
    private static JFrame testFrame;
    private static ViewNavigator navigator;
    private static SkillsetScreen skillsetScreen;
    private static SkillAddScreen skillAddScreen;
    
    /**
     * Main method to run the tests.
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                setup();
                runTests();
            } catch (Exception e) {
                System.err.println("Test failed: " + e.getMessage());
                logStackTrace(e);
            }
        });
    }
    
    private static void logStackTrace(Exception exception) {
        System.err.println("Stack trace:");
        for (StackTraceElement element : exception.getStackTrace()) {
            System.err.println("    at " + element);
        }
    }
    /**
     * Sets up the test environment.
     */
    private static void setup() {
        System.out.println("Setting up test environment...");
        
        testFrame = new JFrame("Skills UI Test");
        testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        testFrame.setSize(800, 600);
        
        JPanel contentPanel = new JPanel();
        testFrame.add(contentPanel);
        
        // Initialize navigation
        navigator = ViewNavigator.initialize(contentPanel);
        
        // Create screens
        skillsetScreen = new SkillsetScreen();
        skillAddScreen = new SkillAddScreen();
        
        // Register screens with navigator
        navigator.registerScreen(SkillsetScreen.SCREEN_ID, skillsetScreen);
        navigator.registerScreen(SkillAddScreen.SCREEN_ID, skillAddScreen);
        
        // Setup test data
        skillsetScreen.setUserData("Test User", "test@example.com", "password");
        
        testFrame.setVisible(true);
        System.out.println("Test environment set up successfully");
    }
    
    /**
     * Runs the UI tests.
     */
    private static void runTests() {
        try {
            System.out.println("\nRunning UI tests...\n");
            
            // Test 1: Display skillset screen
            testDisplaySkillsetScreen();
            
            // Test 2: Add skill functionality
            testAddSkill("Java Programming", "Core Java and frameworks", 8);
            
            // Test 3: Add another skill
            testAddSkill("Web Development", "HTML, CSS, JavaScript", 9);
            
            // Test 4: Verify skills count
            testVerifySkillsCount(2);
            
            // Test 5: Delete a skill
            testDeleteSkill();
            
            // Test 6: Verify skills count after deletion
            testVerifySkillsCount(1);
            
            System.out.println("\nAll UI tests completed successfully!");
            
            // Keep the window open for manual inspection
            System.out.println("\nTest window will remain open for inspection. Close the window to exit.");
        } catch (Exception e) {
            System.err.println("Test execution failed: " + e.getMessage());
            logStackTrace(e);
        }
    }
    
    /**
     * Tests displaying the skillset screen.
     */
    private static void testDisplaySkillsetScreen() {
        System.out.println("Test: Display skillset screen");
        navigator.navigateTo("skillset");
        
        // Verify user data is set
        boolean hasUserData = skillsetScreen.getUserName() != null && 
                             !skillsetScreen.getUserName().isEmpty() &&
                              skillsetScreen.getUserEmail() != null && 
                             !skillsetScreen.getUserEmail().isEmpty();
        
        if (hasUserData) {
            System.out.println("✅ User data is set correctly");
        } else {
            System.err.println("❌ User data is not set correctly");
            throw new AssertionError("User data is not set correctly");
        }
        
        // Verify initial state of create button (should be disabled with no skills)
        boolean isCreateButtonDisabled = !skillsetScreen.getCreateAccountButton().isEnabled();
        
        if (isCreateButtonDisabled) {
            System.out.println("✅ Create account button is initially disabled as expected");
        } else {
            System.err.println("❌ Create account button should be disabled initially");
            throw new AssertionError("Create account button should be disabled initially");
        }
        
        System.out.println("✅ Skillset screen displayed successfully");
        
        // Pause for visual inspection
        sleep(1000);
    }
    
    /**
     * Tests adding a skill.
     * 
     * @param name The skill name
     * @param description The skill description
     * @param proficiency The proficiency level
     */
    private static void testAddSkill(String name, String description, int proficiency) {
        System.out.println("Test: Add skill - " + name);
        
        Skill newSkill = new Skill();
        newSkill.setSkillName(name);
        newSkill.setDescription(description);
        newSkill.setProficiencyLevel(proficiency);

        skillsetScreen.addSkill(newSkill);

        // Allow the EDT to process the addition
        sleep(800);

        // Verify we're back on skillset screen
        if (SkillsetScreen.SCREEN_ID.equals(navigator.getCurrentScreen())) {
            System.out.println("✅ Successfully returned to skillset screen");
        } else {
            System.err.println("❌ Did not return to skillset screen");
            throw new AssertionError("Did not return to skillset screen");
        }
        
        // Verify create button is enabled now that we have a skill
        boolean isCreateButtonEnabled = skillsetScreen.getCreateAccountButton().isEnabled();
        
        if (isCreateButtonEnabled) {
            System.out.println("✅ Create account button is enabled after adding a skill");
        } else {
            System.err.println("❌ Create account button should be enabled after adding a skill");
            throw new AssertionError("Create account button should be enabled after adding a skill");
        }
        
        System.out.println("✅ Skill added successfully");
    }
    
    /**
     * Tests verifying the number of skills.
     * 
     * @param expectedCount The expected number of skills
     */
    private static void testVerifySkillsCount(int expectedCount) {
        System.out.println("Test: Verify skills count - Expected: " + expectedCount);
        
        List<Skill> skills = skillsetScreen.getPendingSkills();
        int actualCount = skills.size();
        
        if (actualCount == expectedCount) {
            System.out.println("✅ Skills count matches expected: " + actualCount);
        } else {
            System.err.println("❌ Skills count does not match. Expected: " + expectedCount + ", Actual: " + actualCount);
            throw new AssertionError("Skills count does not match. Expected: " + expectedCount + ", Actual: " + actualCount);
        }
    }
    
    /**
     * Tests deleting a skill.
     */
    private static void testDeleteSkill() {
        System.out.println("Test: Delete a skill");
        
        // Get initial count
        int initialCount = skillsetScreen.getPendingSkills().size();
        
        // This would normally use the SkillCardPanel delete button
        // Since we can't easily access that in this test, we'll simulate deletion
        if (initialCount > 0) {
            Skill skillToDelete = skillsetScreen.getPendingSkills().get(0);
            try {
                Method deleteMethod = SkillsetScreen.class.getDeclaredMethod("handleSkillDelete", Skill.class);
                deleteMethod.setAccessible(true);
                deleteMethod.invoke(skillsetScreen, skillToDelete);
            } catch (ReflectiveOperationException reflectionError) {
                throw new AssertionError("Failed to invoke handleSkillDelete", reflectionError);
            }
            
            System.out.println("✅ Skill deleted successfully");
        } else {
            System.err.println("❌ No skills available to delete");
            throw new AssertionError("No skills available to delete");
        }
    }
    
    /**
     * Helper method to pause execution for visual inspection.
     * 
     * @param milliseconds The time to pause in milliseconds
     */
    private static void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}