package com.upnext.app.ui.screens;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import com.upnext.app.ui.theme.AppTheme;

/**
 * Help screen providing guidance and FAQ content for using the Upnex application.
 * Features collapsible sections for different help topics including account management,
 * application usage, and technical support information.
 */
public class HelpScreen extends JPanel {
    
    // Constants
    private static final int PADDING_LARGE = 24;
    private static final int PADDING_MEDIUM = 16;
    private static final int PADDING_SMALL = 8;
    private static final int SECTION_SPACING = 20;
    
    // UI Components
    private final JScrollPane scrollPane;
    
    /**
     * Creates a new HelpScreen with collapsible FAQ sections.
     */
    public HelpScreen() {
        setLayout(new BorderLayout());
        setBackground(AppTheme.BACKGROUND);
        
        // Create main content panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(AppTheme.BACKGROUND);
        mainPanel.setBorder(new EmptyBorder(PADDING_LARGE, PADDING_LARGE, PADDING_LARGE, PADDING_LARGE));
        
        // Create page header
        JPanel headerPanel = createPageHeader();
        headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Create help sections
        JPanel accountSection = createHelpSection("Account and Profile", getAccountHelpContent());
        accountSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPanel usageSection = createHelpSection("Using the Application", getUsageHelpContent());
        usageSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPanel supportSection = createHelpSection("Technical Support", getSupportHelpContent());
        supportSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Add components with spacing
        mainPanel.add(headerPanel);
        mainPanel.add(Box.createVerticalStrut(SECTION_SPACING * 2));
        mainPanel.add(accountSection);
        mainPanel.add(Box.createVerticalStrut(SECTION_SPACING));
        mainPanel.add(usageSection);
        mainPanel.add(Box.createVerticalStrut(SECTION_SPACING));
        mainPanel.add(supportSection);
        mainPanel.add(Box.createVerticalGlue());
        
        // Wrap in scroll pane
        scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        add(scrollPane, BorderLayout.CENTER);
    }
    
    /**
     * Creates the page header with title and subtitle.
     * 
     * @return Page header panel
     */
    private JPanel createPageHeader() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        
        // Title
        JLabel titleLabel = new JLabel("Help Center");
        titleLabel.setFont(AppTheme.HEADING_FONT.deriveFont(Font.BOLD, 28f));
        titleLabel.setForeground(AppTheme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Subtitle
        JLabel subtitleLabel = new JLabel("Find answers and guidance for using Upnex");
        subtitleLabel.setFont(AppTheme.PRIMARY_FONT.deriveFont(Font.PLAIN, 16f));
        subtitleLabel.setForeground(AppTheme.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(PADDING_SMALL));
        headerPanel.add(subtitleLabel);
        
        return headerPanel;
    }
    
