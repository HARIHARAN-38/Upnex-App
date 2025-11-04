package com.upnext.app.ui.screens;

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
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import com.upnext.app.core.Logger;
import com.upnext.app.domain.Skill;
import com.upnext.app.domain.User;
import com.upnext.app.service.AuthService;
import com.upnext.app.ui.components.ChangePasswordDialog;
import com.upnext.app.ui.components.ProficiencyBar;
import com.upnext.app.ui.theme.AppTheme;

/**
 * Profile screen displaying user information, skills, and account management options.
 * This screen shows the user's profile header with avatar, name, username, and email,
 * followed by their skill set with proficiency bars and a change password option.
 */
public class ProfileScreen extends JPanel {
    
    // Constants
    private static final int PADDING_LARGE = 24;
    private static final int PADDING_MEDIUM = 16;
    private static final int PADDING_SMALL = 8;
    private static final int AVATAR_SIZE = 80;
    private static final int SECTION_SPACING = 32;
    
    // UI Components
    private JLabel avatarLabel;
    private JLabel nameLabel;
    private JLabel usernameLabel;
    private JLabel emailLabel;
    private JPanel skillsPanel;
    private JButton changePasswordButton;
    private final JScrollPane scrollPane;
    
    /**
     * Creates a new ProfileScreen with user information and skills display.
     */
    public ProfileScreen() {
        setLayout(new BorderLayout());
        setBackground(AppTheme.BACKGROUND);
        
        // Create main content panel with proper spacing
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(AppTheme.BACKGROUND);
        mainPanel.setBorder(new EmptyBorder(PADDING_LARGE, PADDING_LARGE, PADDING_LARGE, PADDING_LARGE));
        
        // Create profile header section
        JPanel headerSection = createProfileHeader();
        headerSection.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Create skills section
        JPanel skillsSection = createSkillsSection();
        skillsSection.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Create change password section
        JPanel passwordSection = createPasswordSection();
        passwordSection.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Add sections with spacing
        mainPanel.add(headerSection);
        mainPanel.add(Box.createVerticalStrut(SECTION_SPACING));
        mainPanel.add(skillsSection);
        mainPanel.add(Box.createVerticalStrut(PADDING_LARGE));
        mainPanel.add(passwordSection);
        mainPanel.add(Box.createVerticalGlue());
        
        // Wrap in scroll pane for responsiveness
        JScrollPane scroll = new JScrollPane(mainPanel);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane = scroll;
        
        add(scrollPane, BorderLayout.CENTER);
        
        // Initialize with current user data (done after construction to avoid overridable method call)
        javax.swing.SwingUtilities.invokeLater(() -> loadUserData());
    }
    
    /**
     * Creates the profile header section with avatar and user information.
     * 
     * @return Profile header panel
     */
    private JPanel createProfileHeader() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.setMaximumSize(new Dimension(500, Integer.MAX_VALUE));
        
        // Create avatar
        avatarLabel = createAvatarLabel();
        avatarLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Create user information panel
        JPanel userInfoPanel = new JPanel();
        userInfoPanel.setLayout(new BoxLayout(userInfoPanel, BoxLayout.Y_AXIS));
        userInfoPanel.setOpaque(false);
        userInfoPanel.setBorder(new EmptyBorder(PADDING_MEDIUM, 0, 0, 0));
        
        // Name label
        nameLabel = new JLabel("Loading...");
        nameLabel.setFont(AppTheme.HEADING_FONT.deriveFont(Font.BOLD, 24f));
        nameLabel.setForeground(AppTheme.TEXT_PRIMARY);
        nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Username label
        usernameLabel = new JLabel("@username");
        usernameLabel.setFont(AppTheme.PRIMARY_FONT.deriveFont(Font.PLAIN, 16f));
        usernameLabel.setForeground(AppTheme.TEXT_SECONDARY);
        usernameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        usernameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Email label
        emailLabel = new JLabel("email@example.com");
        emailLabel.setFont(AppTheme.PRIMARY_FONT);
        emailLabel.setForeground(AppTheme.TEXT_SECONDARY);
        emailLabel.setHorizontalAlignment(SwingConstants.CENTER);
        emailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Add components with spacing
        userInfoPanel.add(nameLabel);
        userInfoPanel.add(Box.createVerticalStrut(PADDING_SMALL));
        userInfoPanel.add(usernameLabel);
        userInfoPanel.add(Box.createVerticalStrut(PADDING_SMALL));
        userInfoPanel.add(emailLabel);
        
        headerPanel.add(avatarLabel);
        headerPanel.add(userInfoPanel);
        
