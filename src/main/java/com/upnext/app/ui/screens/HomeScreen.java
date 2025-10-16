package com.upnext.app.ui.screens;

import com.upnext.app.domain.User;
import com.upnext.app.service.AuthService;
import com.upnext.app.ui.theme.AppTheme;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Home screen for the UpNext application.
 * Displays a welcome message and dashboard placeholder widgets.
 */
public class HomeScreen extends JPanel {
    // UI components
    private final JLabel welcomeLabel;
    private final JButton signOutButton;
    
    /**
     * Creates a new home screen.
     */
    public HomeScreen() {
        setLayout(new BorderLayout());
        setBackground(AppTheme.BACKGROUND);
        setBorder(new EmptyBorder(40, 40, 40, 40));
        
        // Header panel with title and sign out button
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 30, 0));
        
        JLabel titleLabel = new JLabel("UpNext Dashboard");
        titleLabel.setFont(AppTheme.HEADING_FONT.deriveFont(24f));
        
        signOutButton = new JButton("Sign Out");
        signOutButton.setForeground(AppTheme.ACCENT);  // Using ACCENT color for sign out button
        signOutButton.setBorderPainted(false);
        signOutButton.setContentAreaFilled(false);
        signOutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(signOutButton, BorderLayout.EAST);
        
        // Welcome message
        welcomeLabel = new JLabel();
        welcomeLabel.setFont(AppTheme.PRIMARY_FONT.deriveFont(16f));
        welcomeLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        // Create dashboard grid for widgets
        JPanel dashboardPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        dashboardPanel.setOpaque(false);
        
        // Add placeholder widgets
        dashboardPanel.add(createPlaceholderWidget("Tasks", "Your upcoming tasks will appear here"));
        dashboardPanel.add(createPlaceholderWidget("Calendar", "Your schedule will appear here"));
        dashboardPanel.add(createPlaceholderWidget("Analytics", "Your activity metrics will appear here"));
        dashboardPanel.add(createPlaceholderWidget("Notes", "Your recent notes will appear here"));
        
        // Main content
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.add(welcomeLabel, BorderLayout.NORTH);
        contentPanel.add(dashboardPanel, BorderLayout.CENTER);
        
        // Add to main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false);
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Set up sign out action
        signOutButton.addActionListener(_ -> {
            AuthService.getInstance().signOut();
            // Note: Navigation back to sign-in screen will be handled in App.java
        });
    }
    
    /**
     * Updates the welcome message based on the current authenticated user.
     */
    public void updateWelcomeMessage() {
        User currentUser = AuthService.getInstance().getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getName() + "! Here's your dashboard.");
        } else {
            welcomeLabel.setText("Welcome to your dashboard!");
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
     * Creates a placeholder widget panel for the dashboard.
     * 
     * @param title The widget title
     * @param message The placeholder message
     * @return A panel representing the widget
     */
    private JPanel createPlaceholderWidget(String title, String message) {
        JPanel widget = new JPanel(new BorderLayout());
        widget.setBackground(AppTheme.SURFACE);
        widget.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE2E8F0)),
                new EmptyBorder(15, 15, 15, 15)));
        
        // Widget header
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(AppTheme.HEADING_FONT.deriveFont(16f));
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        // Widget content
        JLabel contentLabel = new JLabel(message);
        contentLabel.setForeground(AppTheme.TEXT_SECONDARY);
        contentLabel.setFont(AppTheme.PRIMARY_FONT);
        
        // TODO: Replace these placeholder widgets with actual functionality in future iterations
        // For the Tasks widget:
        //   - Add a TaskList component that displays tasks from a TaskService
        //   - Implement task creation, editing, and completion functionality
        
        // For the Calendar widget:
        //   - Add a CalendarView component that displays upcoming events
        //   - Implement event scheduling and reminder functionality
        
        // For the Analytics widget:
        //   - Add charts and metrics for user activity
        //   - Implement data aggregation and visualization
        
        // For the Notes widget:
        //   - Add a NoteList component that displays recent notes
        //   - Implement note creation, editing, and search functionality
        
        widget.add(titleLabel, BorderLayout.NORTH);
        widget.add(contentLabel, BorderLayout.CENTER);
        
        return widget;
    }
}