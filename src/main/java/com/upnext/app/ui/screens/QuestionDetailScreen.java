package com.upnext.app.ui.screens;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.upnext.app.App;
import com.upnext.app.core.Logger;
import com.upnext.app.data.question.QuestionRepository;
import com.upnext.app.domain.question.Answer;
import com.upnext.app.domain.question.Question;
import com.upnext.app.service.SearchService;
import com.upnext.app.ui.components.FeedbackManager;
import com.upnext.app.ui.navigation.ViewNavigator;
import com.upnext.app.ui.theme.AppTheme;

/**
 * Screen for displaying question details including answers and related questions.
 * Preserves filter state when navigating back to the home screen.
 */
public class QuestionDetailScreen extends JPanel {
    private static final Logger LOGGER = Logger.getInstance();
    
    // Layout constants
    private static final int PADDING_MEDIUM = 16;
    private static final int PADDING_SMALL = 8;
    
    // UI components
    private final JButton backButton;
    private final JPanel questionPanel;
    private final JPanel answersPanel;
    private final JPanel relatedQuestionsPanel;
    private final JLabel titleLabel;
    private final JTextArea contentArea;
    private final JLabel metadataLabel;
    private final JLabel answersHeaderLabel;
    private final JLabel relatedQuestionsHeaderLabel;
    
    // Data
    private Question currentQuestion;
    private final QuestionRepository questionRepository;
    private final SearchService searchService;
    
