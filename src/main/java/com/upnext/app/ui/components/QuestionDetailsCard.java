package com.upnext.app.ui.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.SQLException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.upnext.app.App;
import com.upnext.app.core.Logger;
import com.upnext.app.data.question.QuestionRepository;
import com.upnext.app.domain.question.Question;
import com.upnext.app.ui.navigation.ViewNavigator;
import com.upnext.app.ui.theme.AppTheme;


/**
 * A reusable component that displays detailed question information in a card format.
 * Designed for use in left panels or sidebar layouts to show question metadata,
 * subject information, tags, and user details.
 */
public class QuestionDetailsCard extends JPanel {
    private static final Logger LOGGER = Logger.getInstance();
    
    // Layout constants
    private static final int PADDING_MEDIUM = 16;
    private static final int PADDING_SMALL = 8;
    private static final int PADDING_TINY = 4;
    
    // UI components
    private final JLabel subjectLabel;
    private final JPanel tagsPanel;
    private final JPanel userInfoPanel;
    private final JLabel userAvatarLabel;
    private final JLabel usernameLabel;
    private final JLabel postDateLabel;
    private final JLabel viewCountLabel;
    private final JLabel voteCountLabel;
    
    // Data
    private Question currentQuestion;
    private final QuestionRepository questionRepository;
    
