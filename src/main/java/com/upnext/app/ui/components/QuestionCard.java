package com.upnext.app.ui.components;

// No need for Logger in this class
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.upnext.app.domain.question.Question;
import com.upnext.app.ui.theme.AppTheme;

/**
 * Card component that displays a question with metadata and vote controls.
 * Used in the question feed to show question previews.
 */
public class QuestionCard extends JPanel {
    // Logger removed as it's not needed in this class
    private static final int PADDING = 12;
    private static final int INNER_PADDING = 8;
    private static final int MAX_TITLE_LENGTH = 100;
    private static final int MAX_CONTENT_PREVIEW = 120;
    
    // Formatter for displaying relative time
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy");
    
    // UI Components
    private JLabel titleLabel;
    private JLabel contentPreview;
    private JLabel metadataLabel;
    private JButton upvoteButton;
    private JButton downvoteButton;
    private JLabel voteCountLabel;
    private JPanel tagsPanel;
    private static final int ICON_SIZE = 18;
    
    // Model
    private Question question;
    private QuestionCardListener listener;
    
    /**
     * Creates a new QuestionCard component.
     * 
     * @param question The question to display
     */
    public QuestionCard(Question question) {
        this.question = question;
        
        // Set up the layout and appearance
        setLayout(new BorderLayout(0, INNER_PADDING));
        setBackground(AppTheme.SURFACE);
        setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0xE2E8F0)),
                new EmptyBorder(PADDING, PADDING, PADDING, PADDING)));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Vote panel (left side)
        JPanel votePanel = createVotePanel();
        
        // Content panel (center)
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        
        // Title
        titleLabel = new JLabel();
        titleLabel.setFont(AppTheme.HEADING_FONT.deriveFont(16f));
        titleLabel.setForeground(AppTheme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Content preview
        contentPreview = new JLabel();
        contentPreview.setFont(AppTheme.PRIMARY_FONT);
        contentPreview.setForeground(AppTheme.TEXT_SECONDARY);
        contentPreview.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Tags panel
        tagsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        tagsPanel.setOpaque(false);
        tagsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Metadata
        metadataLabel = new JLabel();
        metadataLabel.setFont(AppTheme.PRIMARY_FONT.deriveFont(12f));
        metadataLabel.setForeground(AppTheme.TEXT_SECONDARY);
        metadataLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        metadataLabel.setBorder(new EmptyBorder(INNER_PADDING, 0, 0, 0));
        
        // Add components to content panel
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, INNER_PADDING)));
        contentPanel.add(contentPreview);
        contentPanel.add(Box.createRigidArea(new Dimension(0, INNER_PADDING)));
        contentPanel.add(tagsPanel);
        contentPanel.add(metadataLabel);
        
        // Add main panels to card
        add(votePanel, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
        
        // Click handler for the card
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (listener != null && question != null) {
                    listener.onQuestionSelected(question);
                }
            }
        });
        
        // Initialize with data
        if (question != null) {
            updateCard(question);
        }
    }
    
    /**
     * Creates the vote panel with upvote/downvote buttons and count.
     * 
     * @return The vote panel
     */
    private JPanel createVotePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 0, INNER_PADDING));
        
    // Upvote button (icon from resources)
    upvoteButton = new JButton();
    upvoteButton.setIcon(loadIcon("/ui/icons/upvote.png", ICON_SIZE, ICON_SIZE));
        upvoteButton.setBorderPainted(false);
        upvoteButton.setContentAreaFilled(false);
        upvoteButton.setFocusPainted(false);
        upvoteButton.setMargin(new Insets(0, 0, 0, 0));
        upvoteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        upvoteButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Upvote action
    upvoteButton.addActionListener(event -> {
            if (question != null && listener != null) {
                listener.onUpvote(question);
            }
            // ActionEvent doesn't propagate by default, so no need to stop it
        });
        
        // Vote count
        voteCountLabel = new JLabel("0", SwingConstants.CENTER);
        voteCountLabel.setFont(AppTheme.PRIMARY_FONT.deriveFont(Font.BOLD, 14f));
        voteCountLabel.setForeground(AppTheme.TEXT_PRIMARY);
        voteCountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
    // Downvote button (icon from resources)
    downvoteButton = new JButton();
    downvoteButton.setIcon(loadIcon("/ui/icons/downvote.png", ICON_SIZE, ICON_SIZE));
        downvoteButton.setBorderPainted(false);
        downvoteButton.setContentAreaFilled(false);
        downvoteButton.setFocusPainted(false);
        downvoteButton.setMargin(new Insets(0, 0, 0, 0));
        downvoteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        downvoteButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Downvote action
    downvoteButton.addActionListener(event -> {
            if (question != null && listener != null) {
                listener.onDownvote(question);
            }
            // ActionEvent doesn't propagate by default, so no need to stop it
        });
        
        // Add components to vote panel
        panel.add(upvoteButton);
        panel.add(Box.createRigidArea(new Dimension(0, 2)));
        panel.add(voteCountLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 2)));
        panel.add(downvoteButton);
        
        return panel;
    }

    private static ImageIcon loadIcon(String resourcePath, int w, int h) {
        java.net.URL url = QuestionCard.class.getResource(resourcePath);
        if (url == null) {
            return new ImageIcon();
        }
        ImageIcon raw = new ImageIcon(url);
        Image scaled = raw.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }
    
    /**
     * Updates the card with new question data.
     * 
     * @param question The question to display
     */
    public void updateCard(Question question) {
        this.question = question;
        
        if (question == null) {
            return;
        }
        
        // Set title (truncate if too long)
        String title = question.getTitle();
        if (title.length() > MAX_TITLE_LENGTH) {
            title = title.substring(0, MAX_TITLE_LENGTH - 3) + "...";
        }
        titleLabel.setText(title);
        
        // Set content preview (truncate if too long)
        String content = question.getContent();
        if (content.length() > MAX_CONTENT_PREVIEW) {
            content = content.substring(0, MAX_CONTENT_PREVIEW - 3) + "...";
        }
        contentPreview.setText(content);
        
        // Set vote count
        int voteCount = question.getUpvotes() - question.getDownvotes();
        voteCountLabel.setText(String.valueOf(voteCount));
        
        // Set metadata
        StringBuilder metadataBuilder = new StringBuilder();
        
        // Add subject if available
        if (question.getSubjectName() != null) {
            metadataBuilder.append("in ").append(question.getSubjectName()).append(" • ");
        }
        
        // Add answer count and solved status
        metadataBuilder.append(question.getAnswerCount()).append(" ");
        metadataBuilder.append(question.getAnswerCount() == 1 ? "answer" : "answers");
        
        if (question.isSolved()) {
            metadataBuilder.append(" • Solved");
        }
        
        // Add date
        metadataBuilder.append(" • ").append(formatDate(question.getCreatedAt()));
        
        metadataLabel.setText(metadataBuilder.toString());
        
        // Set tags
        updateTags(question.getTags());
    }
    
    /**
     * Updates the tags display in the card.
     * 
     * @param tags The list of tags to display
     */
    private void updateTags(List<String> tags) {
        tagsPanel.removeAll();
        
        if (tags != null && !tags.isEmpty()) {
            for (String tagName : tags) {
                JLabel tagLabel = createTagLabel(tagName);
                tagsPanel.add(tagLabel);
                
                // Limit the number of tags displayed
                if (tagsPanel.getComponentCount() >= 3) {
                    int remaining = tags.size() - 3;
                    if (remaining > 0) {
                        JLabel moreLabel = new JLabel("+" + remaining + " more");
                        moreLabel.setFont(AppTheme.PRIMARY_FONT.deriveFont(11f));
                        moreLabel.setForeground(AppTheme.TEXT_SECONDARY);
                        tagsPanel.add(moreLabel);
                        break;
                    }
                }
            }
        }
        
        tagsPanel.revalidate();
        tagsPanel.repaint();
    }
    
    /**
     * Creates a styled label for a tag.
     * 
     * @param tagName The name of the tag
     * @return A styled JLabel for the tag
     */
    private JLabel createTagLabel(String tagName) {
        JLabel tagLabel = new JLabel(tagName);
        tagLabel.setFont(AppTheme.PRIMARY_FONT.deriveFont(11f));
        tagLabel.setForeground(AppTheme.TEXT_PRIMARY);
        tagLabel.setBackground(new Color(0xF0F4F8));
        tagLabel.setOpaque(true);
        tagLabel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0xE2E8F0), 1),
                new EmptyBorder(2, 5, 2, 5)));
        
        return tagLabel;
    }
    
    /**
     * Formats a date in a user-friendly way (relative time or actual date).
     * 
     * @param dateTime The date to format
     * @return A formatted date string
     */
    private String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        
        LocalDateTime now = LocalDateTime.now();
        long minutesAgo = ChronoUnit.MINUTES.between(dateTime, now);
        
        if (minutesAgo < 1) {
            return "just now";
        } else if (minutesAgo < 60) {
            return minutesAgo + (minutesAgo == 1 ? " minute ago" : " minutes ago");
        } else {
            long hoursAgo = ChronoUnit.HOURS.between(dateTime, now);
            if (hoursAgo < 24) {
                return hoursAgo + (hoursAgo == 1 ? " hour ago" : " hours ago");
            } else {
                long daysAgo = ChronoUnit.DAYS.between(dateTime, now);
                if (daysAgo < 7) {
                    return daysAgo + (daysAgo == 1 ? " day ago" : " days ago");
                } else {
                    return dateTime.format(DATE_FORMATTER);
                }
            }
        }
    }
    
    /**
     * Updates the vote count in the UI.
     * This method should be called after a successful vote operation.
     */
    public void updateVoteCount() {
        if (question != null) {
            int voteCount = question.getUpvotes() - question.getDownvotes();
            voteCountLabel.setText(String.valueOf(voteCount));
        }
    }
    
    /**
     * Sets the listener for card interactions.
     * 
     * @param listener The listener to set
     */
    public void setListener(QuestionCardListener listener) {
        this.listener = listener;
    }
    
    /**
     * Gets the question displayed by this card.
     * 
     * @return The question object
     */
    public Question getQuestion() {
        return question;
    }
    
    /**
     * Interface for listening to question card interactions.
     */
    public interface QuestionCardListener {
        /**
         * Called when a question card is clicked.
         * 
         * @param question The question that was selected
         */
        void onQuestionSelected(Question question);
        
        /**
         * Called when the upvote button is clicked.
         * 
         * @param question The question that was upvoted
         */
        void onUpvote(Question question);
        
        /**
         * Called when the downvote button is clicked.
         * 
         * @param question The question that was downvoted
         */
        void onDownvote(Question question);
    }
}