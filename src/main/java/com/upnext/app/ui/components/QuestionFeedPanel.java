package com.upnext.app.ui.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.upnext.app.core.Logger;
import com.upnext.app.data.question.QuestionRepository;
import com.upnext.app.data.question.QuestionVoteRepository;
import com.upnext.app.domain.User;
import com.upnext.app.domain.question.Question;
import com.upnext.app.domain.question.QuestionSearchCriteria;
import com.upnext.app.domain.question.QuestionVote.VoteType;
import com.upnext.app.domain.question.Subject;
import com.upnext.app.domain.question.Tag;
import com.upnext.app.service.AuthService;
import com.upnext.app.ui.theme.AppTheme;

/**
 * Panel that displays a feed of question cards with filtering options.
 * Supports toolbar filters (Hot/New/Unanswered/Solved) and handles empty states.
 */
public class QuestionFeedPanel extends JPanel implements QuestionCard.QuestionCardListener {
    private static final Logger LOGGER = Logger.getInstance();
    private static final int PADDING = 16;
    private static final int CARD_GAP = 10;
    private static final int PAGE_SIZE = 10;
    
    // UI Components
    private final JPanel feedPanel;
    private final JPanel toolbarPanel;
    private final JPanel loadMorePanel;
    private final JPanel emptyStatePanel;
    private final JScrollPane scrollPane;
    private final JButton loadMoreButton;
    
    // Filter buttons
    private JToggleButton newButton;
    private JToggleButton hotButton;
    private JToggleButton unansweredButton;
    private JToggleButton solvedButton;
    
    // Data and state
    private final List<Question> questions = new ArrayList<>();
    private final QuestionRepository questionRepository;
    private final QuestionVoteRepository voteRepository;
    private int currentPage = 0;
    private boolean hasMoreQuestions = true;
    private QuestionSearchCriteria currentCriteria = new QuestionSearchCriteria();
    private QuestionFeedListener feedListener;
    
