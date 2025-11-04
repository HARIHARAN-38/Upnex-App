package com.upnext.app.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import com.upnext.app.core.Logger;
import com.upnext.app.data.question.QuestionRepository;
import com.upnext.app.domain.User;
import com.upnext.app.domain.question.Answer;
import com.upnext.app.service.AuthService;
import com.upnext.app.ui.theme.AppTheme;

/**
 * Panel for inputting and submitting answers to questions.
 * Provides a rich text input area with validation and user feedback.
 */
public class AnswerInputPanel extends JPanel {
    private static final Logger LOGGER = Logger.getInstance();
    
    // Layout constants
    private static final int PADDING_MEDIUM = 16;
    private static final int PADDING_SMALL = 8;
    private static final int MIN_ANSWER_LENGTH = 10;
    private static final int MAX_ANSWER_LENGTH = 5000;
    
    // UI components
    private final JLabel titleLabel;
    private final JTextArea answerTextArea;
    private final JButton submitButton;
    private final JLabel characterCountLabel;
    private final JPanel buttonPanel;
    
    // Services
    private final QuestionRepository questionRepository;
    private final AuthService authService;
    
    // State
    private Long currentQuestionId;
    private Consumer<Answer> onAnswerSubmitted;
    private boolean isSubmitting = false;
    
