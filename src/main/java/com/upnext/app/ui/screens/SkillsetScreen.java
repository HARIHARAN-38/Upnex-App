package com.upnext.app.ui.screens;

import com.upnext.app.core.Logger;
import com.upnext.app.domain.Skill;
import com.upnext.app.ui.components.SkillCardPanel;
import com.upnext.app.ui.components.forms.SubmitButton;
import com.upnext.app.ui.navigation.ViewNavigator;
import com.upnext.app.ui.theme.AppTheme;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

/**
 * Screen for managing and displaying user skills during account creation.
 * Allows users to add, remove, and view their skills.
 */
public class SkillsetScreen extends JPanel {
    // UI components
    private final JLabel titleLabel;
    private final JButton backButton;
    private final JButton addSkillButton;
    private final SubmitButton createAccountButton;
    private final SkillCardPanel skillCardPanel;
    private final JLabel instructionLabel;
    private final JLabel skillCountLabel;
    private final JPanel loadingOverlay;
    
    // Data storage for pending skills
    private final List<Skill> pendingSkills;
    
    // User registration data
    private String userName;
    private String userEmail;
    private String userPassword;
    
    // Navigation constants - should match App.SKILLSET_SCREEN
    public static final String SCREEN_ID = "skillset";
    
    // Logger
    private final Logger logger = Logger.getInstance();
    
