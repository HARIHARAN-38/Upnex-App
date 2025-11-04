package com.upnext.app.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import com.upnext.app.App;
import com.upnext.app.core.Logger;
import com.upnext.app.domain.User;
import com.upnext.app.service.AuthService;
import com.upnext.app.ui.navigation.ViewNavigator;
import com.upnext.app.ui.theme.AppTheme;

/**
 * Component displaying user profile summary information and metrics.
 * Shows avatar, username, member since date, and activity metrics
 * (questions asked, answers given, total upvotes).
 */
public class ProfileSummaryCard extends JPanel {

    // UI Components
    private final JLabel avatarLabel;
    private final JLabel nameLabel;
    private final JLabel memberSinceLabel;
    private final JLabel questionsAskedLabel;
    private final JLabel questionsAskedCountLabel;
    private final JLabel answersGivenLabel;
    private final JLabel answersGivenCountLabel;
    private final JLabel upvotesLabel;
    private final JLabel upvotesCountLabel;
    private final JPanel metricsPanel;
    private final JPanel loadingPanel;
    
    // Constants
    private static final int AVATAR_SIZE = 64;
    private static final int CARD_PADDING = 16;

    /**
     * Creates a new ProfileSummaryCard for displaying user information
     */
    public ProfileSummaryCard() {
        setLayout(new BorderLayout(0, 12));
        setOpaque(true);
        setBackground(AppTheme.SURFACE);
        setBorder(new CompoundBorder(
                new MatteBorder(1, 1, 1, 1, new Color(0xE2E8F0)),
                new EmptyBorder(CARD_PADDING, CARD_PADDING, CARD_PADDING, CARD_PADDING)
        ));

        // Create the header with avatar and name
        JPanel headerPanel = new JPanel(new BorderLayout(12, 0));
        headerPanel.setOpaque(false);
        
        // Avatar placeholder (circular)
        avatarLabel = createAvatarLabel();
        
        // User name and member since info
        JPanel userInfoPanel = new JPanel();
        userInfoPanel.setLayout(new BoxLayout(userInfoPanel, BoxLayout.Y_AXIS));
        userInfoPanel.setOpaque(false);
        
        nameLabel = new JLabel("...");
        nameLabel.setFont(AppTheme.HEADING_FONT);
        nameLabel.setForeground(AppTheme.TEXT_PRIMARY);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        memberSinceLabel = new JLabel("Member since ...");
        memberSinceLabel.setFont(AppTheme.PRIMARY_FONT);
        memberSinceLabel.setForeground(AppTheme.TEXT_SECONDARY);
        memberSinceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        userInfoPanel.add(nameLabel);
        userInfoPanel.add(Box.createVerticalStrut(4));
        userInfoPanel.add(memberSinceLabel);
        
        headerPanel.add(avatarLabel, BorderLayout.WEST);
        headerPanel.add(userInfoPanel, BorderLayout.CENTER);
        
        // Divider under header
        JSeparator divider = new JSeparator();
        divider.setForeground(new Color(0xE2E8F0));
        divider.setBorder(new EmptyBorder(12, 0, 12, 0));
        
        // Metrics section with 3 key metrics
        metricsPanel = new JPanel(new GridLayout(3, 2, 8, 12));
        metricsPanel.setOpaque(false);
        
        // Questions metric
        questionsAskedLabel = new JLabel("Questions Asked");
        questionsAskedLabel.setFont(AppTheme.PRIMARY_FONT);
        questionsAskedLabel.setForeground(AppTheme.TEXT_SECONDARY);
        
        questionsAskedCountLabel = new JLabel("...");
        questionsAskedCountLabel.setFont(AppTheme.HEADING_FONT);
        questionsAskedCountLabel.setForeground(AppTheme.PRIMARY);
        
        // Answers metric
        answersGivenLabel = new JLabel("Answers Given");
        answersGivenLabel.setFont(AppTheme.PRIMARY_FONT);
        answersGivenLabel.setForeground(AppTheme.TEXT_SECONDARY);
        
        answersGivenCountLabel = new JLabel("...");
        answersGivenCountLabel.setFont(AppTheme.HEADING_FONT);
        answersGivenCountLabel.setForeground(AppTheme.PRIMARY);
        
        // Upvotes metric
        upvotesLabel = new JLabel("Total Upvotes");
        upvotesLabel.setFont(AppTheme.PRIMARY_FONT);
        upvotesLabel.setForeground(AppTheme.TEXT_SECONDARY);
        
        upvotesCountLabel = new JLabel("...");
        upvotesCountLabel.setFont(AppTheme.HEADING_FONT);
        upvotesCountLabel.setForeground(AppTheme.PRIMARY);
        
        metricsPanel.add(questionsAskedCountLabel);
        metricsPanel.add(questionsAskedLabel);
        metricsPanel.add(answersGivenCountLabel);
        metricsPanel.add(answersGivenLabel);
        metricsPanel.add(upvotesCountLabel);
        metricsPanel.add(upvotesLabel);
        
        // Create loading panel for initial state
        loadingPanel = new JPanel();
        loadingPanel.setLayout(new BoxLayout(loadingPanel, BoxLayout.Y_AXIS));
        loadingPanel.setOpaque(false);
        loadingPanel.setVisible(false);
        
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 12));
        
        JLabel loadingLabel = new JLabel("Loading profile data...");
        loadingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        loadingLabel.setFont(AppTheme.PRIMARY_FONT);
        loadingLabel.setForeground(AppTheme.TEXT_SECONDARY);
        
        loadingPanel.add(Box.createVerticalGlue());
        loadingPanel.add(progressBar);
        loadingPanel.add(Box.createVerticalStrut(8));
        loadingPanel.add(loadingLabel);
        loadingPanel.add(Box.createVerticalGlue());
        
        // Footer with "View Profile" link
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.setOpaque(false);
        
        JLabel viewProfileLink = new JLabel("View Full Profile");
        viewProfileLink.setFont(AppTheme.PRIMARY_FONT);
        viewProfileLink.setForeground(AppTheme.PRIMARY);
        viewProfileLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewProfileLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Navigate to the profile system
                try {
                    Logger.getInstance().info("Navigating to profile system from profile card");
                    ViewNavigator.getInstance().navigateTo(App.PROFILE_LAYOUT_SCREEN);
                } catch (Exception ex) {
                    Logger.getInstance().error("Failed to navigate to profile system: " + ex.getMessage());
                    JOptionPane.showMessageDialog(
                        ProfileSummaryCard.this,
                        "Unable to open profile page. Please try again.",
                        "Navigation Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                viewProfileLink.setText("<html><u>View Full Profile</u></html>");
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                viewProfileLink.setText("View Full Profile");
            }
        });
        
        footerPanel.add(viewProfileLink);
        
        // Add all panels to the main layout
        add(headerPanel, BorderLayout.NORTH);
        add(divider, BorderLayout.CENTER);
        
        JPanel contentPanel = new JPanel(new BorderLayout(0, 12));
        contentPanel.setOpaque(false);
        contentPanel.add(metricsPanel, BorderLayout.CENTER);
        contentPanel.add(footerPanel, BorderLayout.SOUTH);
        
        add(contentPanel, BorderLayout.SOUTH);
        add(loadingPanel, BorderLayout.CENTER);
        
        // Initialize with empty state (done inline to avoid overridable method call)
        nameLabel.setText("Not Signed In");
        memberSinceLabel.setText("Sign in to view your profile");
        avatarLabel.setText("?");
        
        questionsAskedCountLabel.setText("0");
        answersGivenCountLabel.setText("0");
        upvotesCountLabel.setText("0");
        
        loadingPanel.setVisible(false);
        metricsPanel.setVisible(true);
    }
    
    /**
     * Creates a circular avatar label with placeholder or user initials
     * 
     * @return Avatar label component
     */
    private JLabel createAvatarLabel() {
        JLabel label = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Get the current text (could be initials)
                String text = getText();
                
                // Draw circular background
                g2.setColor(AppTheme.PRIMARY);
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                
                // Draw text if it exists
                if (text != null && !text.isEmpty()) {
                    g2.setColor(Color.WHITE);
                    g2.setFont(AppTheme.HEADING_FONT.deriveFont(Font.BOLD, 24f));
                    
                    FontMetrics fm = g2.getFontMetrics();
                    int textWidth = fm.stringWidth(text);
                    int textHeight = fm.getHeight();
                    
                    int x = (getWidth() - textWidth) / 2;
                    int y = (getHeight() - textHeight) / 2 + fm.getAscent();
                    
                    g2.drawString(text, x, y);
                }
                
                g2.dispose();
            }
            
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(AVATAR_SIZE, AVATAR_SIZE);
            }
        };
        
        return label;
    }
    
    /**
     * Updates the profile card with data from the current user
     */
    public void updateFromCurrentUser() {
        User user = AuthService.getInstance().getCurrentUser();
        if (user != null) {
            updateFromUser(user);
        } else {
            showEmptyState();
        }
    }
    
    /**
     * Updates the profile card with data from a specific user
     * 
     * @param user The user to display data for
     */
    public void updateFromUser(User user) {
        if (user == null) {
            showEmptyState();
            return;
        }
        
        // Hide loading panel if it was visible
        loadingPanel.setVisible(false);
        metricsPanel.setVisible(true);
        
        // Set user name
        nameLabel.setText(user.getName());
        
        // Set member since date (formatted nicely)
        String memberSince = formatMemberSince(user.getCreatedAt());
        memberSinceLabel.setText("Member since " + memberSince);
        
        // Set avatar with initials
        avatarLabel.setText(getInitials(user.getName()));
        
        // Set metrics
        questionsAskedCountLabel.setText(String.valueOf(user.getQuestionsAsked()));
        answersGivenCountLabel.setText(String.valueOf(user.getAnswersGiven()));
        upvotesCountLabel.setText(String.valueOf(user.getTotalUpvotes()));
    }
    
    /**
     * Shows the loading state while waiting for user data
     */
    public void showLoadingState() {
        nameLabel.setText("Loading...");
        memberSinceLabel.setText("Retrieving user data");
        avatarLabel.setText("");
        
        metricsPanel.setVisible(false);
        loadingPanel.setVisible(true);
    }
    
    /**
     * Shows an empty state when no user is logged in
     */
    public void showEmptyState() {
        nameLabel.setText("Not Signed In");
        memberSinceLabel.setText("Sign in to view your profile");
        avatarLabel.setText("?");
        
        questionsAskedCountLabel.setText("0");
        answersGivenCountLabel.setText("0");
        upvotesCountLabel.setText("0");
        
        loadingPanel.setVisible(false);
        metricsPanel.setVisible(true);
    }
    
    /**
     * Gets the initials from a name (first letter of first and last name)
     * 
     * @param name The full name
     * @return Up to two initials
     */
    private String getInitials(String name) {
        if (name == null || name.isEmpty()) {
            return "?";
        }
        
        StringBuilder initials = new StringBuilder();
        String[] parts = name.trim().split("\\s+");
        
        // Add first letter of first name
        if (parts.length > 0 && !parts[0].isEmpty()) {
            initials.append(Character.toUpperCase(parts[0].charAt(0)));
        }
        
        // Add first letter of last name if available
        if (parts.length > 1 && !parts[parts.length - 1].isEmpty()) {
            initials.append(Character.toUpperCase(parts[parts.length - 1].charAt(0)));
        }
        
        return initials.toString();
    }
    
    /**
     * Formats the member since date in a user-friendly format
     * 
     * @param createdAt The timestamp string from the database
     * @return A formatted date string (e.g., "October 2025")
     */
    private String formatMemberSince(String createdAt) {
        if (createdAt == null || createdAt.isEmpty()) {
            return "Unknown";
        }
        
        try {
            // Try to parse standard SQL timestamp format
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = inputFormat.parse(createdAt.split("\\.")[0]); // Remove milliseconds if present
            
            // Format for display
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM yyyy");
            return outputFormat.format(date);
        } catch (ParseException e) {
            // Try alternate format if standard fails
            try {
                if (createdAt.contains("T")) {
                    // Try ISO format with T separator
                    LocalDate date = LocalDate.parse(createdAt.split("T")[0]);
                    return date.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
                }
                return "Unknown";
            } catch (Exception ex) {
                return "Unknown";
            }
        }
    }
}