    /**
     * Creates a collapsible help section with title and content.
     * 
     * @param title The section title
     * @param content The section content
     * @return Help section panel
     */
    private JPanel createHelpSection(String title, String content) {
        JPanel sectionPanel = new JPanel();
        sectionPanel.setLayout(new BoxLayout(sectionPanel, BoxLayout.Y_AXIS));
        sectionPanel.setOpaque(false);
        sectionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        
        // Create expandable header
        JPanel headerPanel = createSectionHeader(title);
        headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Create content area
        JPanel contentPanel = createSectionContent(content);
        contentPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Make the section collapsible
        final boolean[] isExpanded = {true}; // Start expanded
        
        headerPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                isExpanded[0] = !isExpanded[0];
                contentPanel.setVisible(isExpanded[0]);
                
                // Update arrow indicator
                JLabel arrowLabel = (JLabel) headerPanel.getComponent(1);
                arrowLabel.setText(isExpanded[0] ? "▼" : "▶");
                
                sectionPanel.revalidate();
                sectionPanel.repaint();
            }
        });
        
        sectionPanel.add(headerPanel);
        sectionPanel.add(Box.createVerticalStrut(PADDING_SMALL));
        sectionPanel.add(contentPanel);
        
        return sectionPanel;
    }
    
    /**
     * Creates a section header with title and expand/collapse arrow.
     * 
     * @param title The section title
     * @return Section header panel
     */
    private JPanel createSectionHeader(String title) {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(true);
        headerPanel.setBackground(AppTheme.SURFACE);
        headerPanel.setBorder(new CompoundBorder(
            new MatteBorder(1, 1, 1, 1, new Color(0xE2E8F0)),
            new EmptyBorder(PADDING_MEDIUM, PADDING_MEDIUM, PADDING_MEDIUM, PADDING_MEDIUM)
        ));
        headerPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        
        // Title label
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(AppTheme.HEADING_FONT.deriveFont(Font.BOLD, 18f));
        titleLabel.setForeground(AppTheme.TEXT_PRIMARY);
        
        // Arrow indicator
        JLabel arrowLabel = new JLabel("▼");
        arrowLabel.setFont(AppTheme.PRIMARY_FONT.deriveFont(Font.BOLD, 14f));
        arrowLabel.setForeground(AppTheme.PRIMARY);
        arrowLabel.setHorizontalAlignment(SwingConstants.CENTER);
        arrowLabel.setPreferredSize(new Dimension(30, 20));
        
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(arrowLabel, BorderLayout.EAST);
        
        // Add hover effect
        headerPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                headerPanel.setBackground(new Color(0xF8FAFC));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                headerPanel.setBackground(AppTheme.SURFACE);
            }
        });
        
        return headerPanel;
    }
    
    /**
     * Creates a section content panel with formatted text.
     * 
     * @param content The content text
     * @return Section content panel
     */
    private JPanel createSectionContent(String content) {
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(true);
        contentPanel.setBackground(AppTheme.SURFACE);
        contentPanel.setBorder(new CompoundBorder(
            new MatteBorder(0, 1, 1, 1, new Color(0xE2E8F0)),
            new EmptyBorder(PADDING_MEDIUM, PADDING_MEDIUM, PADDING_MEDIUM, PADDING_MEDIUM)
        ));
        contentPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        
        // Create text area for content
        JTextArea textArea = new JTextArea(content);
        textArea.setFont(AppTheme.PRIMARY_FONT);
        textArea.setForeground(AppTheme.TEXT_SECONDARY);
        textArea.setBackground(AppTheme.SURFACE);
        textArea.setEditable(false);
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.setBorder(null);
        textArea.setOpaque(false);
        
        contentPanel.add(textArea, BorderLayout.CENTER);
        
        return contentPanel;
    }
    
    /**
     * Gets the help content for account and profile management.
     * 
     * @return Account help content
     */
    private String getAccountHelpContent() {
        return "Account and Profile Management\n\n" +
               "• Profile Information: View and manage your profile details including name, email, and skills.\n\n" +
               "• Skills Management: Add, update, or remove skills from your profile. Set proficiency levels to showcase your expertise.\n\n" +
               "• Password Management: Change your account password using the 'Change Password' button on your profile page.\n\n" +
               "• Account Security: Keep your account secure by using a strong password and signing out when finished.\n\n" +
               "• Profile Visibility: Your profile information and skills are visible to other users in the community.";
    }
    
    /**
     * Gets the help content for using the application.
     * 
     * @return Usage help content
     */
    private String getUsageHelpContent() {
        return "Using the Application\n\n" +
               "• Navigation: Use the top navigation bar to access different sections of the application.\n\n" +
               "• Search Functionality: Use the search bar to find questions, topics, or users quickly.\n\n" +
               "• Question Management: Ask questions, view answers, and participate in discussions.\n\n" +
               "• Voting System: Upvote or downvote questions and answers to help surface the best content.\n\n" +
               "• Tags and Categories: Use tags to categorize your questions and make them easier to find.\n\n" +
               "• Profile Pages: Access user profiles to view their skills, contributions, and activity.";
    }
    
    /**
     * Gets the help content for technical support.
     * 
     * @return Support help content
     */
    private String getSupportHelpContent() {
        return "Technical Support\n\n" +
               "• Common Issues: If you're experiencing problems, try refreshing the page or signing out and back in.\n\n" +
               "• Browser Compatibility: Upnex works best with modern browsers. Please ensure you're using an up-to-date version.\n\n" +
               "• Data Backup: Your profile information and content are automatically saved. No manual backup is required.\n\n" +
               "• Performance: If the application is running slowly, try closing other browser tabs or applications.\n\n" +
               "• Bug Reports: If you encounter a bug or have feature suggestions, please contact the development team.\n\n" +
               "• System Requirements: Upnex is a lightweight application that works on most computers and devices.";
    }
    
    /**
     * Refreshes the help content display.
     */
    public void refresh() {
        // Help content is static, so no refresh needed
        // This method is provided for consistency with other screens
    }
}