package com.upnext.app.ui.screens;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

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
import com.upnext.app.data.question.AnswerRepository;
import com.upnext.app.data.question.AnswerRepository.VoteResult;
import com.upnext.app.data.question.QuestionRepository;
import com.upnext.app.data.question.QuestionVoteRepository;
import com.upnext.app.data.question.TagRepository;
import com.upnext.app.domain.User;
import com.upnext.app.domain.question.Answer;
import com.upnext.app.domain.question.Question;
import com.upnext.app.domain.question.QuestionVote;
import com.upnext.app.domain.question.Tag;
import com.upnext.app.service.AuthService;
import com.upnext.app.service.SearchService;
import com.upnext.app.ui.components.AnswerInputPanel;
import com.upnext.app.ui.components.FeedbackManager;
import com.upnext.app.ui.components.FilterManager;
import com.upnext.app.ui.components.HeroBar;
import com.upnext.app.ui.components.QuestionDetailsCard;
import com.upnext.app.ui.components.TagChip;
import com.upnext.app.ui.components.VotePanel;
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
    private static final int PADDING_LARGE = 24;
    
    // Responsive breakpoints
    private static final int MOBILE_BREAKPOINT = 768;
    private static final int TABLET_BREAKPOINT = 1024;
    private static final int DESKTOP_BREAKPOINT = 1200;
    
    // Layout dimensions
    private static final int SIDEBAR_WIDTH_DESKTOP = 300;
    private static final int SIDEBAR_WIDTH_TABLET = 250;
    private static final int MIN_CONTENT_WIDTH = 400;
    
    // UI components
    private final JButton backButton;
    private final JPanel questionPanel;
    private final JPanel answersPanel;
    private final JPanel relatedQuestionsPanel;
    private final JLabel titleLabel;
    private final JTextArea contentArea;
    private final JLabel metadataLabel;
    private final JLabel questionAuthorNameLabel;
    private final JLabel questionAuthorAvatarLabel;
    private final JLabel questionAuthorMetaLabel;
    private final JPanel questionTagPanel;
    private final JLabel answersHeaderLabel;
    private final JLabel relatedQuestionsHeaderLabel;
    private final AnswerInputPanel answerInputPanel;
    private final VotePanel questionVotePanel;
    
    // Data
    private Question currentQuestion;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final TagRepository tagRepository;
    private final SearchService searchService;
    private final AuthService authService;
    private final FilterManager filterManager;
    private final QuestionVoteRepository questionVoteRepository;
    
    // Responsive layout components
    private final JPanel mainContentPanel;
    private final QuestionDetailsCard questionDetailsCard;
    private final JScrollPane contentScrollPane;
    private final HeroBar heroBar;
    private boolean isMobileLayout = false;
    
    /**
     * Creates a new question detail screen.
     */
    public QuestionDetailScreen() {
        setLayout(new BorderLayout(0, PADDING_MEDIUM));
        setBackground(AppTheme.BACKGROUND);
        
        // Enhanced responsive padding - more on larger screens
        int responsivePadding = PADDING_MEDIUM;
        setBorder(new EmptyBorder(responsivePadding, responsivePadding, responsivePadding, responsivePadding));
        
        // Initialize repositories and services
    questionRepository = QuestionRepository.getInstance();
    answerRepository = AnswerRepository.getInstance();
    tagRepository = TagRepository.getInstance();
    searchService = SearchService.getInstance();
    authService = AuthService.getInstance();
    filterManager = FilterManager.getInstance();
    questionVoteRepository = QuestionVoteRepository.getInstance();
        
    // Create hero section (top navigation bar with search)
    heroBar = new HeroBar();

    // Create header with breadcrumb navigation (under hero bar)
    JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, PADDING_MEDIUM, 0));
        
        // Create breadcrumb panel
        JPanel breadcrumbPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        breadcrumbPanel.setOpaque(false);
        
        JLabel homeLabel = new JLabel("Home");
        homeLabel.setFont(AppTheme.PRIMARY_FONT);
        homeLabel.setForeground(AppTheme.ACCENT);
        homeLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        homeLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                navigateBack();
            }
            
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                homeLabel.setForeground(AppTheme.PRIMARY);
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                homeLabel.setForeground(AppTheme.ACCENT);
            }
        });
        
        JLabel separatorLabel = new JLabel(" → ");
        separatorLabel.setFont(AppTheme.PRIMARY_FONT);
        separatorLabel.setForeground(AppTheme.TEXT_SECONDARY);
        
        JLabel currentPageLabel = new JLabel("Question");
        currentPageLabel.setFont(AppTheme.PRIMARY_FONT);
        currentPageLabel.setForeground(AppTheme.TEXT_SECONDARY);
        
        breadcrumbPanel.add(homeLabel);
        breadcrumbPanel.add(separatorLabel);
        breadcrumbPanel.add(currentPageLabel);
        
        // Legacy back button for additional navigation option
        backButton = new JButton("← Back");
        backButton.setFont(AppTheme.PRIMARY_FONT.deriveFont(12f));
        backButton.setBorderPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.setForeground(AppTheme.ACCENT);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.addActionListener(e -> navigateBack());
        
        headerPanel.add(breadcrumbPanel, BorderLayout.WEST);
        headerPanel.add(backButton, BorderLayout.EAST);
        
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

    questionAuthorAvatarLabel = new JLabel("👤");
    questionAuthorAvatarLabel.setFont(AppTheme.PRIMARY_FONT.deriveFont(18f));
    questionAuthorAvatarLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
    questionAuthorAvatarLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

    questionAuthorNameLabel = new JLabel();
    questionAuthorNameLabel.setFont(AppTheme.PRIMARY_FONT.deriveFont(Font.BOLD));
    questionAuthorNameLabel.setForeground(AppTheme.ACCENT);
    questionAuthorNameLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
    questionAuthorNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

    questionAuthorMetaLabel = new JLabel();
    questionAuthorMetaLabel.setFont(AppTheme.PRIMARY_FONT.deriveFont(Font.ITALIC, 12f));
    questionAuthorMetaLabel.setForeground(AppTheme.TEXT_SECONDARY);
    questionAuthorMetaLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

    JPanel questionAuthorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, PADDING_SMALL, 0));
    questionAuthorPanel.setOpaque(false);
    questionAuthorPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    questionAuthorPanel.add(questionAuthorAvatarLabel);
    questionAuthorPanel.add(questionAuthorNameLabel);
    questionAuthorPanel.add(questionAuthorMetaLabel);

    contentArea = new JTextArea();
    contentArea.setFont(AppTheme.PRIMARY_FONT);
    contentArea.setLineWrap(true);
    contentArea.setWrapStyleWord(true);
    contentArea.setEditable(false);
    contentArea.setBackground(AppTheme.BACKGROUND);
    contentArea.setAlignmentX(Component.LEFT_ALIGNMENT);
    contentArea.setBorder(new EmptyBorder(PADDING_MEDIUM, 0, PADDING_SMALL, 0));

    questionTagPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
    questionTagPanel.setOpaque(false);
    questionTagPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    questionTagPanel.setVisible(false);

    metadataLabel = new JLabel();
    metadataLabel.setFont(AppTheme.PRIMARY_FONT.deriveFont(Font.PLAIN, 12f));
    metadataLabel.setForeground(AppTheme.TEXT_SECONDARY);
    metadataLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Create question vote panel
        questionVotePanel = VotePanel.createHorizontal();
        questionVotePanel.setOnVoteAction(this::handleQuestionVote);
        questionVotePanel.setOnError(error -> FeedbackManager.showError(this, error, "Vote Error"));
        questionVotePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Add components to question panel
    questionPanel.add(titleLabel);
    questionPanel.add(Box.createRigidArea(new Dimension(0, PADDING_SMALL)));
    questionPanel.add(questionAuthorPanel);
    questionPanel.add(Box.createRigidArea(new Dimension(0, PADDING_SMALL)));
    questionPanel.add(contentArea);
    questionPanel.add(questionTagPanel);
    questionPanel.add(metadataLabel);
    questionPanel.add(Box.createRigidArea(new Dimension(0, PADDING_MEDIUM)));
        questionPanel.add(questionVotePanel);
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
        
        // Create enhanced answer input panel
        answerInputPanel = new AnswerInputPanel();
        answerInputPanel.setOnAnswerSubmitted(this::handleNewAnswer);
        
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

        // Create responsive main content panel with enhanced styling
        mainContentPanel = createEnhancedContentPanel();
        mainContentPanel.setOpaque(false);
        
        // Create question details card for sidebar
        questionDetailsCard = new QuestionDetailsCard();
        questionDetailsCard.setQuestion(currentQuestion);
        
        // Store content scroll pane for responsive layout switching
        contentScrollPane = scrollPane;
        
        // Initialize responsive layout
        setupResponsiveLayout();
        
        // Add component listener for responsive layout adjustments
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                adjustLayoutForScreenSize();
            }
        });

        // Add components to main layout (hero at top, then breadcrumbs)
        JPanel northWrapper = new JPanel();
        northWrapper.setLayout(new BoxLayout(northWrapper, BoxLayout.Y_AXIS));
        northWrapper.setOpaque(false);
        northWrapper.add(heroBar);
        northWrapper.add(headerPanel);
        add(northWrapper, BorderLayout.NORTH);
        add(mainContentPanel, BorderLayout.CENTER);
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
                    // Update left-side details card
                    questionDetailsCard.setQuestion(question);
                    loadAnswers(questionId);
                    loadRelatedQuestions(question);
                    
                    // Set question ID for answer input panel
                    answerInputPanel.setQuestionId(questionId);
                    
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

        String authorName = question.getUserName() != null && !question.getUserName().isBlank()
            ? question.getUserName()
            : "Anonymous";
        questionAuthorNameLabel.setText(authorName);
        questionAuthorAvatarLabel.setToolTipText("View " + authorName + "'s profile");

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a");
        String formattedDate = question.getCreatedAt() != null
            ? dateFormatter.format(question.getCreatedAt())
            : "an unknown date";
        questionAuthorMetaLabel.setText("asked on " + formattedDate);

        configureAuthorInteractions(question.getUserId(), authorName);

        metadataLabel.setText(buildQuestionStatistics(question));

        // Initialize vote panel with current vote count
        int netVotes = question.getUpvotes() - question.getDownvotes();
        questionVotePanel.setVoteCount(netVotes);
        questionVotePanel.setItemId(question.getId());

        displayQuestionTags(question);

        answersHeaderLabel.setText(question.getAnswerCount() > 0
            ? question.getAnswerCount() + " Answers"
            : "No Answers Yet");
    }
    
    /**
     * Displays the question tags as clickable chips.
     * 
     * @param question The question whose tags to display
     */
    private void displayQuestionTags(Question question) {
        questionTagPanel.removeAll();

        try {
            List<String> tags = questionRepository.getTagsForQuestion(question.getId());
            if (tags.isEmpty()) {
                questionTagPanel.setVisible(false);
            } else {
                JLabel tagsLabel = new JLabel("Tags:");
                tagsLabel.setFont(AppTheme.PRIMARY_FONT.deriveFont(Font.BOLD, 12f));
                tagsLabel.setForeground(AppTheme.TEXT_SECONDARY);
                questionTagPanel.add(tagsLabel);

                for (String tag : tags) {
                    TagChip tagChip = TagChip.createCompact(tag);
                    tagChip.setOnClickCallback(this::applyTagFilter);
                    questionTagPanel.add(tagChip);
                }

                questionTagPanel.setVisible(true);
            }
        } catch (SQLException e) {
            LOGGER.logException("Error loading tags for question: " + question.getId(), e);
            questionTagPanel.setVisible(false);
        }

        questionTagPanel.revalidate();
        questionTagPanel.repaint();
    }
    
    private void configureAuthorInteractions(Long userId, String userName) {
        resetMouseListeners(questionAuthorAvatarLabel);
        resetMouseListeners(questionAuthorNameLabel);

        java.awt.event.MouseAdapter avatarAdapter = new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                navigateToProfileLayout(userId, userName);
            }
        };
        questionAuthorAvatarLabel.addMouseListener(avatarAdapter);

        java.awt.event.MouseAdapter nameAdapter = new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                navigateToProfileLayout(userId, userName);
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                questionAuthorNameLabel.setForeground(AppTheme.PRIMARY);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                questionAuthorNameLabel.setForeground(AppTheme.ACCENT);
            }
        };
        questionAuthorNameLabel.addMouseListener(nameAdapter);
    }

    private void resetMouseListeners(JLabel label) {
        for (var listener : label.getMouseListeners()) {
            label.removeMouseListener(listener);
        }
    }

    private String buildQuestionStatistics(Question question) {
        StringBuilder stats = new StringBuilder();

        if (question.getSubjectName() != null && !question.getSubjectName().isBlank()) {
            stats.append("Subject: ").append(question.getSubjectName()).append(" • ");
        }

        stats.append(question.getViewCount()).append(" views");
        stats.append(" • ").append(question.getUpvotes()).append(" upvotes");
        if (question.getDownvotes() > 0) {
            stats.append(", ").append(question.getDownvotes()).append(" downvotes");
        }
        stats.append(" • ").append(question.getAnswerCount()).append(
            question.getAnswerCount() == 1 ? " answer" : " answers");

        return stats.toString();
    }

    private void applyTagFilter(String tagName) {
        if (tagName == null || tagName.isBlank()) {
            return;
        }

        try {
            filterManager.clearAllFilters();
            Tag filterTag = tagRepository.findByName(tagName)
                .orElseGet(() -> new Tag(tagName));
            filterManager.setSelectedTags(List.of(filterTag));
            ViewNavigator.getInstance().navigateTo(App.HOME_SCREEN);
        } catch (SQLException e) {
            LOGGER.logException("Error applying tag filter for: " + tagName, e);
            FeedbackManager.showWarning(
                this,
                "Unable to filter by tag right now. Please try again.",
                "Tag Filter Error"
            );
        }
    }

    private void navigateToProfileLayout(Long userId, String userName) {
        LOGGER.info("Navigating to profile for user: " + userName + " (id=" + userId + ")");
        ViewNavigator.getInstance().navigateTo(App.PROFILE_LAYOUT_SCREEN);
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
                // Sort answers by verification status first, then by net vote score (highest first)
                answers.sort((a1, a2) -> {
                    // Verified answers come first
                    if (a1.isVerified() != a2.isVerified()) {
                        return a1.isVerified() ? -1 : 1;
                    }
                    // Within each group, sort by net vote score (upvotes - downvotes)
                    int score1 = a1.getUpvotes() - a1.getDownvotes();
                    int score2 = a2.getUpvotes() - a2.getDownvotes();
                    return Integer.compare(score2, score1);
                });
                
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
        JPanel card = new JPanel(new BorderLayout(PADDING_MEDIUM, 0));
        card.setOpaque(true);
        card.setBackground(AppTheme.SURFACE);
        
        // Enhanced border with shadow effect for better separation
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xDDDDDD)),
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xF8F9FA))
            ),
            new EmptyBorder(PADDING_MEDIUM, PADDING_SMALL, PADDING_MEDIUM, PADDING_SMALL)
        ));
        
        // Left side - voting panel using VotePanel component
        VotePanel votingPanel = VotePanel.createCompact();
        votingPanel.setItemId(answer.getId());
        votingPanel.setOnVoteAction((answerId, isUpvote) -> handleAnswerVote(answerId, isUpvote));
        votingPanel.setOnError(error -> FeedbackManager.showError(this, error, "Vote Error"));
        
        // Calculate and set initial vote count
        int netVoteScore = answer.getUpvotes() - answer.getDownvotes();
        votingPanel.setVoteCount(netVoteScore);
        
        // Right side - content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Verified answer badge if applicable
        if (answer.isVerified()) {
            JLabel verifiedBadge = new JLabel("✓ Verified Answer");
            verifiedBadge.setFont(AppTheme.PRIMARY_FONT.deriveFont(Font.BOLD, 11f));
            verifiedBadge.setForeground(new Color(0x28a745));
            verifiedBadge.setOpaque(true);
            verifiedBadge.setBackground(new Color(0xd4edda));
            verifiedBadge.setBorder(new EmptyBorder(2, 6, 2, 6));
            verifiedBadge.setAlignmentX(Component.LEFT_ALIGNMENT);
            contentPanel.add(verifiedBadge);
            contentPanel.add(Box.createRigidArea(new Dimension(0, PADDING_SMALL)));
        }
        
        // Answer content
        JTextArea answerContent = new JTextArea(answer.getContent());
        answerContent.setFont(AppTheme.PRIMARY_FONT);
        answerContent.setLineWrap(true);
        answerContent.setWrapStyleWord(true);
        answerContent.setEditable(false);
        answerContent.setBackground(AppTheme.BACKGROUND);
        answerContent.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // User info and metadata panel
        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        userInfoPanel.setOpaque(false);
        userInfoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // User avatar placeholder (clickable)
        JLabel avatarLabel = new JLabel("👤");
        avatarLabel.setFont(AppTheme.PRIMARY_FONT.deriveFont(16f));
        avatarLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        avatarLabel.setToolTipText("View " + answer.getUserName() + "'s profile");
        avatarLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                navigateToProfileLayout(answer.getUserId(), answer.getUserName());
            }
        });
        
        // User name (clickable)
        JLabel userNameLabel = new JLabel(answer.getUserName());
        userNameLabel.setFont(AppTheme.PRIMARY_FONT.deriveFont(Font.BOLD));
        userNameLabel.setForeground(AppTheme.ACCENT);
        userNameLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        userNameLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                navigateToProfileLayout(answer.getUserId(), answer.getUserName());
            }
            
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                userNameLabel.setForeground(AppTheme.PRIMARY);
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                userNameLabel.setForeground(AppTheme.ACCENT);
            }
        });
        
        // Answer date
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a");
        String formattedDate = answer.getCreatedAt() != null
            ? dateFormatter.format(answer.getCreatedAt())
            : "an unknown time";
        JLabel dateLabel = new JLabel(" answered on " + formattedDate);
        dateLabel.setFont(AppTheme.PRIMARY_FONT.deriveFont(Font.ITALIC, 12f));
        dateLabel.setForeground(AppTheme.TEXT_SECONDARY);
        
        userInfoPanel.add(avatarLabel);
        userInfoPanel.add(Box.createRigidArea(new Dimension(4, 0)));
        userInfoPanel.add(userNameLabel);
        userInfoPanel.add(dateLabel);
        
        // Add components to content panel
        contentPanel.add(answerContent);
        contentPanel.add(Box.createRigidArea(new Dimension(0, PADDING_SMALL)));
        contentPanel.add(userInfoPanel);
        
        // Add panels to card
        card.add(votingPanel, BorderLayout.WEST);
        card.add(contentPanel, BorderLayout.CENTER);
        
        return card;
    }
    
    /**
     * Handles voting on an answer with VotePanel callback compatibility.
     * 
     * @param answerId The ID of the answer to vote on
     * @param isUpvote true for upvote, false for downvote
     */
    private void handleAnswerVote(Long answerId, Boolean isUpvote) {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                FeedbackManager.showWarning(this, "Please log in to vote", "Authentication Required");
                return;
            }
            
            VoteResult result = answerRepository.voteAnswer(answerId, currentUser.getId(), isUpvote);
            LOGGER.info("Answer " + answerId + " vote updated. Upvotes=" + result.getUpvotes()
                + ", Downvotes=" + result.getDownvotes());
            
            // Show success feedback
            FeedbackManager.showInfo(this, 
                (isUpvote ? "Upvote" : "Downvote") + " recorded successfully", 
                "Vote Recorded");
            
            // Note: The VotePanel will be updated automatically via the loadAnswers refresh
            // Refresh the answers display to show updated vote counts
            if (currentQuestion != null) {
                loadAnswers(currentQuestion.getId());
            }
            
        } catch (SQLException e) {
            FeedbackManager.showError(this, "Database error while voting: " + e.getMessage(), "Database Error");
        }
    }
    
    /**
     * Loads related questions.
     * 
     * @param question The current question to find related questions for
     */
    private void loadRelatedQuestions(Question question) {
        relatedQuestionsPanel.removeAll();
        
        // Prefer questions that share the same tags (exact match filtering)
        List<Question> relatedQuestions;
        try {
            var criteria = new com.upnext.app.domain.question.QuestionSearchCriteria()
                    .setTags(question.getTags())
                    .setLimit(10);
            relatedQuestions = questionRepository.search(criteria);
            // Exclude the current question
            relatedQuestions.removeIf(q -> q.getId().equals(question.getId()));
            // Keep top 5
            if (relatedQuestions.size() > 5) {
                relatedQuestions = relatedQuestions.subList(0, 5);
            }
        } catch (SQLException ex) {
            LOGGER.logException("Error finding related questions by tags", ex);
            // Fallback to service-based heuristic if tag query fails
            relatedQuestions = searchService.getRelatedQuestions(question, 5);
        }
        
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
    private void handleQuestionVote(Long questionId, Boolean isUpvote) {
        if (currentQuestion == null || !Objects.equals(currentQuestion.getId(), questionId)) {
            FeedbackManager.showError(this, "Question not found", "Error");
            return;
        }

        if (isUpvote == null) {
            FeedbackManager.showWarning(this, "Unable to process vote", "Vote Error");
            return;
        }

        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                FeedbackManager.showWarning(this, "Please log in to vote", "Authentication Required");
                return;
            }

            QuestionVote.VoteType voteType = isUpvote ? QuestionVote.VoteType.UPVOTE : QuestionVote.VoteType.DOWNVOTE;
            QuestionVoteRepository.VoteResult voteResult = questionVoteRepository.castVote(currentUser.getId(), questionId, voteType);

            int[] counts = questionVoteRepository.countVotes(questionId);
            int upvotes = counts[0];
            int downvotes = counts[1];

            currentQuestion.setUpvotes(upvotes);
            currentQuestion.setDownvotes(downvotes);
            questionRepository.updateVoteCounts(questionId, upvotes, downvotes);

            questionVotePanel.setVoteCount(upvotes - downvotes);

            String message;
            if (voteResult == QuestionVoteRepository.VoteResult.REMOVED) {
                message = "Vote removed";
            } else {
                message = isUpvote ? "Upvote recorded" : "Downvote recorded";
            }

            FeedbackManager.showInfo(this, message, "Vote Recorded");

        } catch (SQLException e) {
            FeedbackManager.showError(this, "Database error while voting: " + e.getMessage(), "Database Error");
        }
    }
    
    /**
     * Handles a new answer being submitted successfully.
     * Refreshes the answers display and updates the question's answer count.
     * 
     * @param newAnswer The newly submitted answer (unused - we refresh from database)
     */
    private void handleNewAnswer(Answer newAnswer) {
        if (currentQuestion == null || newAnswer == null) {
            return;
        }
        
        try {
            // Update the question's answer count
            currentQuestion.incrementAnswerCount();
            
            // Refresh answers display to show the new answer at the top
            loadAnswers(currentQuestion.getId());
            
            // Update answers header
            answersHeaderLabel.setText(currentQuestion.getAnswerCount() + " Answers");
            
            LOGGER.info("Answer display refreshed after new submission for question: " + currentQuestion.getId());
            
        } catch (Exception e) {
            LOGGER.logException("Error refreshing answers display after new submission", e);
            // Don't show error to user as the answer was already saved successfully
        }
    }
    
    /**
     * Navigates back to the home screen.
     * The filter state is automatically persisted by FilterManager across screens.
     */
    private void navigateBack() {
        // No additional action needed - FilterManager persists state
        // and the HomeScreen automatically uses this state when redisplayed
        ViewNavigator.getInstance().navigateTo(App.HOME_SCREEN);
    }
    
    /**
     * Sets up the initial responsive layout based on current screen size.
     */
    private void setupResponsiveLayout() {
        adjustLayoutForScreenSize();
    }
    
    /**
     * Adjusts the layout based on current screen size to provide optimal
     * user experience across different device types.
     */
    private void adjustLayoutForScreenSize() {
        int screenWidth = getWidth();
        boolean shouldUseMobileLayout = screenWidth <= MOBILE_BREAKPOINT;
        
        // Only update layout if the breakpoint has changed
        if (shouldUseMobileLayout != isMobileLayout) {
            isMobileLayout = shouldUseMobileLayout;
            updateLayoutForBreakpoint();
        }
        
        // Update sidebar width based on screen size
        updateSidebarWidth(screenWidth);
        
        // Update responsive padding
        updateResponsivePadding(screenWidth);
    }
    
    /**
     * Updates the layout structure based on the current breakpoint.
     */
    private void updateLayoutForBreakpoint() {
        // Remove all components from main content panel
        mainContentPanel.removeAll();
        
        if (isMobileLayout) {
            // Mobile layout: Stack sidebar above content
            setupMobileLayout();
        } else {
            // Desktop/Tablet layout: Side-by-side layout
            setupDesktopLayout();
        }
        
        // Refresh the layout
        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }
    
    /**
     * Sets up mobile-friendly stacked layout.
     */
    private void setupMobileLayout() {
        // Use vertical BoxLayout for mobile
        mainContentPanel.setLayout(new BoxLayout(mainContentPanel, BoxLayout.Y_AXIS));
        
        // Create a wrapper for the sidebar with proper alignment
        JPanel sidebarWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        sidebarWrapper.setOpaque(false);
        sidebarWrapper.add(questionDetailsCard);
        sidebarWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Set maximum width for mobile
        questionDetailsCard.setPreferredSize(new Dimension(Integer.MAX_VALUE, questionDetailsCard.getPreferredSize().height));
        questionDetailsCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        
        // Add components vertically
        mainContentPanel.add(sidebarWrapper);
        mainContentPanel.add(Box.createRigidArea(new Dimension(0, PADDING_MEDIUM)));
        
        // Create wrapper for content scroll pane
        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.setOpaque(false);
        contentWrapper.add(contentScrollPane, BorderLayout.CENTER);
        
        mainContentPanel.add(contentWrapper);
    }
    
    /**
     * Sets up desktop/tablet side-by-side layout.
     */
    private void setupDesktopLayout() {
        // Use BorderLayout for desktop
        mainContentPanel.setLayout(new BorderLayout(PADDING_MEDIUM, 0));
        
        // Add sidebar to west and content to center
        mainContentPanel.add(questionDetailsCard, BorderLayout.WEST);
        mainContentPanel.add(contentScrollPane, BorderLayout.CENTER);
    }
    
    /**
     * Updates sidebar width based on screen size.
     * 
     * @param screenWidth Current screen width
     */
    private void updateSidebarWidth(int screenWidth) {
        if (!isMobileLayout && questionDetailsCard != null) {
            int sidebarWidth;
            
            if (screenWidth >= DESKTOP_BREAKPOINT) {
                sidebarWidth = SIDEBAR_WIDTH_DESKTOP;
            } else if (screenWidth >= TABLET_BREAKPOINT) {
                sidebarWidth = SIDEBAR_WIDTH_TABLET;
            } else {
                // For smaller desktop screens, use a proportional width
                sidebarWidth = Math.max(200, screenWidth / 4);
            }
            
            // Ensure minimum content width is maintained
            int maxSidebarWidth = screenWidth - MIN_CONTENT_WIDTH - PADDING_MEDIUM * 3;
            sidebarWidth = Math.min(sidebarWidth, maxSidebarWidth);
            
            questionDetailsCard.setPreferredSize(new Dimension(sidebarWidth, 0));
            questionDetailsCard.setMaximumSize(new Dimension(sidebarWidth, Integer.MAX_VALUE));
        }
    }
    
    /**
     * Updates padding based on screen size for better spacing on larger screens.
     * 
     * @param screenWidth Current screen width
     */
    private void updateResponsivePadding(int screenWidth) {
        int padding;
        
        if (screenWidth >= DESKTOP_BREAKPOINT) {
            padding = PADDING_LARGE; // More padding on large screens
        } else if (screenWidth >= TABLET_BREAKPOINT) {
            padding = PADDING_MEDIUM;
        } else {
            padding = PADDING_SMALL; // Less padding on mobile
        }
        
        setBorder(new EmptyBorder(padding, padding, padding, padding));
        
        // Also update the main content panel gap
        if (mainContentPanel.getLayout() instanceof BorderLayout borderLayout) {
            borderLayout.setHgap(padding);
        }
    }
    
    /**
     * Paints the component with gradient background and drop shadows for enhanced visual appeal.
     */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = getWidth();
        int height = getHeight();
        
        // Create subtle gradient background
        Color gradientStart = AppTheme.BACKGROUND;
        Color gradientEnd = new Color(
            Math.max(0, gradientStart.getRed() - 10),
            Math.max(0, gradientStart.getGreen() - 10),
            Math.max(0, gradientStart.getBlue() - 10)
        );
        
        GradientPaint gradient = new GradientPaint(
            0, 0, gradientStart,
            0, height, gradientEnd
        );
        
        g2.setPaint(gradient);
        g2.fillRect(0, 0, width, height);
        
        g2.dispose();
        super.paintComponent(g);
    }
    
    /**
     * Creates an enhanced content panel with subtle styling improvements.
     * 
     * @return Enhanced JPanel with responsive BorderLayout
     */
    private JPanel createEnhancedContentPanel() {
        return new JPanel(new BorderLayout(PADDING_MEDIUM, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Add subtle rounded corners and shadow effect for content areas
                int width = getWidth();
                int height = getHeight();
                
                // Draw subtle shadow
                g2.setColor(new Color(0, 0, 0, 10));
                g2.fillRoundRect(2, 2, width - 4, height - 4, 8, 8);
                
                // Draw main background
                g2.setColor(AppTheme.SURFACE);
                g2.fillRoundRect(0, 0, width - 2, height - 2, 6, 6);
                
                g2.dispose();
                super.paintComponent(g);
            }
        };
    }
}