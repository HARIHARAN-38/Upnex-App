package com.upnext.app.ui.screens;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import com.upnext.app.ui.theme.AppTheme;

/**
 * About screen providing information about the Upnex application.
 * Displays overview, key features, team information, and version details
 * in an organized and visually appealing layout.
 */
public class AboutScreen extends JPanel {
    
    // Constants
    private static final int PADDING_LARGE = 24;
    private static final int PADDING_MEDIUM = 16;
    private static final int PADDING_SMALL = 8;
    private static final int SECTION_SPACING = 24;
    
    // Application information
    private static final String APP_VERSION = "1.0.0";
    private static final String LAST_UPDATED = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy"));
    
    // UI Components
    private final JScrollPane scrollPane;
    
    /**
     * Creates a new AboutScreen with application information.
     */
    public AboutScreen() {
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
        
        // Create content sections
        JPanel overviewSection = createOverviewSection();
        overviewSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPanel featuresSection = createFeaturesSection();
        featuresSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPanel teamSection = createTeamSection();
        teamSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPanel versionSection = createVersionSection();
        versionSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Add components with spacing
        mainPanel.add(headerPanel);
        mainPanel.add(Box.createVerticalStrut(SECTION_SPACING * 2));
        mainPanel.add(overviewSection);
        mainPanel.add(Box.createVerticalStrut(SECTION_SPACING));
        mainPanel.add(featuresSection);
        mainPanel.add(Box.createVerticalStrut(SECTION_SPACING));
        mainPanel.add(teamSection);
        mainPanel.add(Box.createVerticalStrut(SECTION_SPACING));
        mainPanel.add(versionSection);
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
        JLabel titleLabel = new JLabel("About Upnex");
        titleLabel.setFont(AppTheme.HEADING_FONT.deriveFont(Font.BOLD, 28f));
        titleLabel.setForeground(AppTheme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Subtitle
        JLabel subtitleLabel = new JLabel("Learn more about our platform and purpose");
        subtitleLabel.setFont(AppTheme.PRIMARY_FONT.deriveFont(Font.PLAIN, 16f));
        subtitleLabel.setForeground(AppTheme.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(PADDING_SMALL));
        headerPanel.add(subtitleLabel);
        
        return headerPanel;
    }
    
    /**
     * Creates the overview section describing the application.
     * 
     * @return Overview section panel
     */
    private JPanel createOverviewSection() {
        JPanel sectionPanel = createSectionPanel("Overview");
        
        String overviewText = "Upnex is a modern knowledge-sharing platform designed to connect " +
                             "people with expertise to those seeking answers. Our mission is to create " +
                             "a collaborative environment where users can ask questions, share knowledge, " +
                             "and build meaningful connections through learning.\n\n" +
                             "Built with a focus on user experience and community engagement, Upnex " +
                             "provides tools for skill management, question discussion, and peer-to-peer " +
                             "learning that empowers individuals and organizations to grow together.";
        
        addTextContent(sectionPanel, overviewText);
        
        return sectionPanel;
    }
    
    /**
     * Creates the key features section.
     * 
     * @return Features section panel
     */
    private JPanel createFeaturesSection() {
        JPanel sectionPanel = createSectionPanel("Key Features");
        
        String[] features = {
            "• Skill Management: Create and manage your professional skill profile with proficiency tracking",
            "• Question & Answer System: Ask questions and provide answers with voting and feedback",
            "• User Profiles: Showcase your expertise and connect with other professionals",
            "• Search & Discovery: Find relevant questions, topics, and users quickly and efficiently",
            "• Community Voting: Help surface the best content through community-driven voting",
            "• Tag-based Organization: Organize and categorize content using a flexible tagging system",
            "• Responsive Design: Access Upnex seamlessly across desktop and mobile devices",
            "• Real-time Updates: Stay informed with live updates on questions and discussions"
        };
        
        addListContent(sectionPanel, features);
        
        return sectionPanel;
    }
    
    /**
     * Creates the team/project information section.
     * 
     * @return Team section panel
     */
    private JPanel createTeamSection() {
        JPanel sectionPanel = createSectionPanel("Team & Project Info");
        
        String teamText = "Upnex is developed by a dedicated team of software engineers and designers " +
                         "who are passionate about creating tools that facilitate learning and knowledge sharing.\n\n" +
                         "Development Team:\n" +
                         "• Lead Developer: Full-stack development and architecture\n" +
                         "• UI/UX Designer: User interface design and user experience optimization\n" +
                         "• Backend Engineer: Database design and API development\n" +
                         "• Quality Assurance: Testing and quality validation\n\n" +
                         "We are committed to continuous improvement and value feedback from our user community. " +
                         "Our goal is to create a platform that truly serves the needs of knowledge seekers and sharers alike.";
        
        addTextContent(sectionPanel, teamText);
        
        return sectionPanel;
    }
    
    /**
     * Creates the version information section.
     * 
     * @return Version section panel
     */
    private JPanel createVersionSection() {
        JPanel sectionPanel = createSectionPanel("Version Information");
        
        String versionText = "Current Version: " + APP_VERSION + "\n" +
                           "Last Updated: " + LAST_UPDATED + "\n\n" +
                           "Release Notes:\n" +
                           "• Initial release with core Q&A functionality\n" +
                           "• User profile and skill management system\n" +
                           "• Advanced search and filtering capabilities\n" +
                           "• Community voting and feedback features\n" +
                           "• Responsive design for all device types\n\n" +
                           "Upcoming Features:\n" +
                           "• Direct messaging between users\n" +
                           "• Advanced analytics and insights\n" +
                           "• Mobile application for iOS and Android\n" +
                           "• Integration with external learning platforms";
        
        addTextContent(sectionPanel, versionText);
        
        return sectionPanel;
    }
    
    /**
     * Creates a section panel with title and content area.
     * 
     * @param title The section title
     * @return Section panel
     */
    private JPanel createSectionPanel(String title) {
        JPanel sectionPanel = new JPanel();
        sectionPanel.setLayout(new BoxLayout(sectionPanel, BoxLayout.Y_AXIS));
        sectionPanel.setOpaque(true);
        sectionPanel.setBackground(AppTheme.SURFACE);
        sectionPanel.setBorder(new CompoundBorder(
            new MatteBorder(1, 1, 1, 1, new Color(0xE2E8F0)),
            new EmptyBorder(PADDING_MEDIUM, PADDING_MEDIUM, PADDING_MEDIUM, PADDING_MEDIUM)
        ));
        sectionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        
        // Section title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(AppTheme.HEADING_FONT.deriveFont(Font.BOLD, 20f));
        titleLabel.setForeground(AppTheme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Add separator
        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(0xE2E8F0));
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        
        sectionPanel.add(titleLabel);
        sectionPanel.add(Box.createVerticalStrut(PADDING_SMALL));
        sectionPanel.add(separator);
        sectionPanel.add(Box.createVerticalStrut(PADDING_MEDIUM));
        
        return sectionPanel;
    }
    
    /**
     * Adds formatted text content to a section panel.
     * 
     * @param sectionPanel The section panel to add content to
     * @param text The text content
     */
    private void addTextContent(JPanel sectionPanel, String text) {
        String[] paragraphs = text.split("\n\n");
        
        for (int i = 0; i < paragraphs.length; i++) {
            String paragraph = paragraphs[i];
            
            // Check if this paragraph contains bullet points or special formatting
            if (paragraph.contains("•") || paragraph.contains(":")) {
                String[] lines = paragraph.split("\n");
                for (String line : lines) {
                    JLabel lineLabel = createContentLabel(line);
                    
                    // Special formatting for headers (lines ending with colon)
                    if (line.endsWith(":") && !line.contains("•")) {
                        lineLabel.setFont(AppTheme.PRIMARY_FONT.deriveFont(Font.BOLD, 14f));
                        lineLabel.setForeground(AppTheme.TEXT_PRIMARY);
                    }
                    
                    sectionPanel.add(lineLabel);
                    if (!line.equals(lines[lines.length - 1])) {
                        sectionPanel.add(Box.createVerticalStrut(4));
                    }
                }
            } else {
                // Regular paragraph
                JLabel paragraphLabel = createContentLabel(paragraph);
                sectionPanel.add(paragraphLabel);
            }
            
            // Add spacing between paragraphs
            if (i < paragraphs.length - 1) {
                sectionPanel.add(Box.createVerticalStrut(PADDING_MEDIUM));
            }
        }
    }
    
    /**
     * Adds list content to a section panel.
     * 
     * @param sectionPanel The section panel to add content to
     * @param items The list items
     */
    private void addListContent(JPanel sectionPanel, String[] items) {
        for (int i = 0; i < items.length; i++) {
            JLabel itemLabel = createContentLabel(items[i]);
            sectionPanel.add(itemLabel);
            
            if (i < items.length - 1) {
                sectionPanel.add(Box.createVerticalStrut(PADDING_SMALL));
            }
        }
    }
    
    /**
     * Creates a content label with proper formatting.
     * 
     * @param text The label text
     * @return Formatted content label
     */
    private JLabel createContentLabel(String text) {
        // Handle multi-line text by converting to HTML
        String htmlText = text.replace("\n", "<br>");
        if (!htmlText.startsWith("<html>")) {
            htmlText = "<html><body style='width: 100%'>" + htmlText + "</body></html>";
        }
        
        JLabel label = new JLabel(htmlText);
        label.setFont(AppTheme.PRIMARY_FONT);
        label.setForeground(AppTheme.TEXT_SECONDARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setVerticalAlignment(SwingConstants.TOP);
        
        return label;
    }
    
    /**
     * Refreshes the about screen content.
     */
    public void refresh() {
        // About content is static, so no refresh needed
        // This method is provided for consistency with other screens
    }
}