    /**
     * Creates a new answer input panel.
     */
    public AnswerInputPanel() {
        // Initialize services
        questionRepository = QuestionRepository.getInstance();
        authService = AuthService.getInstance();
        
        // Setup main layout
        setLayout(new BorderLayout(0, PADDING_SMALL));
        setOpaque(true);
        setBackground(AppTheme.SURFACE);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xE0E0E0)),
            new EmptyBorder(PADDING_MEDIUM, 0, PADDING_MEDIUM, 0)
        ));
        
        // Create title section
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setOpaque(false);
        
        titleLabel = new JLabel("Your Answer");
        titleLabel.setFont(AppTheme.HEADING_FONT.deriveFont(Font.BOLD, 16f));
        titleLabel.setForeground(AppTheme.TEXT_PRIMARY);
        
        titlePanel.add(titleLabel);
        
        // Create input section
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setOpaque(false);
        
        // Text area with enhanced styling
        answerTextArea = new JTextArea();
        answerTextArea.setFont(AppTheme.PRIMARY_FONT.deriveFont(14f));
        answerTextArea.setLineWrap(true);
        answerTextArea.setWrapStyleWord(true);
        answerTextArea.setRows(6);
        answerTextArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xDDDDDD), 1),
            new EmptyBorder(PADDING_SMALL, PADDING_SMALL, PADDING_SMALL, PADDING_SMALL)
        ));
        answerTextArea.setBackground(Color.WHITE);
        
        // Add placeholder text effect
        String placeholderText = "Write your answer here... Be clear, helpful, and provide detailed explanations.";
        answerTextArea.setText(placeholderText);
        answerTextArea.setForeground(AppTheme.TEXT_SECONDARY);
        
        // Handle focus events for placeholder
        answerTextArea.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (answerTextArea.getText().equals(placeholderText)) {
                    answerTextArea.setText("");
                    answerTextArea.setForeground(AppTheme.TEXT_PRIMARY);
                }
            }
            
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (answerTextArea.getText().trim().isEmpty()) {
                    answerTextArea.setText(placeholderText);
                    answerTextArea.setForeground(AppTheme.TEXT_SECONDARY);
                }
                updateCharacterCount();
            }
        });
        
        // Add document listener for real-time character count
        answerTextArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateCharacterCount(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateCharacterCount(); }
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateCharacterCount(); }
        });
        
        // Create scroll pane for text area
        JScrollPane scrollPane = new JScrollPane(answerTextArea);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        // Character count label
        characterCountLabel = new JLabel("0 / " + MAX_ANSWER_LENGTH + " characters");
        characterCountLabel.setFont(AppTheme.PRIMARY_FONT.deriveFont(11f));
        characterCountLabel.setForeground(AppTheme.TEXT_SECONDARY);
        characterCountLabel.setAlignmentX(LEFT_ALIGNMENT);
        
        // Submit button
        submitButton = new JButton("Post Answer");
        submitButton.setFont(AppTheme.PRIMARY_FONT.deriveFont(Font.BOLD, 14f));
        submitButton.setBackground(AppTheme.PRIMARY);
        submitButton.setForeground(Color.WHITE);
        submitButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        submitButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        submitButton.setFocusPainted(false);
        submitButton.addActionListener(this::handleSubmit);
        
        // Add hover effects
        submitButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (submitButton.isEnabled()) {
                    submitButton.setBackground(AppTheme.PRIMARY.darker());
                }
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (submitButton.isEnabled()) {
                    submitButton.setBackground(AppTheme.PRIMARY);
                }
            }
        });
        
        // Button panel with right alignment
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(submitButton);
        
        // Create bottom panel for character count and button
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.add(characterCountLabel, BorderLayout.WEST);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);
        
        // Add components to input panel
        inputPanel.add(scrollPane);
        inputPanel.add(Box.createRigidArea(new Dimension(0, PADDING_SMALL)));
        inputPanel.add(bottomPanel);
        
        // Add to main panel
        add(titlePanel, BorderLayout.NORTH);
        add(inputPanel, BorderLayout.CENTER);
        
        // Initialize state
        updateSubmitButtonState();
    }
    
    /**
     * Sets the question ID that this panel will submit answers for.
     * 
     * @param questionId The ID of the question
     */
    public void setQuestionId(Long questionId) {
        this.currentQuestionId = questionId;
        updateSubmitButtonState();
    }
    
    /**
     * Sets the callback to be invoked when an answer is successfully submitted.
     * 
     * @param onAnswerSubmitted The callback function
     */
    public void setOnAnswerSubmitted(Consumer<Answer> onAnswerSubmitted) {
        this.onAnswerSubmitted = onAnswerSubmitted;
    }
    
    /**
     * Updates the character count display and submit button state.
     */
    private void updateCharacterCount() {
        String text = getAnswerText();
        int length = text.length();
        
        characterCountLabel.setText(length + " / " + MAX_ANSWER_LENGTH + " characters");
        
        // Color code the character count
        if (length > MAX_ANSWER_LENGTH) {
            characterCountLabel.setForeground(new Color(0xdc3545)); // Red
        } else if (length < MIN_ANSWER_LENGTH) {
            characterCountLabel.setForeground(AppTheme.TEXT_SECONDARY); // Gray
        } else {
            characterCountLabel.setForeground(new Color(0x28a745)); // Green
        }
        
        updateSubmitButtonState();
    }
    
    /**
     * Gets the current answer text, excluding placeholder text.
     * 
     * @return The answer text or empty string if placeholder is showing
     */
    private String getAnswerText() {
        String text = answerTextArea.getText();
        String placeholderText = "Write your answer here... Be clear, helpful, and provide detailed explanations.";
        
        if (text.equals(placeholderText) || text.trim().isEmpty()) {
            return "";
        }
        
        return text.trim();
    }
    
    /**
     * Updates the submit button state based on current conditions.
     */
    private void updateSubmitButtonState() {
        String text = getAnswerText();
        boolean isValid = !isSubmitting && 
                         currentQuestionId != null && 
                         text.length() >= MIN_ANSWER_LENGTH && 
                         text.length() <= MAX_ANSWER_LENGTH &&
                         authService.getCurrentUser() != null;
        
        submitButton.setEnabled(isValid);
        
        if (isSubmitting) {
            submitButton.setText("Posting...");
            submitButton.setBackground(AppTheme.TEXT_SECONDARY);
        } else {
            submitButton.setText("Post Answer");
            submitButton.setBackground(isValid ? AppTheme.PRIMARY : AppTheme.TEXT_SECONDARY);
        }
    }
    
    /**
     * Handles the submit button click.
     * 
     * @param event The action event (unused)
     */
    private void handleSubmit(ActionEvent event) {
        if (isSubmitting) {
            return;
        }
        
        // Validate user authentication
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            FeedbackManager.showError(
                this,
                "You must be signed in to post an answer.",
                "Authentication Required"
            );
            return;
        }
        
        // Validate question ID
        if (currentQuestionId == null) {
            FeedbackManager.showError(
                this,
                "No question selected. Please refresh the page and try again.",
                "Invalid Question"
            );
            return;
        }
        
        // Validate answer content
        String answerText = getAnswerText();
        if (answerText.length() < MIN_ANSWER_LENGTH) {
            FeedbackManager.showWarning(
                this,
                "Please provide a more detailed answer (minimum " + MIN_ANSWER_LENGTH + " characters).",
                "Answer Too Short"
            );
            answerTextArea.requestFocus();
            return;
        }
        
        if (answerText.length() > MAX_ANSWER_LENGTH) {
            FeedbackManager.showWarning(
                this,
                "Your answer is too long. Please shorten it to " + MAX_ANSWER_LENGTH + " characters or less.",
                "Answer Too Long"
            );
            answerTextArea.requestFocus();
            return;
        }
        
        // Submit the answer
        submitAnswer(answerText, currentUser);
    }
    
    /**
     * Submits the answer to the database.
     * 
     * @param answerText The answer content
     * @param user The user submitting the answer
     */
    private void submitAnswer(String answerText, User user) {
        isSubmitting = true;
        updateSubmitButtonState();
        
        // Run in background thread to avoid blocking UI
        new Thread(() -> {
            try {
                LOGGER.info("Submitting answer for question " + currentQuestionId + " by user " + user.getId());
                
                // Create answer object
                Answer answer = new Answer();
                answer.setQuestionId(currentQuestionId);
                answer.setUserId(user.getId());
                answer.setUserName(user.getName());
                answer.setContent(answerText);
                answer.setCreatedAt(LocalDateTime.now());
                answer.setUpvotes(0);
                answer.setDownvotes(0);
                
                // Save to database
                questionRepository.saveAnswer(answer);
                
                // Update UI on EDT
                javax.swing.SwingUtilities.invokeLater(() -> {
                    isSubmitting = false;
                    updateSubmitButtonState();
                    
                    // Clear the input
                    clearInput();
                    
                    // Show success message
                    FeedbackManager.showSuccess(
                        this,
                        "Your answer has been posted successfully!",
                        "Answer Posted"
                    );
                    
                    // Notify callback
                    if (onAnswerSubmitted != null) {
                        onAnswerSubmitted.accept(answer);
                    }
                    
                    LOGGER.info("Answer submitted successfully for question " + currentQuestionId);
                });
                
            } catch (SQLException e) {
                LOGGER.logException("Failed to submit answer for question " + currentQuestionId, e);
                
                // Update UI on EDT
                javax.swing.SwingUtilities.invokeLater(() -> {
                    isSubmitting = false;
                    updateSubmitButtonState();
                    
                    FeedbackManager.showError(
                        this,
                        "Failed to post your answer. Please check your connection and try again.",
                        "Submission Error"
                    );
                });
            }
        }).start();
    }
    
    /**
     * Clears the input area and resets it to placeholder state.
     */
    public void clearInput() {
        String placeholderText = "Write your answer here... Be clear, helpful, and provide detailed explanations.";
        answerTextArea.setText(placeholderText);
        answerTextArea.setForeground(AppTheme.TEXT_SECONDARY);
        updateCharacterCount();
    }
    
    /**
     * Sets focus to the text area for immediate typing.
     */
    public void focusInput() {
        answerTextArea.requestFocus();
    }
    
    /**
     * Sets whether this panel is enabled for input.
     * 
     * @param enabled true to enable input, false to disable
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        answerTextArea.setEnabled(enabled);
        updateSubmitButtonState();
    }
    
    /**
     * Gets the current text in the answer input area.
     * 
     * @return The current answer text
     */
    public String getCurrentText() {
        return getAnswerText();
    }
    
    /**
     * Sets the text in the answer input area.
     * 
     * @param text The text to set
     */
    public void setText(String text) {
        if (text == null || text.trim().isEmpty()) {
            clearInput();
        } else {
            answerTextArea.setText(text);
            answerTextArea.setForeground(AppTheme.TEXT_PRIMARY);
            updateCharacterCount();
        }
    }
}