package com.upnext.app.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.upnext.app.domain.User;
import com.upnext.app.service.AuthService;
import com.upnext.app.ui.navigation.ViewNavigator;
import com.upnext.app.ui.theme.AppTheme;

/**
 * Component that displays a hero bar with app logo, search field, and user profile controls.
 * The SimpleHeroBar is a simplified version of HeroBar for screens that don't need full search functionality.
 */
public class SimpleHeroBar extends JPanel {
    // UI Components
    private final JLabel logoLabel;
    private final JTextField searchField;
    private final JLabel avatarLabel;
    private final JButton signOutButton;
    private final JPanel profilePanel;
    private final JPopupMenu profileMenu;
    
    // Constants
    private static final int PADDING_MEDIUM = 16;
    private static final int PADDING_SMALL = 8;
    
    /**
     * Creates a new SimpleHeroBar component with app logo, search field, and user profile.
     */
    public SimpleHeroBar() {
        setLayout(new BorderLayout(PADDING_MEDIUM, 0));
        setOpaque(false);
        setBorder(new EmptyBorder(0, 0, PADDING_MEDIUM, 0));
        
        // Logo/Title on the left
        logoLabel = new JLabel("UpNext");
        logoLabel.setFont(AppTheme.HEADING_FONT.deriveFont(24f));
        logoLabel.setForeground(AppTheme.PRIMARY);
        logoLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Navigate to home screen when logo is clicked
                ViewNavigator.getInstance().navigateTo("HomeScreen");
            }
        });
        
        // Search field in the center
        searchField = new JTextField("Search questions...");
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE2E8F0)),
                BorderFactory.createEmptyBorder(PADDING_SMALL, PADDING_SMALL, PADDING_SMALL, PADDING_SMALL)));
        searchField.setForeground(AppTheme.TEXT_SECONDARY);
        
        // User profile and sign out on the right
        profilePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, PADDING_SMALL, 0));
        profilePanel.setOpaque(false);
        
        // Avatar label for user (will be configured later)
        avatarLabel = new JLabel();
        avatarLabel.setPreferredSize(new Dimension(32, 32));
        avatarLabel.setOpaque(false);
        avatarLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Sign out button
        signOutButton = new JButton("Sign Out");
        signOutButton.setForeground(AppTheme.ACCENT);
        signOutButton.setBorderPainted(false);
        signOutButton.setContentAreaFilled(false);
        signOutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Set up sign out action
        signOutButton.addActionListener(event -> {
            AuthService.getInstance().signOut();
            // Note: Navigation will be handled by the App class
        });
        
        // Create profile menu
        profileMenu = createProfileMenu();
        
        // Set up avatar click for profile menu
        avatarLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                profileMenu.show(avatarLabel, 0, avatarLabel.getHeight());
            }
        });
        
        // Add components to profile panel
        profilePanel.add(avatarLabel);
        profilePanel.add(signOutButton);
        
        // Add components to the hero bar
        add(logoLabel, BorderLayout.WEST);
        add(searchField, BorderLayout.CENTER);
        add(profilePanel, BorderLayout.EAST);
        
        // Initialize user profile after construction to avoid overridable method call
        SwingUtilities.invokeLater(() -> {
            // Use a private method instead of the overridable updateProfileDisplay
            initializeProfileDisplay();
        });
    }
    
    /**
     * Creates the profile dropdown menu.
     * 
     * @return The configured popup menu
     */
    private JPopupMenu createProfileMenu() {
        JPopupMenu menu = new JPopupMenu();
        menu.setBorder(BorderFactory.createLineBorder(new Color(0xE2E8F0)));
        
        // View Profile menu item
        JMenuItem profileMenuItem = new JMenuItem("View Profile");
        profileMenuItem.setFont(AppTheme.PRIMARY_FONT);
        profileMenuItem.addActionListener(e -> {
            // Will navigate to profile screen when implemented
            JOptionPane.showMessageDialog(
                this,
                "Profile page coming soon!",
                "Feature Preview",
                JOptionPane.INFORMATION_MESSAGE
            );
        });
        
        // Account Settings menu item
        JMenuItem settingsMenuItem = new JMenuItem("Account Settings");
        settingsMenuItem.setFont(AppTheme.PRIMARY_FONT);
        settingsMenuItem.addActionListener(e -> {
            // Will navigate to settings screen when implemented
            JOptionPane.showMessageDialog(
                this,
                "Settings page coming soon!",
                "Feature Preview",
                JOptionPane.INFORMATION_MESSAGE
            );
        });
        
        // Sign Out menu item
        JMenuItem signOutMenuItem = new JMenuItem("Sign Out");
        signOutMenuItem.setFont(AppTheme.PRIMARY_FONT);
        signOutMenuItem.setForeground(AppTheme.ACCENT);
        signOutMenuItem.addActionListener(e -> {
            AuthService.getInstance().signOut();
            // Note: Navigation will be handled by the App class
        });
        
        // Add all items to menu
        menu.add(profileMenuItem);
        menu.add(settingsMenuItem);
        menu.addSeparator();
        menu.add(signOutMenuItem);
        
        return menu;
    }
    
    /**
     * Private method to initialize profile display, called from constructor.
     * This avoids calling overridable methods from the constructor.
     */
    private void initializeProfileDisplay() {
        User currentUser = AuthService.getInstance().getCurrentUser();
        if (currentUser != null) {
            avatarLabel.setText(getInitials(currentUser.getName()));
            avatarLabel.setVisible(true);
        } else {
            avatarLabel.setText("?");
            avatarLabel.setVisible(false);
        }
    }

    /**
     * Updates the avatar label with user initials.
     */
    public void updateProfileDisplay() {
        User currentUser = AuthService.getInstance().getCurrentUser();
        if (currentUser != null) {
            avatarLabel.setText(getInitials(currentUser.getName()));
            avatarLabel.setVisible(true);
        } else {
            avatarLabel.setText("?");
            avatarLabel.setVisible(false);
        }
    }
    
    /**
     * Gets initials from a user's name (first letter of first and last name).
     * 
     * @param name The full name
     * @return Initials string
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
     * Sets the search callback to be notified when user performs search.
     * 
     * @param callback A function that takes the search query string
     */
    public void setSearchCallback(java.util.function.Consumer<String> callback) {
        // Can be implemented to support search functionality
    }
    
    /**
     * Gets the sign out button for external access.
     * 
     * @return The sign out button
     */
    public JButton getSignOutButton() {
        return signOutButton;
    }
}