    /**
     * Creates a new question detail screen.
     */
    public QuestionDetailScreen() {
        setLayout(new BorderLayout(0, PADDING_MEDIUM));
        setBackground(AppTheme.BACKGROUND);
        setBorder(new EmptyBorder(PADDING_MEDIUM, PADDING_MEDIUM, PADDING_MEDIUM, PADDING_MEDIUM));
        
        // Initialize repositories and services
        questionRepository = QuestionRepository.getInstance();
        searchService = SearchService.getInstance();
        
        // Create header with back button
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, PADDING_MEDIUM, 0));
        
        backButton = new JButton("← Back to Questions");
        backButton.setFont(AppTheme.PRIMARY_FONT);
        backButton.setBorderPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.setForeground(AppTheme.ACCENT);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.addActionListener(e -> navigateBack());
        
        headerPanel.add(backButton, BorderLayout.WEST);
        
        // Create content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        
        // Question panel
        questionPanel = new JPanel();
        questionPanel.setLayout(new BoxLayout(questionPanel, BoxLayout.Y_AXIS));
        questionPanel.setOpaque(false);
        questionPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xE0E0E0)),
            new EmptyBorder(0, 0, PADDING_MEDIUM, 0)
        ));
        
        titleLabel = new JLabel();
        titleLabel.setFont(AppTheme.HEADING_FONT.deriveFont(Font.BOLD, 24f));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        contentArea = new JTextArea();
        contentArea.setFont(AppTheme.PRIMARY_FONT);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setEditable(false);
        contentArea.setBackground(AppTheme.BACKGROUND);
        contentArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentArea.setBorder(new EmptyBorder(PADDING_MEDIUM, 0, PADDING_MEDIUM, 0));
        
        metadataLabel = new JLabel();
        metadataLabel.setFont(AppTheme.PRIMARY_FONT.deriveFont(Font.ITALIC, 12f));
        metadataLabel.setForeground(AppTheme.TEXT_SECONDARY);
        metadataLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPanel votingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, PADDING_MEDIUM, 0));
        votingPanel.setOpaque(false);
        votingPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JButton upvoteButton = new JButton("▲ Upvote");
        upvoteButton.setFont(AppTheme.PRIMARY_FONT);
        upvoteButton.addActionListener(e -> upvoteQuestion());
        
        JButton downvoteButton = new JButton("▼ Downvote");
        downvoteButton.setFont(AppTheme.PRIMARY_FONT);
        downvoteButton.addActionListener(e -> downvoteQuestion());
        
        votingPanel.add(upvoteButton);
        votingPanel.add(downvoteButton);
        
        // Add components to question panel
        questionPanel.add(titleLabel);
        questionPanel.add(contentArea);
        questionPanel.add(metadataLabel);
        questionPanel.add(Box.createRigidArea(new Dimension(0, PADDING_MEDIUM)));
        questionPanel.add(votingPanel);
        questionPanel.add(Box.createRigidArea(new Dimension(0, PADDING_MEDIUM)));
        
        // Answers section
        JPanel answersSection = new JPanel(new BorderLayout(0, PADDING_SMALL));
        answersSection.setOpaque(false);
        answersSection.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xE0E0E0)),
            new EmptyBorder(PADDING_MEDIUM, 0, PADDING_MEDIUM, 0)
        ));
        
        answersHeaderLabel = new JLabel("Answers");
        answersHeaderLabel.setFont(AppTheme.HEADING_FONT.deriveFont(18f));
        
        // Panel for list of answers
        answersPanel = new JPanel();
        answersPanel.setLayout(new BoxLayout(answersPanel, BoxLayout.Y_AXIS));
        answersPanel.setOpaque(false);
        
        // Add to answers section
        answersSection.add(answersHeaderLabel, BorderLayout.NORTH);
        answersSection.add(answersPanel, BorderLayout.CENTER);
        
        // Answer input area
        JPanel answerInputPanel = new JPanel(new BorderLayout(0, PADDING_SMALL));
        answerInputPanel.setOpaque(false);
        answerInputPanel.setBorder(new EmptyBorder(PADDING_MEDIUM, 0, PADDING_MEDIUM, 0));
        
        JLabel yourAnswerLabel = new JLabel("Your Answer");
        yourAnswerLabel.setFont(AppTheme.HEADING_FONT.deriveFont(16f));
        
        JTextArea answerTextArea = new JTextArea();
        answerTextArea.setFont(AppTheme.PRIMARY_FONT);
        answerTextArea.setLineWrap(true);
        answerTextArea.setWrapStyleWord(true);
        answerTextArea.setRows(5);
        answerTextArea.setBorder(BorderFactory.createLineBorder(new Color(0xDDDDDD)));
        
        JScrollPane answerScrollPane = new JScrollPane(answerTextArea);
        answerScrollPane.setBorder(new EmptyBorder(PADDING_SMALL, 0, PADDING_SMALL, 0));
        
        JButton submitAnswerButton = new JButton("Submit Answer");
        submitAnswerButton.setBackground(AppTheme.PRIMARY);
        submitAnswerButton.setForeground(Color.WHITE);
        submitAnswerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        submitAnswerButton.addActionListener(e -> {
            // Submit answer implementation
            String answerText = answerTextArea.getText().trim();
            if (!answerText.isEmpty()) {
                submitAnswer(answerText);
                answerTextArea.setText("");
            } else {
                FeedbackManager.showWarning(
                    this,
                    "Please enter your answer before submitting.",
                    "Empty Answer"
                );
            }
        });
        
        JPanel submitButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        submitButtonPanel.setOpaque(false);
        submitButtonPanel.add(submitAnswerButton);
        
        answerInputPanel.add(yourAnswerLabel, BorderLayout.NORTH);
        answerInputPanel.add(answerScrollPane, BorderLayout.CENTER);
        answerInputPanel.add(submitButtonPanel, BorderLayout.SOUTH);
        
        // Related questions section
        JPanel relatedQuestionsSection = new JPanel(new BorderLayout(0, PADDING_SMALL));
        relatedQuestionsSection.setOpaque(false);
        relatedQuestionsSection.setBorder(new EmptyBorder(PADDING_MEDIUM, 0, 0, 0));
        
        relatedQuestionsHeaderLabel = new JLabel("Related Questions");
        relatedQuestionsHeaderLabel.setFont(AppTheme.HEADING_FONT.deriveFont(18f));
        
        // Panel for list of related questions
        relatedQuestionsPanel = new JPanel();
        relatedQuestionsPanel.setLayout(new BoxLayout(relatedQuestionsPanel, BoxLayout.Y_AXIS));
        relatedQuestionsPanel.setOpaque(false);
        
        // Add to related questions section
        relatedQuestionsSection.add(relatedQuestionsHeaderLabel, BorderLayout.NORTH);
        relatedQuestionsSection.add(relatedQuestionsPanel, BorderLayout.CENTER);
        
        // Add all sections to content panel
        contentPanel.add(questionPanel);
        contentPanel.add(answersSection);
        contentPanel.add(answerInputPanel);
        contentPanel.add(relatedQuestionsSection);
        
        // Create scroll pane for content
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        
        // Add components to main layout
        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    /**
     * Loads a question for display.
     * 
     * @param questionId The ID of the question to load
     * @return true if the question was loaded successfully, false otherwise
     */
    public boolean loadQuestion(Long questionId) {
        if (questionId == null) {
            LOGGER.error("Attempted to load question with null ID");
            return false;
        }
        
        try {
            // Clear previous content
            clearContent();
            
            // Load question
            questionRepository.findById(questionId).ifPresentOrElse(
                question -> {
                    this.currentQuestion = question;
                    displayQuestion(question);
                    loadAnswers(questionId);
                    loadRelatedQuestions(question);
                    
                    // Increment view count in background
                    incrementViewCount(questionId);
                },
                () -> {
                    LOGGER.error("Question not found with ID: " + questionId);
                    showErrorState("Question not found");
                }
            );
            
            return currentQuestion != null;
        } catch (SQLException e) {
            LOGGER.logException("Error loading question with ID: " + questionId, e);
            showErrorState("Error loading question");
            return false;
        }
    }
    
    /**
     * Clears all content from the screen.
     */
    private void clearContent() {
        titleLabel.setText("");
        contentArea.setText("");
        metadataLabel.setText("");
        answersPanel.removeAll();
        relatedQuestionsPanel.removeAll();
        currentQuestion = null;
    }
    
    /**
     * Shows an error state when a question cannot be loaded.
     * 
     * @param message The error message to display
     */
    private void showErrorState(String message) {
        titleLabel.setText(message);
        contentArea.setText("The requested question could not be loaded. Please try again or go back to the questions list.");
        metadataLabel.setText("");
        answersHeaderLabel.setText("No Answers");
        relatedQuestionsHeaderLabel.setText("No Related Questions");
    }
    
    /**
     * Displays the question content.
     * 
     * @param question The question to display
     */
    private void displayQuestion(Question question) {
        titleLabel.setText(question.getTitle());
        contentArea.setText(question.getContent());
        
        // Format date
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy 'at' h:mm a");
        String formattedDate = dateFormat.format(question.getCreatedAt());
        
        // Build metadata string
        StringBuilder metadataBuilder = new StringBuilder();
        metadataBuilder.append("Asked by ").append(question.getUserName())
                      .append(" on ").append(formattedDate);
        
        if (question.getSubjectName() != null && !question.getSubjectName().isEmpty()) {
            metadataBuilder.append(" in ").append(question.getSubjectName());
        }
        
        metadataBuilder.append(" • ").append(question.getViewCount()).append(" views");
        metadataBuilder.append(" • ").append(question.getUpvotes()).append(" upvotes");
        
        metadataLabel.setText(metadataBuilder.toString());
        
        // Update answer header
        answersHeaderLabel.setText(question.getAnswerCount() > 0 ? 
                                 question.getAnswerCount() + " Answers" : 
                                 "No Answers Yet");
    }
    
    /**
     * Loads answers for the question.
     * 
     * @param questionId The ID of the question
     */
    private void loadAnswers(Long questionId) {
        answersPanel.removeAll();
        
        try {
            List<Answer> answers = questionRepository.findAnswersForQuestion(questionId);
            
            if (answers.isEmpty()) {
                JLabel noAnswersLabel = new JLabel("Be the first to answer this question!");
                noAnswersLabel.setFont(AppTheme.PRIMARY_FONT.deriveFont(Font.ITALIC));
                noAnswersLabel.setForeground(AppTheme.TEXT_SECONDARY);
                noAnswersLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                answersPanel.add(noAnswersLabel);
            } else {
                for (Answer answer : answers) {
                    JPanel answerCard = createAnswerCard(answer);
                    answersPanel.add(answerCard);
                    answersPanel.add(Box.createRigidArea(new Dimension(0, PADDING_MEDIUM)));
                }
            }
            
            answersPanel.revalidate();
            answersPanel.repaint();
        } catch (SQLException e) {
            LOGGER.logException("Error loading answers for question: " + questionId, e);
            JLabel errorLabel = new JLabel("Error loading answers");
            errorLabel.setForeground(Color.RED);
            errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            answersPanel.add(errorLabel);
        }
    }
    
    /**
     * Creates a card for displaying an answer.
     * 
     * @param answer The answer to display
     * @return A panel containing the answer card
     */
    private JPanel createAnswerCard(Answer answer) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xEEEEEE)),
            new EmptyBorder(PADDING_SMALL, PADDING_SMALL, PADDING_SMALL, PADDING_SMALL)
        ));
        
        // Answer content
        JTextArea answerContent = new JTextArea(answer.getContent());
        answerContent.setFont(AppTheme.PRIMARY_FONT);
        answerContent.setLineWrap(true);
        answerContent.setWrapStyleWord(true);
        answerContent.setEditable(false);
        answerContent.setBackground(AppTheme.BACKGROUND);
        answerContent.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Answer metadata
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy 'at' h:mm a");
        String formattedDate = dateFormat.format(answer.getCreatedAt());
        
        JLabel answerMetadataLabel = new JLabel("Answered by " + answer.getUserName() + 
                                      " on " + formattedDate);
        answerMetadataLabel.setFont(AppTheme.PRIMARY_FONT.deriveFont(Font.ITALIC, 12f));
        answerMetadataLabel.setForeground(AppTheme.TEXT_SECONDARY);
        answerMetadataLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Add to card
        card.add(answerContent);
        card.add(Box.createRigidArea(new Dimension(0, PADDING_SMALL)));
        card.add(answerMetadataLabel);
        
        return card;
    }
    
    /**
     * Loads related questions.
     * 
     * @param question The current question to find related questions for
     */
    private void loadRelatedQuestions(Question question) {
        relatedQuestionsPanel.removeAll();
        
        // Get related questions
        List<Question> relatedQuestions = searchService.getRelatedQuestions(question, 5);
        
        if (relatedQuestions.isEmpty()) {
            JLabel noRelatedLabel = new JLabel("No related questions found");
            noRelatedLabel.setFont(AppTheme.PRIMARY_FONT.deriveFont(Font.ITALIC));
            noRelatedLabel.setForeground(AppTheme.TEXT_SECONDARY);
            noRelatedLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            relatedQuestionsPanel.add(noRelatedLabel);
        } else {
            // Create a grid for related questions
            JPanel gridPanel = new JPanel(new GridLayout(0, 1, 0, PADDING_SMALL));
            gridPanel.setOpaque(false);
            
            for (Question relatedQuestion : relatedQuestions) {
                JPanel relatedCard = createRelatedQuestionCard(relatedQuestion);
                gridPanel.add(relatedCard);
            }
            
            gridPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            relatedQuestionsPanel.add(gridPanel);
        }
        
        relatedQuestionsPanel.revalidate();
        relatedQuestionsPanel.repaint();
    }
    
    /**
     * Creates a card for displaying a related question.
     * 
     * @param question The related question to display
     * @return A panel containing the related question card
     */
    private JPanel createRelatedQuestionCard(Question question) {
        JPanel card = new JPanel(new BorderLayout());
        card.setOpaque(false);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xEEEEEE)),
            new EmptyBorder(PADDING_SMALL, PADDING_SMALL, PADDING_SMALL, PADDING_SMALL)
        ));
        
        // Question link
        JLabel relatedQuestionTitleLabel = new JLabel(question.getTitle());
        relatedQuestionTitleLabel.setFont(AppTheme.PRIMARY_FONT);
        relatedQuestionTitleLabel.setForeground(AppTheme.ACCENT);
        relatedQuestionTitleLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add click listener to navigate to the related question
        relatedQuestionTitleLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                loadQuestion(question.getId());
            }
        });
        
        // Question metadata
        JLabel relatedCardMetadataLabel = new JLabel(question.getAnswerCount() + " answers • " +
                                      question.getUpvotes() + " upvotes");
        relatedCardMetadataLabel.setFont(AppTheme.PRIMARY_FONT.deriveFont(Font.ITALIC, 11f));
        relatedCardMetadataLabel.setForeground(AppTheme.TEXT_SECONDARY);
        
        card.add(relatedQuestionTitleLabel, BorderLayout.CENTER);
        card.add(relatedCardMetadataLabel, BorderLayout.SOUTH);
        
        return card;
    }
    
    /**
     * Increments the view count for a question.
     * 
     * @param questionId The ID of the question
     */
    private void incrementViewCount(Long questionId) {
        SwingUtilities.invokeLater(() -> {
            try {
                questionRepository.incrementViewCount(questionId);
                if (currentQuestion != null) {
                    currentQuestion.incrementViewCount();
                }
            } catch (SQLException e) {
                LOGGER.logException("Failed to increment view count for question: " + questionId, e);
                // Non-critical operation, don't show error to user
            }
        });
    }
    
    /**
     * Upvotes the current question.
     */
    private void upvoteQuestion() {
        if (currentQuestion == null) return;
        
        try {
            currentQuestion.incrementUpvotes();
            questionRepository.updateVoteCounts(
                currentQuestion.getId(),
                currentQuestion.getUpvotes(),
                currentQuestion.getDownvotes()
            );
            
            // Update metadata display
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy 'at' h:mm a");
            String formattedDate = dateFormat.format(currentQuestion.getCreatedAt());
            
            StringBuilder metadataBuilder = new StringBuilder();
            metadataBuilder.append("Asked by ").append(currentQuestion.getUserName())
                         .append(" on ").append(formattedDate);
            
            if (currentQuestion.getSubjectName() != null && !currentQuestion.getSubjectName().isEmpty()) {
                metadataBuilder.append(" in ").append(currentQuestion.getSubjectName());
            }
            
            metadataBuilder.append(" • ").append(currentQuestion.getViewCount()).append(" views");
            metadataBuilder.append(" • ").append(currentQuestion.getUpvotes()).append(" upvotes");
            
            metadataLabel.setText(metadataBuilder.toString());
        } catch (SQLException e) {
            LOGGER.logException("Failed to upvote question: " + currentQuestion.getId(), e);
            FeedbackManager.showError(
                this,
                "Failed to upvote question. Please try again later.",
                "Error"
            );
        }
    }
    
    /**
     * Downvotes the current question.
     */
    private void downvoteQuestion() {
        if (currentQuestion == null) return;
        
        try {
            currentQuestion.incrementDownvotes();
            questionRepository.updateVoteCounts(
                currentQuestion.getId(),
                currentQuestion.getUpvotes(),
                currentQuestion.getDownvotes()
            );
        } catch (SQLException e) {
            LOGGER.logException("Failed to downvote question: " + currentQuestion.getId(), e);
            FeedbackManager.showError(
                this,
                "Failed to downvote question. Please try again later.",
                "Error"
            );
        }
    }
    
    /**
     * Submits an answer for the current question.
     * 
     * @param answerContent The answer content
     */
    private void submitAnswer(String answerContent) {
        if (currentQuestion == null) return;
        
        try {
            Answer answer = new Answer();
            answer.setQuestionId(currentQuestion.getId());
            answer.setContent(answerContent);
            // User would be set from AuthService in a complete implementation
            
            questionRepository.saveAnswer(answer);
            
            // Update the question's answer count
            currentQuestion.incrementAnswerCount();
            
            // Refresh answers display
            loadAnswers(currentQuestion.getId());
            
            // Update answers header
            answersHeaderLabel.setText(currentQuestion.getAnswerCount() + " Answers");
            
            FeedbackManager.showSuccess(
                this,
                "Your answer was submitted successfully.",
                "Answer Submitted"
            );
        } catch (SQLException e) {
            LOGGER.logException("Failed to submit answer for question: " + currentQuestion.getId(), e);
            FeedbackManager.showError(
                this,
                "Failed to submit answer. Please try again later.",
                "Error"
            );
        }
    }
    
    /**
     * Navigates back to the home screen while preserving filter state.
     * The filter state is automatically persisted by FilterManager across screens.
     */
    private void navigateBack() {
        // No additional action needed - FilterManager persists state
        // and the HomeScreen automatically uses this state when redisplayed
        ViewNavigator.getInstance().navigateTo(App.HOME_SCREEN);
    }
}