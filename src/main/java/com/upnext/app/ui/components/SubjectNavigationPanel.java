package com.upnext.app.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.upnext.app.core.Logger;
import com.upnext.app.data.question.SubjectRepository;
import com.upnext.app.data.question.TagRepository;
import com.upnext.app.domain.question.Subject;
import com.upnext.app.domain.question.Tag;
import com.upnext.app.ui.theme.AppTheme;

/**
 * Panel component for subject navigation and tag selection.
 * Allows users to select a single subject and multiple tags for filtering content.
 */
public class SubjectNavigationPanel extends JPanel {
    private static final Logger LOGGER = Logger.getInstance();
    
    // UI constants
    private static final int PADDING = 10;
    private static final int TAG_SPACING = 5;
    private static final int MAX_TRENDING_TAGS = 15;
    
    // Repositories
    private final SubjectRepository subjectRepository;
    private final TagRepository tagRepository;
    
    // UI components
    private final JPanel subjectsPanel;
    private final JPanel tagsPanel;
    private final ButtonGroup subjectButtonGroup;
    
    // State tracking
    private Subject selectedSubject;
    private final Set<Tag> selectedTags = new HashSet<>();
    
    // Listener for filter changes
    private FilterChangeListener filterChangeListener;
    
    /**
     * Creates a new SubjectNavigationPanel.
     */
    public SubjectNavigationPanel() {
        setLayout(new BorderLayout(0, PADDING));
        setOpaque(false);
        setBorder(new EmptyBorder(PADDING, PADDING, PADDING, PADDING));
        
        // Get repository instances
        subjectRepository = SubjectRepository.getInstance();
        tagRepository = TagRepository.getInstance();
        
        // Set up the subjects panel
        JPanel subjectsHeader = new JPanel(new BorderLayout());
        subjectsHeader.setOpaque(false);
        
        JLabel subjectsTitle = new JLabel("Subjects");
        subjectsTitle.setFont(AppTheme.HEADING_FONT.deriveFont(16f));
        subjectsHeader.add(subjectsTitle, BorderLayout.NORTH);
        
        subjectsPanel = new JPanel();
        subjectsPanel.setLayout(new BoxLayout(subjectsPanel, BoxLayout.Y_AXIS));
        subjectsPanel.setOpaque(false);
        subjectsPanel.setBorder(new EmptyBorder(PADDING, 0, PADDING, 0));
        
        // Radio button group for single-select subjects
        subjectButtonGroup = new ButtonGroup();
        
        // Set up the tags panel
        JPanel tagsHeader = new JPanel(new BorderLayout());
        tagsHeader.setOpaque(false);
        tagsHeader.setBorder(new EmptyBorder(PADDING, 0, 0, 0));
        
        JLabel tagsTitle = new JLabel("Trending Tags");
        tagsTitle.setFont(AppTheme.HEADING_FONT.deriveFont(16f));
        tagsHeader.add(tagsTitle, BorderLayout.NORTH);
        
        tagsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, TAG_SPACING, TAG_SPACING));
        tagsPanel.setOpaque(false);
        tagsPanel.setBorder(new EmptyBorder(PADDING, 0, 0, 0));
        
        // Wrap in scroll panes
        JScrollPane subjectsScrollPane = new JScrollPane(subjectsPanel);
        subjectsScrollPane.setBorder(null);
        subjectsScrollPane.setOpaque(false);
        subjectsScrollPane.getViewport().setOpaque(false);
        
        JScrollPane tagsScrollPane = new JScrollPane(tagsPanel);
        tagsScrollPane.setBorder(null);
        tagsScrollPane.setOpaque(false);
        tagsScrollPane.getViewport().setOpaque(false);
        
        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.add(subjectsHeader, BorderLayout.NORTH);
        contentPanel.add(subjectsScrollPane, BorderLayout.CENTER);
        
        // Tags section panel
        JPanel tagsSection = new JPanel(new BorderLayout());
        tagsSection.setOpaque(false);
        tagsSection.add(tagsHeader, BorderLayout.NORTH);
        tagsSection.add(tagsScrollPane, BorderLayout.CENTER);
        
        // Add to main layout
        add(contentPanel, BorderLayout.CENTER);
        add(tagsSection, BorderLayout.SOUTH);
        
        // Load data
        loadSubjects();
        loadTrendingTags();
    }
    
    /**
     * Sets a listener for filter change events.
     * 
     * @param listener The listener to set
     */
    public void setFilterChangeListener(FilterChangeListener listener) {
        this.filterChangeListener = listener;
    }
    
    /**
     * Gets the currently selected subject.
     * 
     * @return The selected subject, or null if none is selected
     */
    public Subject getSelectedSubject() {
        return selectedSubject;
    }
    
    /**
     * Gets the currently selected tags.
     * 
     * @return A set of selected tags
     */
    public Set<Tag> getSelectedTags() {
        return new HashSet<>(selectedTags);
    }
    
    /**
     * Selects a subject programmatically.
     * 
     * @param subject The subject to select
     */
    public void selectSubject(Subject subject) {
        if (subject == null) {
            subjectButtonGroup.clearSelection();
            selectedSubject = null;
            notifyFilterChanged();
            return;
        }
        
        // Find the radio button for this subject and select it
        boolean found = false;
        for (Enumeration<AbstractButton> buttons = subjectButtonGroup.getElements(); buttons.hasMoreElements();) {
            JRadioButton button = (JRadioButton) buttons.nextElement();
            if (button.getActionCommand() != null && button.getActionCommand().equals(subject.getId().toString())) {
                button.setSelected(true);
                selectedSubject = subject;
                notifyFilterChanged();
                found = true;
                break;
            }
        }

        // If the corresponding radio button doesn't exist (e.g., in tests
        // where the UI wasn't populated), still update the internal state
        // and notify listeners so programmatic selection works as expected.
        if (!found) {
            selectedSubject = subject;
            notifyFilterChanged();
        }
    }
    
    /**
     * Clears all selected tags.
     */
    public void clearSelectedTags() {
        selectedTags.clear();
        
        // Update UI to reflect changes
        for (Component component : tagsPanel.getComponents()) {
            if (component instanceof JToggleButton button) {
                button.setSelected(false);
            }
        }
        
        notifyFilterChanged();
    }
    
    /**
     * Refreshes the data in the panel.
     */
    public void refreshData() {
        loadSubjects();
        loadTrendingTags();
    }
    
    /**
     * Loads subjects from the repository and populates the UI.
     */
    private void loadSubjects() {
        subjectsPanel.removeAll();
        subjectButtonGroup.clearSelection();
        
        try {
            List<Subject> subjects = subjectRepository.findAll();
            
            // Add "All Subjects" option
            JRadioButton allSubjectsButton = new JRadioButton("All Subjects");
            allSubjectsButton.setOpaque(false);
            allSubjectsButton.setActionCommand("all");
            allSubjectsButton.setFont(AppTheme.PRIMARY_FONT);
            allSubjectsButton.addActionListener(event -> {
                selectedSubject = null;
                notifyFilterChanged();
            });
            
            subjectButtonGroup.add(allSubjectsButton);
            subjectsPanel.add(allSubjectsButton);
            
            // Select "All Subjects" by default
            allSubjectsButton.setSelected(true);
            
            // Add each subject as a radio button
            for (Subject subject : subjects) {
                JRadioButton subjectButton = new JRadioButton(subject.getName());
                subjectButton.setOpaque(false);
                subjectButton.setActionCommand(subject.getId().toString());
                subjectButton.setFont(AppTheme.PRIMARY_FONT);
                
                subjectButton.addActionListener(event -> {
                    selectedSubject = subject;
                    notifyFilterChanged();
                });
                
                subjectButtonGroup.add(subjectButton);
                subjectsPanel.add(subjectButton);
            }
            
            subjectsPanel.revalidate();
            subjectsPanel.repaint();
        } catch (SQLException e) {
            LOGGER.logException("Error loading subjects", e);
            showErrorMessage("Failed to load subjects. Please try again later.");
        }
    }
    
    /**
     * Loads trending tags from the repository and populates the UI.
     */
    private void loadTrendingTags() {
        tagsPanel.removeAll();
        
        try {
            List<Tag> trendingTags = tagRepository.findTrendingTags(MAX_TRENDING_TAGS);
            
            // Add each trending tag as a toggle button
            for (Tag tag : trendingTags) {
                JToggleButton tagButton = new JToggleButton(tag.getName());
                tagButton.setOpaque(false);
                tagButton.setFont(AppTheme.PRIMARY_FONT.deriveFont(12f));
                tagButton.setBorderPainted(true);
                tagButton.setContentAreaFilled(false);
                tagButton.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(AppTheme.ACCENT),
                        new EmptyBorder(2, 5, 2, 5)));
                tagButton.setForeground(AppTheme.TEXT_PRIMARY);
                tagButton.setFocusPainted(false);
                
                // Selected state styling
                tagButton.addChangeListener(changeEvent -> {
                    if (tagButton.isSelected()) {
                        tagButton.setBackground(AppTheme.ACCENT);
                        tagButton.setForeground(Color.WHITE);
                        tagButton.setContentAreaFilled(true);
                    } else {
                        tagButton.setContentAreaFilled(false);
                        tagButton.setForeground(AppTheme.TEXT_PRIMARY);
                    }
                });
                
                // Action listener to update selected tags
                tagButton.addActionListener(event -> {
                    if (tagButton.isSelected()) {
                        selectedTags.add(tag);
                    } else {
                        selectedTags.remove(tag);
                    }
                    notifyFilterChanged();
                });
                
                tagsPanel.add(tagButton);
            }
            
            tagsPanel.revalidate();
            tagsPanel.repaint();
        } catch (SQLException e) {
            LOGGER.logException("Error loading trending tags", e);
            showErrorMessage("Failed to load trending tags. Please try again later.");
        }
    }
    
    /**
     * Notifies the filter change listener when filter selections change.
     */
    private void notifyFilterChanged() {
        if (filterChangeListener != null) {
            filterChangeListener.onFilterChanged(
                    selectedSubject,
                    new ArrayList<>(selectedTags)
            );
        }
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
    
    /**
     * Interface for listening to filter change events.
     */
    public interface FilterChangeListener {
        /**
         * Called when the subject or tag selection changes.
         * 
         * @param subject The currently selected subject, or null if "All Subjects" is selected
         * @param tags The currently selected tags
         */
        void onFilterChanged(Subject subject, List<Tag> tags);
    }
}