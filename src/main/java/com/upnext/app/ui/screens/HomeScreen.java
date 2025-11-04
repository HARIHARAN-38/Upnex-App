package com.upnext.app.ui.screens;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.upnext.app.App;
import com.upnext.app.core.Logger;
import com.upnext.app.data.question.SubjectRepository;
import com.upnext.app.data.question.TagRepository;
import com.upnext.app.domain.User;
import com.upnext.app.domain.question.Question;
import com.upnext.app.service.AuthService;
import com.upnext.app.ui.components.FeedbackManager;
import com.upnext.app.ui.components.FilterManager;
import com.upnext.app.ui.components.HeroBar;
import com.upnext.app.ui.components.ProfileSummaryCard;
import com.upnext.app.ui.components.QuestionFeedPanel;
import com.upnext.app.ui.components.SubjectNavigationPanel;
import com.upnext.app.ui.navigation.ViewNavigator;
import com.upnext.app.ui.theme.AppTheme;

/**
 * Home screen for the UpNext application with a three-column layout.
 * Left column: Subject navigation and filters
 * Center column: Question feed
 * Right column: Profile summary and metrics
 */
public class HomeScreen extends JPanel {
    // Layout constants
    public static final int PADDING_LARGE = 24;
    public static final int PADDING_MEDIUM = 16;
    public static final int PADDING_SMALL = 8;
    public static final int COLUMN_GAP = 20;
    
    // Breakpoints for responsive design
    public static final int BREAKPOINT_SMALL = 800;
    public static final int BREAKPOINT_MEDIUM = 1200;
    public static final int BREAKPOINT_LARGE = 1600;
    
    // Width ratios for the three columns (left:center:right)
    private static final double LEFT_COLUMN_RATIO = 0.22;    // 22% of total width
    private static final double RIGHT_COLUMN_RATIO = 0.23;   // 23% of total width
    // Center column gets the remaining space (1.0 - LEFT_COLUMN_RATIO - RIGHT_COLUMN_RATIO)
    
    // UI components
    private final JLabel welcomeLabel;
    private JButton signOutButton; // Changed from final to allow initialization in createHeroBar
    private final JPanel leftColumn;
    private final JPanel centerColumn;
    private final JPanel rightColumn;
    private final HeroBar heroBar;
    private QuestionFeedPanel questionFeedPanel;
    
    // Filter manager for unified filter state
    private final FilterManager filterManager = FilterManager.getInstance();
    
    /**
     * Creates a new home screen with a three-column layout.
     */
    public HomeScreen() {
        setLayout(new BorderLayout());
        setBackground(AppTheme.BACKGROUND);
        setBorder(new EmptyBorder(PADDING_MEDIUM, PADDING_MEDIUM, PADDING_MEDIUM, PADDING_MEDIUM));
        
        // Initialize FilterManager with repository data
        SwingUtilities.invokeLater(() -> {
            // Load persisted subject and tag data from repositories
            filterManager.loadPersistedSubject(id -> {
                try {
                    return SubjectRepository.getInstance().findById(id).orElse(null);
                } catch (Exception e) {
                    Logger.getInstance().error("Failed to load persisted subject: " + e.getMessage());
                    return null;
                }
            });
            
            filterManager.loadPersistedTags(name -> {
                try {
                    return TagRepository.getInstance().findByName(name).orElse(null);
                } catch (Exception e) {
                    Logger.getInstance().error("Failed to load persisted tag: " + e.getMessage());
                    return null;
                }
            });
        });
        
        // Welcome message that will be updated based on the current user
        welcomeLabel = new JLabel();
        welcomeLabel.setFont(AppTheme.PRIMARY_FONT.deriveFont(16f));
        welcomeLabel.setBorder(new EmptyBorder(PADDING_SMALL, 0, PADDING_MEDIUM, 0));
        
        // Hero bar (top bar with logo, search, and user profile)
    heroBar = createHeroBar();
        
        // Three-column container using custom layout
        JPanel columnsPanel = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                // This ensures the panel uses all available width
                return new Dimension(super.getPreferredSize().width, super.getPreferredSize().height);
            }
            
