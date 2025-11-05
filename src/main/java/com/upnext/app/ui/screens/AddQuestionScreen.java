package com.upnext.app.ui.screens;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import com.upnext.app.core.Logger;
import com.upnext.app.domain.question.Tag;
import com.upnext.app.ui.components.HeroBar;
import com.upnext.app.ui.components.forms.FormField;
import com.upnext.app.ui.components.questions.TagChipList;
import com.upnext.app.ui.components.questions.TagInputField;
import com.upnext.app.ui.navigation.ViewNavigator;
import com.upnext.app.ui.theme.AppTheme;
import com.upnext.app.ui.viewmodel.AddQuestionViewModel;

/**
 * Screen for adding new questions to the UpNext application.
 * Features a Hero bar alignment, gradient background, and SURFACE card layout
 * with tag input system and form validation.
 */
public class AddQuestionScreen extends JPanel {
    // Layout constants
    private static final int CARD_WIDTH = 800;
    private static final int CARD_MAX_HEIGHT = 700;
    private static final int PADDING_LARGE = 24;
    private static final int PADDING_MEDIUM = 16;
    private static final int PADDING_SMALL = 8;
    private static final int COMPONENT_SPACING = 12;
    private static final String SUBMIT_BUTTON_LABEL = "Add Question";
    
    // UI Components
    private final HeroBar heroBar;
    private final JPanel cardPanel;
    private final FormField titleField;
    private final JTextArea descriptionArea;
    private final JScrollPane descriptionScrollPane;
    private final com.upnext.app.ui.components.forms.SubjectComboBox subjectComboBox;
    private final TagInputField tagInputField;
    private final TagChipList tagChipList;
    private final JButton submitButton;
    private final JButton cancelButton;
    private final JLabel validationLabel;
    
    // ViewModel for state management and business logic
    private final AddQuestionViewModel viewModel;
    
    // Navigation callbacks
    private Runnable onNavigateBack;
    private java.util.function.Consumer<com.upnext.app.domain.question.Question> onQuestionCreated;
    
    /**
     * Creates a new AddQuestionScreen with proper layout and theming.
     */
    public AddQuestionScreen() {
    // Initialize ViewModel (listeners configured after UI components are ready)
    viewModel = new AddQuestionViewModel();
        
        // Main layout setup with gradient background
        setLayout(new BorderLayout());
        setBackground(AppTheme.BACKGROUND);
        
        // Create Hero bar
        heroBar = new HeroBar();
        
        // Create main card panel
        cardPanel = createCardPanel();
        
        // Initialize form components
        titleField = new FormField("Question Title", 30);
        descriptionArea = createDescriptionArea();
        descriptionScrollPane = createDescriptionScrollPane();
        subjectComboBox = new com.upnext.app.ui.components.forms.SubjectComboBox("Subject");
        subjectComboBox.setOnSelectionChanged(viewModel::setSelectedSubjectId);
        tagInputField = new TagInputField(viewModel::addTag);
        tagChipList = new TagChipList(viewModel::removeTag);
    tagChipList.setBorder(new EmptyBorder(PADDING_SMALL, 0, 0, 0));
    tagChipList.setVisible(false);

    // Connect ViewModel listeners now that UI components exist
    setupViewModelListeners();
        
        // Create buttons
        submitButton = createSubmitButton();
        cancelButton = createCancelButton();
        
        // Create validation label
        validationLabel = createValidationLabel();
        
        // Layout the card content
        layoutCardContent();
        
        // Create centering panel for the card
        JPanel centeringPanel = createCenteringPanel();
        
        // Add components to main layout
        add(heroBar, BorderLayout.NORTH);
        add(centeringPanel, BorderLayout.CENTER);
        
        // Style the components
        applyThemeStyles();
    }
    
    /**
     * Sets up listeners for ViewModel events.
     */
    private void setupViewModelListeners() {
        // Update tag chip list when tags change
        viewModel.setOnTagsChanged(tags -> {
            tagChipList.clearTags();
            for (Tag tag : tags) {
                tagChipList.addTag(tag);
            }
            // Always keep the tag chip list visible to maintain layout consistency
            tagChipList.setVisible(true);
            tagChipList.revalidate();
            tagChipList.repaint();
        });
        
        // Show validation errors
        viewModel.setOnValidationError(this::showValidationMessage);
        
        // Clear validation messages
        viewModel.setOnValidationCleared(message -> clearValidationMessage());
        
        // Handle successful question creation
        viewModel.setOnQuestionCreated(question -> {
            Logger.getInstance().info("Question created successfully with ID: " + question.getId());
            resetSubmissionState();
            clearForm();
            if (onQuestionCreated != null) {
                onQuestionCreated.accept(question);
            } else {
                // Fallback to direct navigation if no callback is set
                ViewNavigator.getInstance().navigateTo("home");
            }
        });
        
        // Handle failed question creation
        viewModel.setOnQuestionCreateFailed(() -> {
            // Error message is already shown via validation error listener
            // Additional failure handling could go here
            resetSubmissionState();
        });
    }
    