    /**
     * Creates a new question feed panel.
     */
    public QuestionFeedPanel() {
        questionRepository = QuestionRepository.getInstance();
        voteRepository = QuestionVoteRepository.getInstance();
        
        setLayout(new BorderLayout());
        setOpaque(false);
        
        // Create the toolbar for filter options
        toolbarPanel = createToolbarPanel();
        
        // Create the feed panel for question cards
        feedPanel = new JPanel();
        feedPanel.setOpaque(false);
        feedPanel.setLayout(new BoxLayout(feedPanel, BoxLayout.Y_AXIS));
        feedPanel.setBorder(new EmptyBorder(PADDING, 0, PADDING, 0));
        
        // Create load more button panel
        loadMorePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        loadMorePanel.setOpaque(false);
        loadMoreButton = new JButton("Load More");
        loadMoreButton.setFont(AppTheme.PRIMARY_FONT);
    loadMoreButton.addActionListener(event -> loadMoreQuestions());
        loadMorePanel.add(loadMoreButton);
        
        // Create empty state panel
        emptyStatePanel = createEmptyStatePanel();
        
        // Create the scroll pane
        scrollPane = new JScrollPane();
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
        // Initial setup with feed panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.add(feedPanel, BorderLayout.NORTH);
        contentPanel.add(loadMorePanel, BorderLayout.CENTER);
        
        scrollPane.setViewportView(contentPanel);
        
        // Add components to main layout
        add(toolbarPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        
        // Initial load of questions
        loadQuestions();
    }
    
    /**
     * Creates the toolbar panel with filter buttons.
     * 
     * @return The toolbar panel
     */
    private JPanel createToolbarPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, PADDING, 0));
        
        // Create filter buttons
        ButtonGroup filterGroup = new ButtonGroup();
        
        newButton = createFilterButton("New", true);
        hotButton = createFilterButton("Hot", false);
        unansweredButton = createFilterButton("Unanswered", false);
        solvedButton = createFilterButton("Solved", false);
        
        // Add buttons to group and panel
        filterGroup.add(newButton);
        filterGroup.add(hotButton);
        filterGroup.add(unansweredButton);
        filterGroup.add(solvedButton);
        
        panel.add(newButton);
        panel.add(hotButton);
        panel.add(unansweredButton);
        panel.add(solvedButton);
        
        return panel;
    }
    
    /**
     * Creates a filter toggle button with the given label.
     * 
     * @param label The button label
     * @param selected Whether the button is initially selected
     * @return The toggle button
     */
    private JToggleButton createFilterButton(String label, boolean selected) {
        JToggleButton button = new JToggleButton(label);
        button.setFont(AppTheme.PRIMARY_FONT);
        button.setFocusPainted(false);
        button.setSelected(selected);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Set colors based on state
        button.addChangeListener(changeEvent -> {
            if (button.isSelected()) {
                button.setForeground(AppTheme.PRIMARY);
            } else {
                button.setForeground(AppTheme.TEXT_SECONDARY);
            }
        });
        
        // Set initial colors
        if (selected) {
            button.setForeground(AppTheme.PRIMARY);
        } else {
            button.setForeground(AppTheme.TEXT_SECONDARY);
        }
        
        // Add action listener
        button.addActionListener(event -> {
            resetFeed();
            
            // Update filter settings based on the selected button
            if (button == newButton) {
                currentCriteria.setSortOption(QuestionSearchCriteria.SortOption.NEWEST);
                currentCriteria.setOnlyUnanswered(false);
                currentCriteria.setOnlySolved(false);
            } else if (button == hotButton) {
                currentCriteria.setSortOption(QuestionSearchCriteria.SortOption.MOST_UPVOTED);
                currentCriteria.setOnlyUnanswered(false);
                currentCriteria.setOnlySolved(false);
            } else if (button == unansweredButton) {
                currentCriteria.setSortOption(QuestionSearchCriteria.SortOption.NEWEST);
                currentCriteria.setOnlyUnanswered(true);
                currentCriteria.setOnlySolved(false);
            } else if (button == solvedButton) {
                currentCriteria.setSortOption(QuestionSearchCriteria.SortOption.NEWEST);
                currentCriteria.setOnlyUnanswered(false);
                currentCriteria.setOnlySolved(true);
            }
            
            // If we have a FilterManager instance, update it with our changes
            FilterManager filterManager = FilterManager.getInstance();
            if (button == newButton) {
                filterManager.setSortOption(QuestionSearchCriteria.SortOption.NEWEST);
                filterManager.setOnlyUnanswered(false);
                filterManager.setOnlySolved(false);
            } else if (button == hotButton) {
                filterManager.setSortOption(QuestionSearchCriteria.SortOption.MOST_UPVOTED);
                filterManager.setOnlyUnanswered(false);
                filterManager.setOnlySolved(false);
            } else if (button == unansweredButton) {
                filterManager.setSortOption(QuestionSearchCriteria.SortOption.NEWEST);
                filterManager.setOnlyUnanswered(true);
                filterManager.setOnlySolved(false);
            } else if (button == solvedButton) {
                filterManager.setSortOption(QuestionSearchCriteria.SortOption.NEWEST);
                filterManager.setOnlyUnanswered(false);
                filterManager.setOnlySolved(true);
            }
            
            // Apply current filters and reload
            loadQuestions();
        });
        
        return button;
    }
    
    /**
     * Creates the empty state panel to show when there are no questions.
     * 
     * @return The empty state panel
     */
    private JPanel createEmptyStatePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(PADDING * 2, PADDING, PADDING * 2, PADDING));
        
        // Empty state icon (placeholder)
        JLabel iconLabel = new JLabel("📭");
        iconLabel.setFont(new Font("Dialog", Font.PLAIN, 48));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Empty state message
        JLabel messageLabel = new JLabel("No questions found");
        messageLabel.setFont(AppTheme.HEADING_FONT);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Empty state description
        JLabel descriptionLabel = new JLabel("Try changing your filters or be the first to ask a question");
        descriptionLabel.setFont(AppTheme.PRIMARY_FONT);
        descriptionLabel.setForeground(AppTheme.TEXT_SECONDARY);
        descriptionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Add components to panel
        panel.add(Box.createVerticalGlue());
        panel.add(iconLabel);
        panel.add(Box.createRigidArea(new Dimension(0, PADDING)));
        panel.add(messageLabel);
        panel.add(Box.createRigidArea(new Dimension(0, PADDING / 2)));
        panel.add(descriptionLabel);
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }
    
    /**
     * Sets the search criteria based on UI filter selections.
     */
    private void applyCurrentFilters() {
        // Reset criteria to defaults
        currentCriteria = new QuestionSearchCriteria();
        currentCriteria.setLimit(PAGE_SIZE);
        currentCriteria.setOffset(currentPage * PAGE_SIZE);
        
        // Apply sort option based on selected filter
        if (hotButton.isSelected()) {
            currentCriteria.setSortOption(QuestionSearchCriteria.SortOption.MOST_UPVOTED);
        } else if (newButton.isSelected()) {
            currentCriteria.setSortOption(QuestionSearchCriteria.SortOption.NEWEST);
        }
        
        // Apply status filters
        if (unansweredButton.isSelected()) {
            currentCriteria.setOnlyUnanswered(true);
        } else if (solvedButton.isSelected()) {
            currentCriteria.setOnlySolved(true);
        }
    }
    
    /**
     * Loads questions based on current search criteria.
     */
    private void loadQuestions() {
        try {
            List<Question> results = questionRepository.search(currentCriteria);
            
            // Check if we have more pages
            hasMoreQuestions = results.size() == PAGE_SIZE;
            loadMoreButton.setVisible(hasMoreQuestions);
            
            // Add results to our list
            questions.addAll(results);
            
            // Update UI
            updateFeed();
        } catch (SQLException e) {
            LOGGER.logException("Failed to load questions", e);
            showErrorMessage("Failed to load questions. Please try again later.");
        }
    }
    
    /**
     * Loads the next page of questions.
     */
    private void loadMoreQuestions() {
        // Increment page and update offset
        currentPage++;
        currentCriteria.setOffset(currentPage * PAGE_SIZE);
        
        // Load more questions
        loadQuestions();
    }
    
    /**
     * Updates the feed panel with current questions.
     */
    private void updateFeed() {
        // Clear the feed panel
        feedPanel.removeAll();
        
        // Check if we have questions
        if (questions.isEmpty()) {
            // Show empty state
            scrollPane.setViewportView(emptyStatePanel);
        } else {
            // Add question cards to feed
            for (Question question : questions) {
                QuestionCard card = new QuestionCard(question);
                card.setListener(this);
                card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height));
                card.setAlignmentX(Component.LEFT_ALIGNMENT);
                feedPanel.add(card);
                feedPanel.add(Box.createRigidArea(new Dimension(0, CARD_GAP)));
            }
            
            // Set the feed as the viewport view
            JPanel contentPanel = new JPanel(new BorderLayout());
            contentPanel.setOpaque(false);
            contentPanel.add(feedPanel, BorderLayout.NORTH);
            contentPanel.add(loadMorePanel, BorderLayout.CENTER);
            
            scrollPane.setViewportView(contentPanel);
        }
        
        // Refresh UI
        revalidate();
        repaint();
    }

    /**
     * Inserts a newly created question at the top of the feed so that users see
     * their contribution immediately without waiting for a full reload.
     *
     * @param question The question to prepend to the feed
     */
    public void prependQuestion(Question question) {
        if (question == null) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            // Remove an existing copy of the same question if present
            questions.removeIf(existing -> existing.getId() != null && existing.getId().equals(question.getId()));

            // Insert at the top and trim to current page size
            questions.add(0, question);
            if (questions.size() > PAGE_SIZE) {
                questions.subList(PAGE_SIZE, questions.size()).clear();
            }

            // Update paging state and redraw cards
            boolean shouldShowLoadMore = hasMoreQuestions || questions.size() >= PAGE_SIZE;
            loadMoreButton.setVisible(shouldShowLoadMore);
            updateFeed();
        });
    }
    
    /**
     * Resets the feed to start fresh with new filters.
     */
    private void resetFeed() {
        currentPage = 0;
        questions.clear();
        feedPanel.removeAll();
    }
    
    /**
     * Applies filter changes from subject navigation panel.
     * 
     * @param subject The selected subject
     * @param tags The selected tags
     */
    public void applyFilters(Subject subject, List<Tag> tags) {
        resetFeed();
        
        // Update search criteria
        currentCriteria.setSubjectId(subject != null ? subject.getId() : null);
        
        // Add tags
        List<String> tagNames = new ArrayList<>();
        for (Tag tag : tags) {
            tagNames.add(tag.getName());
        }
        currentCriteria.setTags(tagNames);
        
        // Apply existing filter button settings
        applyCurrentFilters();
        
        // Reload questions
        loadQuestions();
    }
    
    /**
     * Applies text search to the feed.
     * 
     * @param searchText The search text
     */
    public void applySearch(String searchText) {
        resetFeed();
        
        // Update search criteria
        currentCriteria.setSearchText(searchText);
        
        // Keep existing filter button settings
        applyCurrentFilters();
        
        // Reload questions
        loadQuestions();
    }
    
    /**
     * Applies a complete search criteria to the feed.
     * This method is used by the FilterManager to apply unified filters.
     * 
     * @param criteria The search criteria to apply
     */
    public void applySearchCriteria(QuestionSearchCriteria criteria) {
        if (criteria == null) return;
        
        resetFeed();
        
        // Copy the provided criteria values to our current criteria
        // but keep our pagination settings
        int limit = currentCriteria.getLimit();
        int offset = 0; // Reset to first page
        
        currentCriteria = new QuestionSearchCriteria();
        currentCriteria.setLimit(limit);
        currentCriteria.setOffset(offset);
        
        // Copy all filter values
        currentCriteria.setSearchText(criteria.getSearchText());
        currentCriteria.setSubjectId(criteria.getSubjectId());
        currentCriteria.setTags(criteria.getTags());
        currentCriteria.setSortOption(criteria.getSortOption());
        currentCriteria.setOnlyUnanswered(criteria.isOnlyUnanswered());
        currentCriteria.setOnlySolved(criteria.isOnlySolved());
        
        // Update toolbar UI to match criteria
        updateToolbarFromCriteria();
        
        // Load questions with the new criteria
        loadQuestions();
    }
    
    /**
     * Updates the toolbar buttons to match the current criteria.
     */
    private void updateToolbarFromCriteria() {
        // First reset all selections
        newButton.setSelected(false);
        hotButton.setSelected(false);
        unansweredButton.setSelected(false);
        solvedButton.setSelected(false);
        
        // Set the appropriate button based on criteria
        if (currentCriteria.isOnlyUnanswered()) {
            unansweredButton.setSelected(true);
        } else if (currentCriteria.isOnlySolved()) {
            solvedButton.setSelected(true);
        } else {
            // Select based on sort option
            switch (currentCriteria.getSortOption()) {
                case NEWEST -> newButton.setSelected(true);
                case MOST_UPVOTED -> hotButton.setSelected(true);
                default -> newButton.setSelected(true); // Default to "New"
            }
        }
    }
    
    /**
     * Sets the listener for feed events.
     * 
     * @param listener The listener to set
     */
    public void setFeedListener(QuestionFeedListener listener) {
        this.feedListener = listener;
    }
    
    /**
     * Shows an error message dialog.
     * 
     * @param message The error message to show
     */
    private void showErrorMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                    this,
                    message,
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        });
    }
    
    // QuestionCardListener implementation
    
    @Override
    public void onQuestionSelected(Question question) {
        if (feedListener != null) {
            feedListener.onQuestionSelected(question);
        }
    }
    
    @Override
    public void onUpvote(Question question) {
        try {
            // Get current user
            User currentUser = AuthService.getInstance().getCurrentUser();
            if (currentUser == null) {
                showErrorMessage("Please sign in to vote on questions.");
                return;
            }
            
            // Cast the vote using the new Reddit-like system
            var voteResult = voteRepository.castVote(currentUser.getId(), question.getId(), VoteType.UPVOTE);
            
            // Recalculate and update vote counts from the database
            int[] voteCounts = voteRepository.countVotes(question.getId());
            question.setUpvotes(voteCounts[0]);
            question.setDownvotes(voteCounts[1]);
            
            // Update question vote counts in database
            questionRepository.updateVoteCounts(question.getId(), question.getUpvotes(), question.getDownvotes());
            
            // Find and update the card in the UI
            for (Component component : feedPanel.getComponents()) {
                if (component instanceof QuestionCard card && card.getQuestion().getId().equals(question.getId())) {
                    card.updateVoteCount();
                    break;
                }
            }
            
            // Show feedback based on vote result
            String message = switch (voteResult) {
                case CREATED -> "Upvoted!";
                case UPDATED -> "Changed to upvote!";
                case REMOVED -> "Upvote removed!";
            };
            LOGGER.info("Vote action: " + message + " for question " + question.getId());
            
            if (feedListener != null) {
                feedListener.onQuestionVoted(question);
            }
        } catch (SQLException e) {
            LOGGER.logException("Failed to upvote question", e);
            showErrorMessage("Failed to upvote question. Please try again later.");
        }
    }
    
    @Override
    public void onDownvote(Question question) {
        try {
            // Get current user
            User currentUser = AuthService.getInstance().getCurrentUser();
            if (currentUser == null) {
                showErrorMessage("Please sign in to vote on questions.");
                return;
            }
            
            // Cast the vote using the new Reddit-like system
            var voteResult = voteRepository.castVote(currentUser.getId(), question.getId(), VoteType.DOWNVOTE);
            
            // Recalculate and update vote counts from the database
            int[] voteCounts = voteRepository.countVotes(question.getId());
            question.setUpvotes(voteCounts[0]);
            question.setDownvotes(voteCounts[1]);
            
            // Update question vote counts in database
            questionRepository.updateVoteCounts(question.getId(), question.getUpvotes(), question.getDownvotes());
            
            // Find and update the card in the UI
            for (Component component : feedPanel.getComponents()) {
                if (component instanceof QuestionCard card && card.getQuestion().getId().equals(question.getId())) {
                    card.updateVoteCount();
                    break;
                }
            }
            
            // Show feedback based on vote result
            String message = switch (voteResult) {
                case CREATED -> "Downvoted!";
                case UPDATED -> "Changed to downvote!";
                case REMOVED -> "Downvote removed!";
            };
            LOGGER.info("Vote action: " + message + " for question " + question.getId());
            
            if (feedListener != null) {
                feedListener.onQuestionVoted(question);
            }
        } catch (SQLException e) {
            LOGGER.logException("Failed to downvote question", e);
            showErrorMessage("Failed to downvote question. Please try again later.");
        }
    }
    
    /**
     * Interface for listening to feed events.
     */
    public interface QuestionFeedListener {
        /**
         * Called when a question is selected from the feed.
         * 
         * @param question The selected question
         */
        void onQuestionSelected(Question question);
        
        /**
         * Called when a question is voted on (upvoted or downvoted).
         * 
         * @param question The voted question
         */
        void onQuestionVoted(Question question);
    }
}