            @Override
            public void doLayout() {
                // Custom layout for the three columns with proper ratios
                int width = getWidth();
                int height = getHeight();
                
                // Calculate column widths based on ratios
                int leftWidth = (int)(width * LEFT_COLUMN_RATIO);
                int rightWidth = (int)(width * RIGHT_COLUMN_RATIO);
                int centerWidth = width - leftWidth - rightWidth - 2 * COLUMN_GAP;
                
                // Position the columns
                leftColumn.setBounds(0, 0, leftWidth, height);
                centerColumn.setBounds(leftWidth + COLUMN_GAP, 0, centerWidth, height);
                rightColumn.setBounds(leftWidth + centerWidth + 2 * COLUMN_GAP, 0, rightWidth, height);
            }
        };
        columnsPanel.setOpaque(false);
        columnsPanel.setLayout(null); // Using null layout for custom positioning
        
        // Create the three columns
        leftColumn = createLeftColumn();
        centerColumn = createCenterColumn();
        rightColumn = createRightColumn();
        
        // Add columns to the panel
        columnsPanel.add(leftColumn);
        columnsPanel.add(centerColumn);
        columnsPanel.add(rightColumn);
        
        // Initial minimum sizes to guide layout
        int minPanelHeight = 400;
        leftColumn.setMinimumSize(new Dimension(200, minPanelHeight));
        centerColumn.setMinimumSize(new Dimension(400, minPanelHeight));
        rightColumn.setMinimumSize(new Dimension(200, minPanelHeight));
        
        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.add(welcomeLabel, BorderLayout.NORTH);
        contentPanel.add(columnsPanel, BorderLayout.CENTER);
        
        // Add to main layout
    add(heroBar, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        
        // Set initial welcome message
        setInitialWelcomeMessage();
    }
    
    /**
     * Creates the hero bar (top bar with logo, search, and profile).
     * 
     * @return The hero bar panel
     */
    private HeroBar createHeroBar() {
        HeroBar heroBarComponent = new HeroBar();
        
        // Set search callback to handle search from hero bar
        heroBarComponent.setSearchCallback(query -> {
            // Handle search queries from the hero bar
            if (query.startsWith("SELECTED:")) {
                // Handle a selected question from search results
                try {
                    long questionId = Long.parseLong(query.substring(9));
                    Logger.getInstance().info("Question selected from search: " + questionId);
                    SwingUtilities.invokeLater(() -> navigateToQuestionDetail(questionId));
                } catch (NumberFormatException e) {
                    Logger.getInstance().error("Invalid question ID format: " + query);
                }
            } else {
                // Update the filter manager with the search query
                filterManager.setSearchText(query);
            }
        });
        
        // Store reference to sign out button for App.java
        signOutButton = heroBarComponent.getSignOutButton();
        
        return heroBarComponent;
    }

    /**
     * Exposes the hero bar for integration tests.
     *
     * @return the hero bar component
     */
    public HeroBar getHeroBar() {
        return heroBar;
    }

    /**
     * Exposes the question feed panel for integration tests.
     *
     * @return the question feed panel
     */
    public QuestionFeedPanel getQuestionFeedPanel() {
        return questionFeedPanel;
    }
    
    /**
     * Creates the left column (subject navigation and filters).
     * 
     * @return The left column panel
     */
    private JPanel createLeftColumn() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(createColumnBorder());
        
        // Header for the left column
        JLabel headerLabel = new JLabel("Subjects & Tags");
        headerLabel.setFont(AppTheme.HEADING_FONT);
        headerLabel.setBorder(new EmptyBorder(0, 0, PADDING_MEDIUM, 0));
        
        // Subject navigation panel
        SubjectNavigationPanel navigationPanel = new SubjectNavigationPanel();
        
        // Connect the SubjectNavigationPanel to the FilterManager
        navigationPanel.setFilterChangeListener((subject, tags) -> {
            // Update the FilterManager with selected subject and tags
            filterManager.setSelectedSubject(subject);
            filterManager.setSelectedTags(tags);
        });
        
        // Initialize with saved filter values (after load)
        SwingUtilities.invokeLater(() -> {
            // If there's a previously selected subject, select it in the UI
            if (filterManager.getSelectedSubject() != null) {
                navigationPanel.selectSubject(filterManager.getSelectedSubject());
            }
        });
        
        panel.add(headerLabel, BorderLayout.NORTH);
        panel.add(navigationPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Creates the center column (question feed).
     * 
     * @return The center column panel
     */
    private JPanel createCenterColumn() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(createColumnBorder());
        
        // Header with title
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, PADDING_MEDIUM, 0));
        
        JLabel headerLabel = new JLabel("Questions");
        headerLabel.setFont(AppTheme.HEADING_FONT);
        headerPanel.add(headerLabel, BorderLayout.WEST);
        
        // Filter reset button
        JButton resetFiltersButton = new JButton("Clear Filters");
        resetFiltersButton.setForeground(AppTheme.ACCENT);
        resetFiltersButton.setBorderPainted(false);
        resetFiltersButton.setContentAreaFilled(false);
        resetFiltersButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        resetFiltersButton.addActionListener(e -> filterManager.clearAllFilters());
        headerPanel.add(resetFiltersButton, BorderLayout.EAST);
        
        // Question feed panel
        questionFeedPanel = new QuestionFeedPanel();
        questionFeedPanel.setFeedListener(new QuestionFeedPanel.QuestionFeedListener() {
            @Override
            public void onQuestionSelected(Question question) {
                // Navigate to question detail screen
                Logger.getInstance().info("Question selected: " + question.getTitle());
                navigateToQuestionDetail(question.getId());
            }
            
            @Override
            public void onQuestionVoted(Question question) {
                // No additional action needed as the feed panel handles the vote
                Logger.getInstance().info("Question voted: " + question.getTitle() + 
                                  " (Upvotes: " + question.getUpvotes() + 
                                  ", Downvotes: " + question.getDownvotes() + ")");
            }
        });
        
        // Connect the QuestionFeedPanel to the FilterManager
        filterManager.addFilterChangeListener((criteria, filterState) -> {
            questionFeedPanel.applySearchCriteria(criteria);
        });
        
        // "Ask a Question" button at the bottom
        JButton askButton = new JButton("Ask a Question");
        askButton.setBackground(AppTheme.PRIMARY);
        askButton.setForeground(Color.WHITE);
        askButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        askButton.addActionListener(e -> {
            Logger.getInstance().info("Navigating to Add Question screen");
            ViewNavigator.getInstance().navigateTo(com.upnext.app.App.ADD_QUESTION_SCREEN);
        });
        
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(PADDING_MEDIUM, 0, 0, 0));
        bottomPanel.add(askButton);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(questionFeedPanel, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    // Instance variable for the profile summary card
    private ProfileSummaryCard profileSummaryCard;
    
    /**
     * Creates the right column (profile summary and metrics).
     * 
     * @return The right column panel
     */
    private JPanel createRightColumn() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(createColumnBorder());
        
        // Header for the right column
        JLabel headerLabel = new JLabel("Profile");
        headerLabel.setFont(AppTheme.HEADING_FONT);
        headerLabel.setBorder(new EmptyBorder(0, 0, PADDING_MEDIUM, 0));
        
        // Create the profile summary card
        profileSummaryCard = new ProfileSummaryCard();
        profileSummaryCard.showLoadingState();
        
        // Refresh the profile data
        SwingUtilities.invokeLater(() -> {
            User currentUser = AuthService.getInstance().getCurrentUser();
            if (currentUser != null) {
                profileSummaryCard.updateFromUser(currentUser);
            } else {
                profileSummaryCard.showEmptyState();
            }
        });
        
        panel.add(headerLabel, BorderLayout.NORTH);
        panel.add(profileSummaryCard, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Creates a standard border for columns.
     * 
     * @return The border
     */
    private EmptyBorder createColumnBorder() {
        return new EmptyBorder(PADDING_SMALL, PADDING_SMALL, PADDING_SMALL, PADDING_SMALL);
    }
    
    // Method removed as it's no longer needed
    
    /**
     * Updates the welcome message and profile card based on the current authenticated user.
     */
    public void updateWelcomeMessage() {
        User currentUser = AuthService.getInstance().getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getName() + "! Explore questions below or ask your own.");
            
            // Update profile card with current user data
            if (profileSummaryCard != null) {
                profileSummaryCard.updateFromUser(currentUser);
            }
        } else {
            welcomeLabel.setText("Welcome to UpNext! Sign in to continue.");
            
            // Update profile card to empty state
            if (profileSummaryCard != null) {
                profileSummaryCard.showEmptyState();
            }
        }
    }
    
    /**
     * Gets the sign out button.
     * 
     * @return The sign out button
     */
    public JButton getSignOutButton() {
        return signOutButton;
    }
    
    /**
     * Sets the initial welcome message in the constructor.
     * This is separate from updateWelcomeMessage() to avoid overridable method call in constructor.
     */
    private void setInitialWelcomeMessage() {
        User currentUser = AuthService.getInstance().getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getName() + "! Explore questions below or ask your own.");
        } else {
            welcomeLabel.setText("Welcome to UpNext! Sign in to continue.");
        }
    }
    
    /**
     * Navigates to the question detail screen for the specified question ID.
     * 
     * @param questionId The ID of the question to view in detail
     */
    private void navigateToQuestionDetail(Long questionId) {
        try {
            ViewNavigator navigator = ViewNavigator.getInstance();
            
            // Get question detail screen
            if (!navigator.hasScreen(App.QUESTION_DETAIL_SCREEN)) {
                Logger.getInstance().info("Question detail screen not registered, cannot navigate");
                return;
            }
            
            // Navigate to question detail screen
            navigator.navigateTo(App.QUESTION_DETAIL_SCREEN);
            
            // Get the question detail screen and load the question
            QuestionDetailScreen detailScreen = (QuestionDetailScreen) navigator.getScreen(App.QUESTION_DETAIL_SCREEN);
            boolean loaded = detailScreen.loadQuestion(questionId);
            
            if (!loaded) {
                // If loading fails, navigate back to home screen
                navigator.navigateTo(App.HOME_SCREEN);
                FeedbackManager.showError(
                    this,
                    "Failed to load question details. Please try again.",
                    "Error"
                );
            }
        } catch (Exception e) {
            Logger.getInstance().logException("Error navigating to question detail: " + e.getMessage(), e);
            FeedbackManager.showError(
                this,
                "An error occurred while opening the question.",
                "Navigation Error"
            );
        }
    }
    
    /**
     * Adds a new question to the top of the question feed.
     * This method is called after a question is successfully created
     * to refresh the feed with the new content.
     * 
     * @param question The newly created question to add to the feed
     */
    public void addNewQuestionToFeed(Question question) {
        Logger.getInstance().info("Adding new question to feed: " + question.getTitle());
        
        // For now, we'll trigger a refresh of the feed
        // In the future, this could be enhanced to prepend the question directly
        if (questionFeedPanel != null) {
            SwingUtilities.invokeLater(() -> {
                questionFeedPanel.prependQuestion(question);
            });
        }
    }
}