    /**
     * Creates a new question details card.
     */
    public QuestionDetailsCard() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(true);
        setBackground(AppTheme.SURFACE);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xE0E0E0), 1),
            new EmptyBorder(PADDING_MEDIUM, PADDING_MEDIUM, PADDING_MEDIUM, PADDING_MEDIUM)
        ));
        
        questionRepository = QuestionRepository.getInstance();
        
        // Create header
        JLabel headerLabel = new JLabel("Question Details");
        headerLabel.setFont(AppTheme.HEADING_FONT.deriveFont(Font.BOLD, 16f));
        headerLabel.setForeground(AppTheme.TEXT_PRIMARY);
        headerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        add(headerLabel);
        add(Box.createRigidArea(new Dimension(0, PADDING_MEDIUM)));
        
        // Subject section
        JLabel subjectTitleLabel = new JLabel("Subject:");
        subjectTitleLabel.setFont(AppTheme.PRIMARY_FONT.deriveFont(Font.BOLD, 12f));
        subjectTitleLabel.setForeground(AppTheme.TEXT_SECONDARY);
        subjectTitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        subjectLabel = new JLabel();
        subjectLabel.setFont(AppTheme.PRIMARY_FONT);
        subjectLabel.setForeground(AppTheme.TEXT_PRIMARY);
        subjectLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        add(subjectTitleLabel);
        add(Box.createRigidArea(new Dimension(0, PADDING_TINY)));
        add(subjectLabel);
        add(Box.createRigidArea(new Dimension(0, PADDING_MEDIUM)));
        
        // Tags section
        JLabel tagsLabelTitle = new JLabel("Tags:");
        tagsLabelTitle.setFont(AppTheme.PRIMARY_FONT.deriveFont(Font.BOLD, 12f));
        tagsLabelTitle.setForeground(AppTheme.TEXT_SECONDARY);
        tagsLabelTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        tagsPanel = new JPanel();
        tagsPanel.setLayout(new BoxLayout(tagsPanel, BoxLayout.Y_AXIS));
        tagsPanel.setOpaque(false);
        tagsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        add(tagsLabelTitle);
        add(Box.createRigidArea(new Dimension(0, PADDING_TINY)));
        add(tagsPanel);
        add(Box.createRigidArea(new Dimension(0, PADDING_MEDIUM)));
        
        // User info section
        JLabel userInfoTitle = new JLabel("Asked by:");
        userInfoTitle.setFont(AppTheme.PRIMARY_FONT.deriveFont(Font.BOLD, 12f));
        userInfoTitle.setForeground(AppTheme.TEXT_SECONDARY);
        userInfoTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        userInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        userInfoPanel.setOpaque(false);
        userInfoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // User avatar (clickable)
        userAvatarLabel = new JLabel("👤");
        userAvatarLabel.setFont(AppTheme.PRIMARY_FONT.deriveFont(18f));
        userAvatarLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        userAvatarLabel.setToolTipText("View user profile");
        
        // Username (clickable)
        usernameLabel = new JLabel();
        usernameLabel.setFont(AppTheme.PRIMARY_FONT.deriveFont(Font.BOLD));
        usernameLabel.setForeground(AppTheme.ACCENT);
        usernameLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        userInfoPanel.add(userAvatarLabel);
        userInfoPanel.add(Box.createRigidArea(new Dimension(PADDING_SMALL, 0)));
        userInfoPanel.add(usernameLabel);
        
        add(userInfoTitle);
        add(Box.createRigidArea(new Dimension(0, PADDING_TINY)));
        add(userInfoPanel);
        add(Box.createRigidArea(new Dimension(0, PADDING_SMALL)));
        
    // Post date
    postDateLabel = new JLabel();
        postDateLabel.setFont(AppTheme.PRIMARY_FONT.deriveFont(Font.ITALIC, 11f));
        postDateLabel.setForeground(AppTheme.TEXT_SECONDARY);
        postDateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        add(postDateLabel);
        add(Box.createRigidArea(new Dimension(0, PADDING_MEDIUM)));
        
    // Statistics section removed as per updated design requirements
    viewCountLabel = new JLabel();
    voteCountLabel = new JLabel();
        
        // Add glue to push content to top
        add(Box.createVerticalGlue());
        
        // Set preferred size for consistent layout
        setPreferredSize(new Dimension(280, 400));
        setMinimumSize(new Dimension(250, 300));
    }
    
    /**
     * Updates the card with question information.
     * 
     * @param question The question to display
     */
    public void setQuestion(Question question) {
        if (question == null) {
            clearContent();
            return;
        }
        
        this.currentQuestion = question;
        
        // Update subject
        String subjectText = question.getSubjectName();
        if (subjectText == null || subjectText.trim().isEmpty()) {
            subjectText = "General";
        }
        subjectLabel.setText(subjectText);
        
        // Update tags
        loadAndDisplayTags(question.getId());
        
        // Update user info
        String username = question.getUserName();
        if (username == null || username.trim().isEmpty()) {
            username = "Anonymous";
        }
        usernameLabel.setText(username);
        
        // Add click handlers for user profile navigation
        addUserProfileClickHandlers(question.getUserId(), username);
        
        // Update post date (use java.time formatting)
        try {
            java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy");
            String formattedDate = question.getCreatedAt() != null
                    ? dtf.format(question.getCreatedAt())
                    : "unknown date";
            postDateLabel.setText("Posted on " + formattedDate);
        } catch (Exception ex) {
            postDateLabel.setText("Posted on unknown date");
        }
        
        // Statistics are intentionally not displayed in the details card
        
        revalidate();
        repaint();
    }
    
    /**
     * Loads and displays tags for the question.
     * 
     * @param questionId The question ID
     */
    private void loadAndDisplayTags(Long questionId) {
        tagsPanel.removeAll();
        
        try {
            List<String> tags = questionRepository.getTagsForQuestion(questionId);
            
            if (tags.isEmpty()) {
                JLabel noTagsLabel = new JLabel("No tags");
                noTagsLabel.setFont(AppTheme.PRIMARY_FONT.deriveFont(Font.ITALIC, 11f));
                noTagsLabel.setForeground(AppTheme.TEXT_SECONDARY);
                noTagsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                tagsPanel.add(noTagsLabel);
            } else {
                // Create a flow panel for tags
                JPanel tagFlowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, PADDING_TINY, PADDING_TINY));
                tagFlowPanel.setOpaque(false);
                tagFlowPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
                
                for (String tag : tags) {
                    JLabel tagChip = createTagChip(tag);
                    tagFlowPanel.add(tagChip);
                }
                
                tagsPanel.add(tagFlowPanel);
            }
            
        } catch (SQLException e) {
            LOGGER.logException("Error loading tags for question: " + questionId, e);
            JLabel errorLabel = new JLabel("Error loading tags");
            errorLabel.setFont(AppTheme.PRIMARY_FONT.deriveFont(Font.ITALIC, 11f));
            errorLabel.setForeground(Color.RED);
            errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            tagsPanel.add(errorLabel);
        }
    }
    
    /**
     * Creates a styled tag chip.
     * 
     * @param tag The tag text
     * @return A styled tag chip component
     */
    private JLabel createTagChip(String tag) {
        JLabel tagChip = new JLabel(tag);
        tagChip.setFont(AppTheme.PRIMARY_FONT.deriveFont(10f));
        tagChip.setForeground(AppTheme.ACCENT);
        tagChip.setOpaque(true);
        tagChip.setBackground(new Color(0xF0F8FF));
        tagChip.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppTheme.ACCENT.brighter(), 1),
            new EmptyBorder(2, 4, 2, 4)
        ));
        tagChip.setCursor(new Cursor(Cursor.HAND_CURSOR));
        tagChip.setToolTipText("Click to filter by tag: " + tag);
        
        // Add hover effect
        tagChip.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                tagChip.setBackground(AppTheme.ACCENT.brighter());
                tagChip.setForeground(Color.WHITE);
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                tagChip.setBackground(new Color(0xF0F8FF));
                tagChip.setForeground(AppTheme.ACCENT);
            }
            
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                // In a full implementation, this would filter questions by tag
                LOGGER.info("Tag clicked in details card: " + tag);
                // Could navigate back to home with tag filter applied
            }
        });
        
        return tagChip;
    }
    
    /**
     * Adds click handlers for user profile navigation.
     * 
     * @param userId The user ID
     * @param username The username
     */
    private void addUserProfileClickHandlers(Long userId, String username) {
        // Remove existing listeners
        for (var listener : userAvatarLabel.getMouseListeners()) {
            userAvatarLabel.removeMouseListener(listener);
        }
        for (var listener : usernameLabel.getMouseListeners()) {
            usernameLabel.removeMouseListener(listener);
        }
        
        // Add new listeners
        java.awt.event.MouseAdapter profileClickHandler = new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                navigateToUserProfile(userId, username);
            }
            
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (evt.getSource() == usernameLabel) {
                    usernameLabel.setForeground(AppTheme.PRIMARY);
                }
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (evt.getSource() == usernameLabel) {
                    usernameLabel.setForeground(AppTheme.ACCENT);
                }
            }
        };
        
        userAvatarLabel.addMouseListener(profileClickHandler);
        usernameLabel.addMouseListener(profileClickHandler);
    }
    
    /**
     * Navigates to the user profile page.
     * 
     * @param userId The user ID
     * @param username The username
     */
    private void navigateToUserProfile(Long userId, String username) {
        LOGGER.info("Navigate to user profile: " + username + " (ID: " + userId + ")");
        // Navigate to the ProfileLayout system to show user profile
        ViewNavigator.getInstance().navigateTo(App.PROFILE_LAYOUT_SCREEN);
    }
    
    /**
     * Clears all content from the card.
     */
    private void clearContent() {
        currentQuestion = null;
        subjectLabel.setText("");
        tagsPanel.removeAll();
        usernameLabel.setText("");
        postDateLabel.setText("");
        viewCountLabel.setText("");
        voteCountLabel.setText("");
        
        revalidate();
        repaint();
    }
    
    /**
     * Gets the currently displayed question.
     * 
     * @return The current question, or null if none is set
     */
    public Question getCurrentQuestion() {
        return currentQuestion;
    }
}