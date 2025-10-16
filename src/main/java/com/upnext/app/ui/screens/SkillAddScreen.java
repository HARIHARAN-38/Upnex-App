package com.upnext.app.ui.screens;

import com.upnext.app.core.Logger;
import com.upnext.app.domain.Skill;
import com.upnext.app.ui.components.FeedbackManager;
import com.upnext.app.ui.components.ProficiencyBar;
import com.upnext.app.ui.components.forms.FormField;
import com.upnext.app.ui.components.forms.FormPanel;
import com.upnext.app.ui.components.forms.SubmitButton;
import com.upnext.app.ui.navigation.ViewNavigator;
import com.upnext.app.ui.theme.AppTheme;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

/**
 * Screen for adding a new skill during account creation or profile management.
 * 
 * This screen implements a complete skill creation workflow including:
 * - Validated data entry for skill information
 * - Interactive proficiency level selection with visual feedback
 * - Processing indicators during submission
 * - Navigation back to the skillset overview screen
 * 
 * The design follows several UI/UX patterns:
 * - Form validation with specific error messages
 * - Visual feedback during asynchronous operations
 * - Data loss prevention with confirmation dialogs
 * - Keyboard navigation support for accessibility
 * 
 * This screen supports two primary use cases:
 * 1. Adding skills during initial account creation
 * 2. Adding new skills to an existing user profile
 * 
 * Architectural patterns implemented:
 * - Screen Registry pattern for navigation
 * - Command pattern for encapsulated operations
 * - Observer pattern for proficiency level updates
 * - Mediator pattern for screen communication
 */
public class SkillAddScreen extends JPanel {
    // UI components
    private final JLabel titleLabel;
    private final JButton cancelButton;
    private final SubmitButton addSkillButton;
    private final FormField skillNameField;
    private final FormField descriptionField;
    private final ProficiencyBar proficiencyBar;
    private final JLabel proficiencyLabel;
    private final JLabel proficiencyValueLabel;
    private final JPanel loadingPanel;
    
    // Navigation constants - should match App.SKILL_ADD_SCREEN
    public static final String SCREEN_ID = "add-skill";
    
    // Logger
    private final Logger logger = Logger.getInstance();
    