    /**
     * Creates a new skillset screen.
     */
    public SkillsetScreen() {
        setLayout(new BorderLayout(0, 20));
        setBackground(AppTheme.BACKGROUND);
        setBorder(new EmptyBorder(40, 40, 40, 40));
        
        // Initialize pending skills list
        pendingSkills = new ArrayList<>();
        
        // Header panel with title and back button
        JPanel headerPanel = new JPanel(new BorderLayout(15, 0));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 25, 0));
        
        titleLabel = new JLabel("Add Your Skills");
        titleLabel.setFont(AppTheme.HEADING_FONT.deriveFont(28f));
        titleLabel.setForeground(AppTheme.TEXT_PRIMARY);
        
        backButton = new JButton("← Back");
        backButton.setBorderPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.setForeground(AppTheme.PRIMARY);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.setFont(AppTheme.PRIMARY_FONT.deriveFont(Font.BOLD));
        
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(backButton, BorderLayout.WEST);
        
        // Instruction container with count
        JPanel instructionContainer = new JPanel(new BorderLayout(10, 0));
        instructionContainer.setOpaque(false);
        instructionContainer.setBorder(new EmptyBorder(0, 0, 15, 0));
        
        instructionLabel = new JLabel("Add at least one skill to continue");
        instructionLabel.setFont(AppTheme.PRIMARY_FONT);
        instructionLabel.setForeground(AppTheme.TEXT_SECONDARY);
        
        skillCountLabel = new JLabel("0 skills added");
        skillCountLabel.setFont(AppTheme.PRIMARY_FONT.deriveFont(Font.BOLD));
        skillCountLabel.setForeground(AppTheme.PRIMARY);
        
        instructionContainer.add(instructionLabel, BorderLayout.WEST);
        instructionContainer.add(skillCountLabel, BorderLayout.EAST);
        
        // Content panel with rounded surface background
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(AppTheme.SURFACE);
        contentPanel.setBorder(new CompoundBorder(
            new MatteBorder(1, 1, 1, 1, new Color(0xE2E8F0)),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        // Skill cards panel with improved styling
        skillCardPanel = new SkillCardPanel();
        skillCardPanel.setDeleteCallback(this::handleSkillDelete);
        
        contentPanel.add(skillCardPanel, BorderLayout.CENTER);
        
        // Loading overlay for visual feedback during operations
        loadingOverlay = new JPanel(new GridBagLayout());
        loadingOverlay.setBackground(new Color(255, 255, 255, 200));
        loadingOverlay.setVisible(false);
        
        JLabel loadingLabel = new JLabel("Processing...");
        loadingLabel.setFont(AppTheme.PRIMARY_FONT);
        loadingLabel.setForeground(AppTheme.PRIMARY);
        
        loadingOverlay.add(loadingLabel);
        
        // Button panel with improved styling
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(25, 0, 0, 0));
        
        addSkillButton = new JButton("Add New Skill");
        addSkillButton.setFont(AppTheme.PRIMARY_FONT);
        addSkillButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addSkillButton.setFocusPainted(false);
        addSkillButton.setBackground(AppTheme.SURFACE);
        addSkillButton.setForeground(AppTheme.PRIMARY);
        addSkillButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppTheme.PRIMARY),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        
        createAccountButton = new SubmitButton("Create Account");
        createAccountButton.setEnabled(false); // Disabled initially
        
        buttonPanel.add(addSkillButton);
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(createAccountButton);
        
        // Main layout using layered pane for loading overlay
        JLayeredPane layeredPane = new JLayeredPane() {
            @Override
            public Dimension getPreferredSize() {
                return contentPanel.getPreferredSize();
            }
            
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                contentPanel.setBounds(0, 0, getWidth(), getHeight());
                loadingOverlay.setBounds(0, 0, getWidth(), getHeight());
            }
        };
        
        layeredPane.add(contentPanel, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(loadingOverlay, JLayeredPane.MODAL_LAYER);
        
        // Main container
        JPanel mainContainer = new JPanel(new BorderLayout(0, 0));
        mainContainer.setOpaque(false);
        mainContainer.add(headerPanel, BorderLayout.NORTH);
        mainContainer.add(instructionContainer, BorderLayout.CENTER);
        
        // Add components to main layout
        add(mainContainer, BorderLayout.NORTH);
        add(layeredPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Set up event listeners
        setupEventListeners();
    }
    
    /**
     * Sets up the event listeners for buttons and other interactive elements.
     * Uses lambda expressions for concise event handling.
     * The double underscore parameter is used as a convention for ignored parameters.
     */
    private void setupEventListeners() {
        // Note: The '__' parameter is intentionally unused - it's a common convention 
        // for lambda parameters that aren't referenced in the method body
        backButton.addActionListener(__ -> navigateBack());
        addSkillButton.addActionListener(__ -> navigateToAddSkill());
        createAccountButton.addActionListener(__ -> finalizeRegistration());
    }
    
    /**
     * Navigates back to the create account screen.
     * Shows a confirmation dialog if skills have been added to prevent accidental data loss.
     * 
     * The flow:
     * 1. Check if user has added any skills
     * 2. If yes, show confirmation dialog to prevent data loss
     * 3. If user confirms or if no skills were added, navigate back
     */
    private void navigateBack() {
        // Check if there are any pending skills that would be lost
        if (!pendingSkills.isEmpty()) {
            // Use YES_NO_OPTION to force explicit user choice
            int response = JOptionPane.showConfirmDialog(
                this,
                "Going back will lose your added skills. Continue?",
                "Confirm Navigation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            
            if (response == JOptionPane.YES_OPTION) {
                // Only clear skills and navigate if user confirms
                clearSkills();
                ViewNavigator.getInstance().navigateTo(com.upnext.app.App.CREATE_ACCOUNT_SCREEN);
            }
            // If NO selected, do nothing and stay on current screen
        } else {
            // No skills to lose, so navigate directly
            ViewNavigator.getInstance().navigateTo(com.upnext.app.App.CREATE_ACCOUNT_SCREEN);
        }
    }
    
    /**
     * Navigates to the add skill screen.
     * Called when the "Add New Skill" button is clicked.
     */
    private void navigateToAddSkill() {
        // Reset any previous skills in the form by clearing the fields
        SkillAddScreen addSkillScreen = (SkillAddScreen) ViewNavigator.getInstance()
            .getScreen(com.upnext.app.App.SKILL_ADD_SCREEN);
        
        if (addSkillScreen != null) {
            addSkillScreen.clearFields();
        }
        
        // Navigate to the add skill screen
        ViewNavigator.getInstance().navigateTo(com.upnext.app.App.SKILL_ADD_SCREEN);
        logger.info("Navigating to add skill screen");
    }
    
    /**
     * Finalizes the user registration with the collected skills.
     * Note: Actual implementation is handled in App.java to centralize 
     * authentication and navigation logic.
     */
    private void finalizeRegistration() {
        // Validation is done in App.java before creating the account
        logger.info("Finalizing registration with " + pendingSkills.size() + " skills");
        
        // Check if there are any skills added
        if (pendingSkills.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "Please add at least one skill to continue.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        // The actual account creation happens in App.java through the button's action listener
    }
    
    /**
     * Handles the deletion of a skill.
     * 
     * @param skill The skill to delete
     */
    private void handleSkillDelete(Skill skill) {
        pendingSkills.remove(skill);
        updateCreateButtonState();
    }
    
    /**
     * Adds a new skill to the list with visual feedback.
     * 
     * This method uses a multi-threaded approach for UI responsiveness:
     * 1. Shows a loading overlay immediately
     * 2. Processes the skill addition in a separate event dispatch thread
     * 3. Adds a small artificial delay for better UX (makes the loading state visible)
     * 4. Updates the UI and hides the loading overlay when complete
     * 
     * @param skill The skill to add
     */
    public void addSkill(Skill skill) {
        // Show loading state briefly for visual feedback
        showLoading(true);
        
        // Use SwingUtilities.invokeLater to ensure UI operations happen on the EDT
        // This prevents UI freezing during the operation
        SwingUtilities.invokeLater(() -> {
            try {
                // Add a small delay to show the loading state
                // This gives users visual feedback that something is happening
                // A 300ms delay is short enough to not be annoying but long enough to be perceptible
                Thread.sleep(300);
                
                // Now perform the actual skill addition operations
                pendingSkills.add(skill);
                skillCardPanel.addSkill(skill);
                updateCreateButtonState();
                
                // Hide loading state when done
                showLoading(false);
            } catch (InterruptedException e) {
                // Log the error but don't crash the application
                logger.error("Thread interrupted during skill addition: " + e.getMessage());
                
                // Make sure we always hide the loading overlay, even if an error occurs
                // This prevents the UI from being permanently stuck in a loading state
                showLoading(false);
            }
        });
    }
    
    /**
     * Shows or hides the loading overlay.
     * 
     * The loading overlay is a semi-transparent panel that covers the entire content area
     * to indicate that an operation is in progress. This prevents users from interacting
     * with the UI during operations and provides visual feedback.
     * 
     * @param show Whether to show or hide the loading overlay
     */
    private void showLoading(boolean show) {
        loadingOverlay.setVisible(show);
        repaint(); // Force immediate visual update
    }
    
    /**
     * Updates the state of the create account button based on whether there are skills.
     * Also updates the skill count label and instruction text.
     * 
     * This method handles several related UI updates in a single call:
     * 1. Enables/disables the create account button based on skill count
     * 2. Updates the skill count label with proper pluralization
     * 3. Updates the instruction text color and message based on state
     * 
     * Having these updates in a single method ensures UI consistency and reduces code duplication,
     * as this needs to happen after any operation that changes the skill list.
     */
    private void updateCreateButtonState() {
        boolean hasSkills = !pendingSkills.isEmpty();
        createAccountButton.setEnabled(hasSkills);
        
        // Update skill count label with proper pluralization
        // The ternary operator (count != 1 ? "s" : "") adds an 's' for plural
        int count = pendingSkills.size();
        skillCountLabel.setText(count + " skill" + (count != 1 ? "s" : "") + " added");
        
        // Change instruction text and color based on skill count
        // This provides contextual feedback to the user about their current state
        if (hasSkills) {
            // Green success text when skills have been added
            instructionLabel.setText("Your skills have been added");
            instructionLabel.setForeground(new Color(0x047857)); // Success green color
        } else {
            // Standard prompt when no skills have been added yet
            instructionLabel.setText("Add at least one skill to continue");
            instructionLabel.setForeground(AppTheme.TEXT_SECONDARY);
        }
    }
    
    /**
     * Sets the user registration data to be used when finalizing registration.
     * 
     * @param name The user's name
     * @param email The user's email
     * @param password The user's password
     */
    public void setUserData(String name, String email, String password) {
        this.userName = name;
        this.userEmail = email;
        this.userPassword = password;
    }
    
    /**
     * Gets the user's name.
     * 
     * @return The user's name
     */
    public String getUserName() {
        return userName;
    }
    
    /**
     * Gets the user's email.
     * 
     * @return The user's email
     */
    public String getUserEmail() {
        return userEmail;
    }
    
    /**
     * Gets the user's password.
     * 
     * @return The user's password
     */
    public String getUserPassword() {
        return userPassword;
    }
    
    /**
     * Gets the back button.
     * 
     * @return The back button
     */
    public JButton getBackButton() {
        return backButton;
    }
    
    /**
     * Gets the add skill button.
     * 
     * @return The add skill button
     */
    public JButton getAddSkillButton() {
        return addSkillButton;
    }
    
    /**
     * Gets the create account button.
     * 
     * @return The create account button
     */
    public SubmitButton getCreateAccountButton() {
        return createAccountButton;
    }
    
    /**
     * Gets the list of pending skills.
     * 
     * @return The list of skills
     */
    public List<Skill> getPendingSkills() {
        return new ArrayList<>(pendingSkills);
    }
    
    /**
     * Clears the list of pending skills.
     */
    public void clearSkills() {
        pendingSkills.clear();
        skillCardPanel.clear();
        updateCreateButtonState();
    }
}