        return headerPanel;
    }
    
    /**
     * Creates a circular avatar label for the user profile picture.
     * 
     * @return Avatar label component
     */
    private JLabel createAvatarLabel() {
        JLabel label = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Get the current text (user initials)
                String text = getText();
                
                // Draw circular background
                g2.setColor(AppTheme.PRIMARY);
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                
                // Draw border
                g2.setColor(AppTheme.PRIMARY_DARK);
                g2.drawOval(0, 0, getWidth() - 1, getHeight() - 1);
                
                // Draw text if it exists
                if (text != null && !text.isEmpty()) {
                    g2.setColor(Color.WHITE);
                    g2.setFont(AppTheme.HEADING_FONT.deriveFont(Font.BOLD, 32f));
                    
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
            
            @Override
            public Dimension getMaximumSize() {
                return getPreferredSize();
            }
            
            @Override
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }
        };
        
        return label;
    }
    
    /**
     * Creates the skills section displaying user skills with proficiency bars.
     * 
     * @return Skills section panel
     */
    private JPanel createSkillsSection() {
        JPanel sectionPanel = new JPanel();
        sectionPanel.setLayout(new BoxLayout(sectionPanel, BoxLayout.Y_AXIS));
        sectionPanel.setOpaque(false);
        sectionPanel.setMaximumSize(new Dimension(600, Integer.MAX_VALUE));
        
        // Section title
        JLabel titleLabel = new JLabel("Skill Set");
        titleLabel.setFont(AppTheme.HEADING_FONT.deriveFont(Font.BOLD, 20f));
        titleLabel.setForeground(AppTheme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Skills container panel
        skillsPanel = new JPanel();
        skillsPanel.setLayout(new BoxLayout(skillsPanel, BoxLayout.Y_AXIS));
        skillsPanel.setOpaque(false);
        skillsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        sectionPanel.add(titleLabel);
        sectionPanel.add(Box.createVerticalStrut(PADDING_MEDIUM));
        sectionPanel.add(skillsPanel);
        
        return sectionPanel;
    }
    
    /**
     * Creates the change password section.
     * 
     * @return Change password section panel
     */
    private JPanel createPasswordSection() {
        JPanel sectionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        sectionPanel.setOpaque(false);
        sectionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        
        changePasswordButton = new JButton("Change Password");
        changePasswordButton.setFont(AppTheme.PRIMARY_FONT);
        changePasswordButton.setBackground(AppTheme.PRIMARY);
        changePasswordButton.setForeground(Color.WHITE);
        changePasswordButton.setOpaque(true);
        changePasswordButton.setBorderPainted(false);
        changePasswordButton.setFocusPainted(false);
        changePasswordButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        changePasswordButton.setPreferredSize(new Dimension(160, 40));
        
        // Add rounded border
        changePasswordButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppTheme.PRIMARY, 1, true),
            new EmptyBorder(PADDING_SMALL, PADDING_MEDIUM, PADDING_SMALL, PADDING_MEDIUM)
        ));
        
        // Add click handler
        changePasswordButton.addActionListener(e -> showChangePasswordDialog());
        
        // Add hover effects
        changePasswordButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                changePasswordButton.setBackground(AppTheme.PRIMARY_DARK);
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                changePasswordButton.setBackground(AppTheme.PRIMARY);
            }
        });
        
        sectionPanel.add(changePasswordButton);
        return sectionPanel;
    }
    
    /**
     * Creates a skill card component for displaying individual skills.
     * 
     * @param skill The skill to display
     * @return Skill card panel
     */
    private JPanel createSkillCard(Skill skill) {
        JPanel cardPanel = new JPanel(new BorderLayout(PADDING_MEDIUM, PADDING_SMALL));
        cardPanel.setBackground(AppTheme.SURFACE);
        cardPanel.setBorder(new CompoundBorder(
            new MatteBorder(1, 1, 1, 1, new Color(0xE2E8F0)),
            new EmptyBorder(PADDING_MEDIUM, PADDING_MEDIUM, PADDING_MEDIUM, PADDING_MEDIUM)
        ));
        cardPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        
        // Skill info panel (left side)
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        
        JLabel skillNameLabel = new JLabel(skill.getSkillName());
        skillNameLabel.setFont(AppTheme.PRIMARY_FONT.deriveFont(Font.BOLD, 16f));
        skillNameLabel.setForeground(AppTheme.TEXT_PRIMARY);
        skillNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel skillDescLabel = new JLabel(skill.getDescription());
        skillDescLabel.setFont(AppTheme.PRIMARY_FONT);
        skillDescLabel.setForeground(AppTheme.TEXT_SECONDARY);
        skillDescLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        infoPanel.add(skillNameLabel);
        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(skillDescLabel);
        
        // Proficiency panel (right side)
        JPanel proficiencyPanel = new JPanel();
        proficiencyPanel.setLayout(new BoxLayout(proficiencyPanel, BoxLayout.Y_AXIS));
        proficiencyPanel.setOpaque(false);
        proficiencyPanel.setPreferredSize(new Dimension(200, 60));
        
        JLabel proficiencyLabel = new JLabel("Proficiency");
        proficiencyLabel.setFont(AppTheme.PRIMARY_FONT);
        proficiencyLabel.setForeground(AppTheme.TEXT_SECONDARY);
        proficiencyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Create proficiency bar with percentage
        JPanel barPanel = new JPanel(new BorderLayout(PADDING_SMALL, 0));
        barPanel.setOpaque(false);
        
        ProficiencyBar proficiencyBar = new ProficiencyBar(skill.getProficiencyLevel(), false);
        proficiencyBar.setPreferredSize(new Dimension(150, 20));
        
        JLabel percentageLabel = new JLabel(skill.getProficiencyLevel() + "%");
        percentageLabel.setFont(AppTheme.PRIMARY_FONT.deriveFont(Font.BOLD));
        percentageLabel.setForeground(AppTheme.PRIMARY);
        
        barPanel.add(proficiencyBar, BorderLayout.CENTER);
        barPanel.add(percentageLabel, BorderLayout.EAST);
        
        proficiencyPanel.add(proficiencyLabel);
        proficiencyPanel.add(Box.createVerticalStrut(4));
        proficiencyPanel.add(barPanel);
        
        cardPanel.add(infoPanel, BorderLayout.CENTER);
        cardPanel.add(proficiencyPanel, BorderLayout.EAST);
        
        return cardPanel;
    }
    
    /**
     * Shows the change password dialog.
     */
    private void showChangePasswordDialog() {
        try {
            ChangePasswordDialog dialog = new ChangePasswordDialog(
                (java.awt.Frame) javax.swing.SwingUtilities.getWindowAncestor(this)
            );
            dialog.setVisible(true);
        } catch (Exception e) {
            Logger.getInstance().error("Error showing change password dialog: " + e.getMessage());
        }
    }
    
    /**
     * Loads and displays the current user's data.
     */
    public void loadUserData() {
        try {
            User currentUser = AuthService.getInstance().getCurrentUser();
            if (currentUser != null) {
                // Update profile header
                nameLabel.setText(currentUser.getName());
                usernameLabel.setText("@" + currentUser.getEmail().split("@")[0]); // Use email prefix as username
                emailLabel.setText(currentUser.getEmail());
                avatarLabel.setText(getInitials(currentUser.getName()));
                
                // Update skills section
                updateSkillsDisplay(currentUser);
            } else {
                showNotSignedInState();
            }
        } catch (Exception e) {
            Logger.getInstance().error("Error loading user data in ProfileScreen: " + e.getMessage());
            showErrorState();
        }
    }
    
    /**
     * Updates the skills display section with user's skills.
     * 
     * @param user The user whose skills to display
     */
    private void updateSkillsDisplay(User user) {
        skillsPanel.removeAll();
        
        if (user.getSkills() != null && !user.getSkills().isEmpty()) {
            for (Skill skill : user.getSkills()) {
                JPanel skillCard = createSkillCard(skill);
                skillsPanel.add(skillCard);
                skillsPanel.add(Box.createVerticalStrut(PADDING_MEDIUM));
            }
        } else {
            // Show no skills message
            JPanel noSkillsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            noSkillsPanel.setOpaque(false);
            noSkillsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
            
            JLabel noSkillsLabel = new JLabel("No skills added yet");
            noSkillsLabel.setFont(AppTheme.PRIMARY_FONT);
            noSkillsLabel.setForeground(AppTheme.TEXT_SECONDARY);
            
            noSkillsPanel.add(noSkillsLabel);
            skillsPanel.add(noSkillsPanel);
        }
        
        skillsPanel.revalidate();
        skillsPanel.repaint();
    }
    
    /**
     * Shows the not signed in state.
     */
    private void showNotSignedInState() {
        nameLabel.setText("Not Signed In");
        usernameLabel.setText("@guest");
        emailLabel.setText("Please sign in to view profile");
        avatarLabel.setText("?");
        
        skillsPanel.removeAll();
        JLabel signInLabel = new JLabel("Sign in to view your skills");
        signInLabel.setFont(AppTheme.PRIMARY_FONT);
        signInLabel.setForeground(AppTheme.TEXT_SECONDARY);
        skillsPanel.add(signInLabel);
        
        changePasswordButton.setEnabled(false);
        
        revalidate();
        repaint();
    }
    
    /**
     * Shows an error state when data loading fails.
     */
    private void showErrorState() {
        nameLabel.setText("Error Loading Profile");
        usernameLabel.setText("@error");
        emailLabel.setText("Please try refreshing the page");
        avatarLabel.setText("!");
        
        skillsPanel.removeAll();
        JLabel errorLabel = new JLabel("Unable to load profile data");
        errorLabel.setFont(AppTheme.PRIMARY_FONT);
        errorLabel.setForeground(AppTheme.ACCENT);
        skillsPanel.add(errorLabel);
        
        revalidate();
        repaint();
    }
    
    /**
     * Gets the initials from a user's name.
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
     * Refreshes the profile data display.
     */
    public void refresh() {
        loadUserData();
    }
}