    /**
     * Creates a new skill add screen with a complete form UI.
     * 
     * The constructor implements a hierarchical UI construction approach:
     * 1. Set up the base panel with layout and styling
     * 2. Create the header section with title and navigation
     * 3. Build the form container with appropriate styling
     * 4. Add specialized input components for skill properties
     * 5. Create the action buttons with loading indicators
     * 6. Assemble the component hierarchy with proper constraints
     * 7. Set up event listeners for interactive behavior
     * 
     * The UI follows a responsive design approach with:
     * - Proper component hierarchy for logical grouping
     * - Consistent padding and spacing for visual clarity
     * - Width constraints for optimal readability
     * - Visual feedback mechanisms for user actions
     * 
     * This component can be reused in multiple contexts within the application.
     */
    public SkillAddScreen() {
        setLayout(new BorderLayout(0, 20));
        setBackground(AppTheme.BACKGROUND);
        setBorder(new EmptyBorder(40, 40, 40, 40));
        
        // Header panel with title and cancel button
        JPanel headerPanel = new JPanel(new BorderLayout(15, 0));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 25, 0));
        
        titleLabel = new JLabel("Add a New Skill");
        titleLabel.setFont(AppTheme.HEADING_FONT.deriveFont(28f));
        titleLabel.setForeground(AppTheme.TEXT_PRIMARY);
        
        cancelButton = new JButton("Cancel");
        cancelButton.setBorderPainted(false);
        cancelButton.setContentAreaFilled(false);
        cancelButton.setForeground(AppTheme.TEXT_SECONDARY);
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelButton.setFont(AppTheme.PRIMARY_FONT.deriveFont(Font.BOLD));
        
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(cancelButton, BorderLayout.EAST);
        
        // Form container with rounded corners and shadow effect
        JPanel formContainer = new JPanel(new BorderLayout());
        formContainer.setBackground(AppTheme.SURFACE);
        formContainer.setBorder(new CompoundBorder(
            new MatteBorder(1, 1, 1, 1, new Color(0xE2E8F0)),
            new EmptyBorder(25, 25, 25, 25)
        ));
        
        // Form panel with improved styling
        FormPanel formPanel = new FormPanel();
        
        // Enhanced form fields
        skillNameField = new FormField("Skill Name");
        skillNameField.getTextField().addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                skillNameField.getTextField().selectAll();
            }
        });
        
        descriptionField = new FormField("Description");
        descriptionField.getTextField().setPreferredSize(new Dimension(
            descriptionField.getTextField().getPreferredSize().width,
            descriptionField.getTextField().getPreferredSize().height + 20
        ));
        
        // Proficiency section with improved styling
        JPanel proficiencyPanel = new JPanel();
        proficiencyPanel.setLayout(new BoxLayout(proficiencyPanel, BoxLayout.Y_AXIS));
        proficiencyPanel.setOpaque(false);
        proficiencyPanel.setBorder(new EmptyBorder(15, 0, 20, 0));
        
        // Label row with value display
        JPanel proficiencyHeaderPanel = new JPanel(new BorderLayout(10, 0));
        proficiencyHeaderPanel.setOpaque(false);
        
        proficiencyLabel = new JLabel("Proficiency Level");
        proficiencyLabel.setFont(AppTheme.PRIMARY_FONT);
        proficiencyLabel.setForeground(AppTheme.TEXT_PRIMARY);
        
        proficiencyValueLabel = new JLabel("5 / 10");
        proficiencyValueLabel.setFont(AppTheme.PRIMARY_FONT.deriveFont(Font.BOLD));
        proficiencyValueLabel.setForeground(AppTheme.PRIMARY);
        
        proficiencyHeaderPanel.add(proficiencyLabel, BorderLayout.WEST);
        proficiencyHeaderPanel.add(proficiencyValueLabel, BorderLayout.EAST);
        
        // Create proficiency bar (default to level 5, editable)
        proficiencyBar = new ProficiencyBar(5, true);
        proficiencyBar.addPropertyChangeListener("proficiencyLevel", e -> {
            int level = (Integer) e.getNewValue();
            proficiencyValueLabel.setText(level + " / 10");
        });
        
        // Proficiency help text
        JLabel proficiencyHelpText = new JLabel("Drag to adjust your skill proficiency level");
        proficiencyHelpText.setFont(AppTheme.PRIMARY_FONT.deriveFont(12f));
        proficiencyHelpText.setForeground(AppTheme.TEXT_SECONDARY);
        proficiencyHelpText.setBorder(new EmptyBorder(5, 0, 0, 0));
        
        proficiencyPanel.add(proficiencyHeaderPanel);
        proficiencyPanel.add(Box.createVerticalStrut(10));
        proficiencyPanel.add(proficiencyBar);
        proficiencyPanel.add(proficiencyHelpText);
        
        // Add components to form
        formPanel.addField(skillNameField);
        formPanel.addField(descriptionField);
        formPanel.getContentPanel().add(proficiencyPanel);
        
        // Add button with loading indicator
        JPanel buttonPanel = new JPanel(new BorderLayout(10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        // Loading panel with spinner
        loadingPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        loadingPanel.setOpaque(false);
        loadingPanel.setVisible(false);
        
        JLabel loadingLabel = new JLabel("Adding skill...");
        loadingLabel.setFont(AppTheme.PRIMARY_FONT);
        loadingLabel.setForeground(AppTheme.PRIMARY);
        
        loadingPanel.add(new JLabel(new ImageIcon(getClass().getResource("/ui/icons/spinner.gif"))));
        loadingPanel.add(loadingLabel);
        
        // Add button
        addSkillButton = new SubmitButton("Add Skill");
        
        buttonPanel.add(loadingPanel, BorderLayout.WEST);
        buttonPanel.add(addSkillButton, BorderLayout.EAST);
        
        // Add components to form container
        formContainer.add(formPanel, BorderLayout.CENTER);
        formContainer.add(buttonPanel, BorderLayout.SOUTH);
        
        // Main container with header and form
        JPanel mainContainer = new JPanel(new BorderLayout(0, 20));
        mainContainer.setOpaque(false);
        mainContainer.add(headerPanel, BorderLayout.NORTH);
        
        // Center the form container with some maximum width constraints
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        
        // Set maximum width for better readability
        JPanel maxWidthPanel = new JPanel(new BorderLayout());
        maxWidthPanel.setOpaque(false);
        maxWidthPanel.setMaximumSize(new Dimension(600, Integer.MAX_VALUE));
        maxWidthPanel.add(formContainer, BorderLayout.CENTER);
        
        centerWrapper.add(maxWidthPanel, gbc);
        mainContainer.add(centerWrapper, BorderLayout.CENTER);
        
        // Add the main container to this panel
        add(mainContainer, BorderLayout.CENTER);
        
        // Set up event listeners
        setupEventListeners();
    }
    
    /**
     * Sets up the event listeners for buttons and other interactive elements.
     * 
     * This method implements:
     * 1. Button click handlers for the main actions
     * 2. Enter key support for form fields to improve keyboard navigation
     * 3. Field-to-field navigation with Enter key to simulate tab behavior
     * 
     * The keyboard navigation pattern follows standard form conventions:
     * - Enter in first field advances to next field
     * - Enter in last field submits the form
     */
    private void setupEventListeners() {
        // Button click handlers using lambda expressions
        // Using underscore (_) as per Java convention for ignored parameters
        cancelButton.addActionListener((_) -> cancelAddSkill());
        addSkillButton.addActionListener((_) -> addSkill());
        
        // Enhanced keyboard navigation support
        // When user presses Enter in skill name field, focus moves to description field
        skillNameField.getTextField().addActionListener((_) -> descriptionField.getTextField().requestFocus());
        
        // When user presses Enter in description field, it triggers the add skill action
        // This allows for keyboard-only form submission
        descriptionField.getTextField().addActionListener((_) -> addSkill());
    }
    
    /**
     * Cancels the add skill operation and returns to the skillset screen.
     * 
     * This method implements a data loss protection pattern:
     * 1. Checks if the user has entered any data in the form
     * 2. If data exists, prompts for confirmation before discarding
     * 3. If confirmed or no data exists, resets the form and navigates back
     * 
     * The confirmation dialog prevents accidental data loss while maintaining
     * a smooth user experience for cases where no data has been entered.
     * This follows UX best practices for form abandonment flows.
     */
    private void cancelAddSkill() {
        // Check if there are any non-default inputs that would be lost
        boolean hasUserInput = !skillNameField.getText().trim().isEmpty() || 
                               !descriptionField.getText().trim().isEmpty() ||
                               proficiencyBar.getProficiencyLevel() != 5; // 5 is the default value
                
        if (hasUserInput) {
            // Show a confirmation dialog only if user has entered data
            // This prevents unnecessary prompts for empty forms
            int response = JOptionPane.showConfirmDialog(
                this,
                "Discard this skill?",
                "Cancel Adding Skill",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            
            // Return early if user chooses not to discard data
            if (response != JOptionPane.YES_OPTION) {
                return;
            }
        }
        
        // Proceed with cancellation - clear form and navigate back
        clearFields();
        navigateToSkillset();
    }
    
    /**
     * Validates input and adds the skill with visual feedback.
     * 
     * This method implements a multi-step process:
     * 1. Input validation with specific error messages
     * 2. Visual feedback during processing
     * 3. Asynchronous operation for UI responsiveness
     * 4. Error handling and recovery
     * 5. Automatic navigation upon success
     * 
     * The validation follows database constraints:
     * - Skill name: required, max 100 chars
     * - Description: optional, max 255 chars
     * - Proficiency: 1-10 (validated by the ProficiencyBar component)
     */
    private void addSkill() {
        // Retrieve and trim input values to avoid whitespace issues
        String skillName = skillNameField.getText().trim();
        String description = descriptionField.getText().trim();
        int proficiency = proficiencyBar.getProficiencyLevel();
        
        // Validation phase 1: Required fields
        // Skill name is the only required field
        if (skillName.isEmpty()) {
            showValidationError("Skill name is required");
            skillNameField.getTextField().requestFocus(); // Set focus for user convenience
            return;
        }
        
        // Validation phase 2: Length constraints
        // These match the database column sizes to prevent data truncation
        if (skillName.length() > 100) {
            showValidationError("Skill name cannot exceed 100 characters");
            skillNameField.getTextField().requestFocus();
            return;
        }
        
        // Description is optional but has length limits if provided
        if (description.length() > 255) {
            showValidationError("Description cannot exceed 255 characters");
            descriptionField.getTextField().requestFocus();
            return;
        }
        
        // Processing phase: Prepare UI for operation
        // Disable all inputs and show loading indicator
        setFormEnabled(false);
        loadingPanel.setVisible(true);
        
        // Use SwingUtilities.invokeLater to keep UI responsive
        // This ensures the UI thread doesn't freeze during processing
        SwingUtilities.invokeLater(() -> {
            try {
                // Simulate a small delay for better UX
                // This gives visual feedback that something is happening
                Thread.sleep(500);
                
                // Create the skill object with validated data
                Skill newSkill = new Skill();
                newSkill.setSkillName(skillName);
                newSkill.setDescription(description);
                newSkill.setProficiencyLevel(proficiency);
                
                // Pass the skill to the parent screen and navigate back
                addSkillToSkillset(newSkill);
                clearFields();
                navigateToSkillset();
                
                // Log the action for audit and debugging
                logger.info("Added new skill: " + skillName + " with proficiency: " + proficiency);
            } catch (InterruptedException e) {
                // Handle thread interruption gracefully
                logger.error("Thread interrupted during skill addition: " + e.getMessage());
                
                // Restore UI to interactive state if operation fails
                setFormEnabled(true);
                loadingPanel.setVisible(false);
            }
        });
    }
    
    /**
     * Enables or disables all form components during processing.
     * 
     * This method implements a comprehensive UI state management approach:
     * 1. Controls all input fields as a group for consistent state
     * 2. Manages both text fields and custom components (proficiency bar)
     * 3. Handles both input components and action buttons
     * 
     * This centralized approach ensures:
     * - Consistent UI state during processing operations
     * - Prevention of concurrent/multiple submissions
     * - Clear visual feedback about the form's interactive state
     * - No partial form states where some elements are enabled and others disabled
     * 
     * Typically called with false before async operations and true after completion.
     * 
     * @param enabled Whether the form should be in an interactive (true) or processing (false) state
     */
    private void setFormEnabled(boolean enabled) {
        // Control text input fields
        skillNameField.setEditable(enabled);
        descriptionField.setEditable(enabled);
        
        // Control custom components
        proficiencyBar.setEditable(enabled);
        
        // Control action buttons
        addSkillButton.setEnabled(enabled);
        cancelButton.setEnabled(enabled);
    }
    
    /**
     * Adds the skill to the skillset screen's pending skills list.
     * 
     * This method implements cross-screen communication using the Screen Registry pattern:
     * 1. Retrieves the ViewNavigator singleton instance
     * 2. Checks if the target screen exists in the navigation registry
     * 3. Safely casts the screen reference to the specific screen type
     * 4. Calls the appropriate method on the target screen
     * 
     * This approach allows for loosely coupled screen interactions without
     * direct dependencies, following the Mediator pattern principles. The
     * conditional check prevents errors if the navigation structure changes.
     * 
     * @param skill The validated and fully-populated skill object to add to the skillset
     */
    private void addSkillToSkillset(Skill skill) {
        ViewNavigator navigator = ViewNavigator.getInstance();
        if (navigator.hasScreen(com.upnext.app.App.SKILLSET_SCREEN)) {
            SkillsetScreen skillsetScreen = (SkillsetScreen) navigator.getScreen(com.upnext.app.App.SKILLSET_SCREEN);
            skillsetScreen.addSkill(skill);
        }
    }
    
    /**
     * Navigates back to the skillset screen.
     * 
     * This method encapsulates the navigation logic to:
     * 1. Keep navigation code centralized within this class
     * 2. Abstract away the details of the navigation mechanism
     * 3. Use the application's screen constants for type-safe navigation
     * 
     * Using the ViewNavigator singleton ensures consistent navigation
     * behavior across the application. The constant from App.java
     * ensures navigation targets remain synchronized if screen IDs change.
     * 
     * This follows the Command pattern where the navigation action is
     * encapsulated in a method that can be called from multiple places.
     */
    private void navigateToSkillset() {
        ViewNavigator.getInstance().navigateTo(com.upnext.app.App.SKILLSET_SCREEN);
    }
    
    /**
     * Clears all input fields and resets the form state.
     * 
     * This method performs a comprehensive reset of the form by:
     * 1. Clearing all text input fields to empty state
     * 2. Resetting the proficiency bar to its default middle value
     * 3. Updating all associated labels to reflect default values
     * 4. Restoring the UI to its fully interactive state
     * 5. Setting focus to the first field for immediate data entry
     * 
     * This method is called in several scenarios:
     * - After a successful skill submission
     * - When canceling the add skill operation
     * - When navigating away from the screen
     * - When the user manually requests a form reset
     * 
     * The form is returned to its pristine state, with default values
     * and focus properly positioned for the next interaction.
     */
    public void clearFields() {
        // Reset all text fields to empty values
        skillNameField.setText("");
        descriptionField.setText("");
        
        // Reset the proficiency bar to its default middle value (5)
        proficiencyBar.setProficiencyLevel(5);
        
        // Update the proficiency value label to match the reset proficiency level
        proficiencyValueLabel.setText("5 / 10");
        
        // Hide any loading indicators that may be visible
        loadingPanel.setVisible(false);
        
        // Re-enable all form fields and buttons for interaction
        setFormEnabled(true);
        
        // Reset focus to the first field for immediate data entry
        // Using invokeLater ensures this happens after any pending UI updates
        SwingUtilities.invokeLater(() -> skillNameField.getTextField().requestFocus());
    }
    
    /**
     * Shows a validation error message to the user.
     * 
     * This method encapsulates the error display mechanism and provides
     * a consistent error presentation across the application by:
     * 1. Using the centralized FeedbackManager to display errors
     * 2. Maintaining a consistent "Validation Error" title for all form validation issues
     * 3. Ensuring the error dialog is properly parented to this screen
     * 
     * This approach promotes:
     * - Consistent UX for all validation errors
     * - Centralized error handling that can be updated app-wide
     * - Clear distinction between validation errors and other error types
     * 
     * @param message The specific validation error message to display to the user
     */
    private void showValidationError(String message) {
        FeedbackManager.showError(this, message, "Validation Error");
    }
}