    /**
     * Creates the main card panel with SURFACE background and rounded borders.
     */
    private JPanel createCardPanel() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(AppTheme.SURFACE);
        card.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(new Color(0xE2E8F0), 1),
            new EmptyBorder(PADDING_LARGE, PADDING_LARGE, PADDING_LARGE, PADDING_LARGE)
        ));
        card.setPreferredSize(new Dimension(CARD_WIDTH, CARD_MAX_HEIGHT));
        return card;
    }
    
    /**
     * Creates the description text area with proper styling.
     */
    private JTextArea createDescriptionArea() {
        JTextArea area = new JTextArea(6, 50); // Increased rows and columns for better size
        area.setFont(AppTheme.PRIMARY_FONT);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        // Use consistent padding with title field (8px padding like FormField margin)
        area.setBorder(new EmptyBorder(12, 12, 12, 12)); // Increased padding for better text visibility
        area.setBackground(Color.WHITE);
        area.setForeground(AppTheme.TEXT_PRIMARY); // Ensure text color is visible
        area.setCaretColor(AppTheme.TEXT_PRIMARY); // Ensure cursor is visible
        area.setSelectionColor(AppTheme.PRIMARY.brighter()); // Set selection color
        return area;
    }
    
    /**
     * Creates the scroll pane for the description area.
     */
    private JScrollPane createDescriptionScrollPane() {
        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        // Use consistent border styling with title field (matches SkillAddScreen FormField style)
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(0xD1D5DB), 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        // Set proper size for multi-line description - much larger than before
        scrollPane.setPreferredSize(new Dimension(CARD_WIDTH - (PADDING_LARGE * 2), 180)); // Increased from 120 to 180
        scrollPane.setMinimumSize(new Dimension(CARD_WIDTH - (PADDING_LARGE * 2), 150)); // Added minimum size
        return scrollPane;
    }
    
    /**
     * Creates the submit button with primary styling.
     */
    private JButton createSubmitButton() {
        JButton button = new JButton(SUBMIT_BUTTON_LABEL);
        button.setFont(AppTheme.PRIMARY_FONT.deriveFont(Font.BOLD));
        button.setBackground(AppTheme.PRIMARY);
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(120, 36));
        button.addActionListener(event -> handleSubmit());
        return button;
    }
    
    /**
     * Creates the cancel button with secondary styling.
     */
    private JButton createCancelButton() {
        JButton button = new JButton("Cancel");
        button.setFont(AppTheme.PRIMARY_FONT);
        button.setBackground(Color.WHITE);
        button.setForeground(AppTheme.TEXT_SECONDARY);
        button.setBorder(BorderFactory.createLineBorder(new Color(0xD1D5DB), 1));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(80, 36));
        button.addActionListener(event -> handleCancel());
        return button;
    }
    
    /**
     * Creates the validation message label.
     */
    private JLabel createValidationLabel() {
        JLabel label = new JLabel();
        label.setFont(AppTheme.PRIMARY_FONT.deriveFont(12f));
        label.setForeground(AppTheme.ACCENT);
        label.setHorizontalAlignment(SwingConstants.LEFT);
        return label;
    }
    
    /**
     * Creates a centering panel for the card.
     */
    private JPanel createCenteringPanel() {
        JPanel centeringPanel = new JPanel(new GridBagLayout());
        centeringPanel.setOpaque(false);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        
        centeringPanel.add(cardPanel, gbc);
        return centeringPanel;
    }
    
    /**
     * Lays out the content within the card panel.
     */
    private void layoutCardContent() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, COMPONENT_SPACING, 0);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Title
        JLabel titleLabel = new JLabel("Add New Question");
        titleLabel.setFont(AppTheme.HEADING_FONT.deriveFont(24f));
        titleLabel.setForeground(AppTheme.TEXT_PRIMARY);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, PADDING_MEDIUM, 0);
        cardPanel.add(titleLabel, gbc);
        
        // Question Title Field
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, COMPONENT_SPACING, 0);
        cardPanel.add(titleField, gbc);
        
        // Description Label
        JLabel descLabel = new JLabel("Description");
        descLabel.setFont(AppTheme.PRIMARY_FONT);
        descLabel.setForeground(AppTheme.TEXT_PRIMARY);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 4, 0);
        cardPanel.add(descLabel, gbc);
        
        // Description Area
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 0.4;
        gbc.insets = new Insets(0, 0, COMPONENT_SPACING, 0);
        cardPanel.add(descriptionScrollPane, gbc);
        
        // Reset constraints for remaining components
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;
        
        // Subject ComboBox
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, COMPONENT_SPACING, 0);
        cardPanel.add(subjectComboBox, gbc);
        
        // Tags Label
        JLabel tagsLabel = new JLabel("Tags");
        tagsLabel.setFont(AppTheme.PRIMARY_FONT);
        tagsLabel.setForeground(AppTheme.TEXT_PRIMARY);
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 4, 0);
        cardPanel.add(tagsLabel, gbc);
        
        // Tag Input Field
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, PADDING_SMALL, 0);
        cardPanel.add(tagInputField, gbc);
        
        // Tag Chip List
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, COMPONENT_SPACING, 0);
        cardPanel.add(tagChipList, gbc);
        
        // Validation Label
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, COMPONENT_SPACING, 0);
        cardPanel.add(validationLabel, gbc);
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, PADDING_SMALL, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(submitButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(PADDING_MEDIUM, 0, 0, 0);
        cardPanel.add(buttonPanel, gbc);
    }
    
    /**
     * Applies theme styles to components.
     */
    private void applyThemeStyles() {
        // Apply focus rings and hover effects
        titleField.getTextField().setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xD1D5DB), 1),
            new EmptyBorder(8, 8, 8, 8)
        ));
        
        // Add focus effects to description area
        descriptionArea.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                descriptionScrollPane.setBorder(BorderFactory.createLineBorder(AppTheme.PRIMARY, 2));
            }
            
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                descriptionScrollPane.setBorder(BorderFactory.createLineBorder(new Color(0xD1D5DB), 1));
            }
        });
    }
    

    
    /**
     * Handles form submission.
     */
    private void handleSubmit() {
        try {
            // Clear previous validation messages
            clearValidationMessage();
            setSubmittingState(true);
            
            // Update ViewModel with current form values
            viewModel.setTitle(titleField.getText().trim());
            viewModel.setDescription(descriptionArea.getText().trim());
            
            // Let the ViewModel handle validation and creation
            viewModel.createQuestion();
            
        } catch (Exception e) {
            Logger.getInstance().error("Failed to create question: " + e.getMessage());
            showValidationMessage("Failed to create question: " + e.getMessage());
            resetSubmissionState();
        }
    }
    
    /**
     * Handles form cancellation.
     */
    private void handleCancel() {
        clearForm();
        resetSubmissionState();
        if (onNavigateBack != null) {
            onNavigateBack.run();
        } else {
            // Fallback to direct navigation if no callback is set
            ViewNavigator.getInstance().navigateTo("home");
        }
    }
    
    /**
     * Shows a validation message.
     */
    private void showValidationMessage(String message) {
        validationLabel.setText(message);
        validationLabel.setVisible(true);
        resetSubmissionState();
    }
    
    /**
     * Clears the validation message.
     */
    private void clearValidationMessage() {
        validationLabel.setText("");
        validationLabel.setVisible(false);
    }

    /**
     * Clears all user input fields and resets the ViewModel state after submission.
     */
    private void clearForm() {
        titleField.setText("");
        descriptionArea.setText("");
        subjectComboBox.clearSelection();
        tagInputField.clear();
        viewModel.clearAll();
        clearValidationMessage();
    }

    /**
     * Toggles the submission state visuals so users receive feedback while saving.
     *
     * @param submitting whether the form is currently submitting
     */
    private void setSubmittingState(boolean submitting) {
        submitButton.setEnabled(!submitting);
        submitButton.setText(submitting ? "Saving..." : SUBMIT_BUTTON_LABEL);
        cancelButton.setEnabled(!submitting);
        titleField.getTextField().setEditable(!submitting);
        descriptionArea.setEditable(!submitting);
        subjectComboBox.getComboBox().setEnabled(!submitting);
        tagInputField.setEnabled(!submitting);
        setCursor(submitting ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
                             : Cursor.getDefaultCursor());
    }

    /**
     * Restores the submit button and cursor to their default state.
     */
    private void resetSubmissionState() {
        setSubmittingState(false);
    }
    
    /**
     * Gets the selected tags for testing purposes.
     */
    public List<Tag> getSelectedTags() {
        return viewModel.getSelectedTags();
    }
    
    /**
     * Gets the title field component.
     */
    public FormField getTitleField() {
        return titleField;
    }
    
    /**
     * Gets the description area component.
     */
    public JTextArea getDescriptionArea() {
        return descriptionArea;
    }
    
    /**
     * Gets the submit button component.
     */
    public JButton getSubmitButton() {
        return submitButton;
    }
    
    /**
     * Gets the cancel button component.
     */
    public JButton getCancelButton() {
        return cancelButton;
    }
    
    /**
     * Sets the callback to be executed when navigation back is requested.
     * 
     * @param callback The callback to execute on navigation back
     */
    public void setOnNavigateBack(Runnable callback) {
        this.onNavigateBack = callback;
    }
    
    /**
     * Sets the callback to be executed when a question is successfully created.
     * 
     * @param callback The callback to execute with the created question
     */
    public void setOnQuestionCreated(java.util.function.Consumer<com.upnext.app.domain.question.Question> callback) {
        this.onQuestionCreated = callback;
    }
}