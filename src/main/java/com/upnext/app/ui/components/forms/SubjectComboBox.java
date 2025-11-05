package com.upnext.app.ui.components.forms;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.upnext.app.core.Logger;
import com.upnext.app.data.question.SubjectRepository;
import com.upnext.app.domain.question.Subject;
import com.upnext.app.ui.theme.AppTheme;

/**
 * A styled subject selection combo box component.
 * Loads subjects from the database and provides selection callback.
 */
public class SubjectComboBox extends JPanel {
    private final JLabel label;
    private final JComboBox<SubjectItem> comboBox;
    private Consumer<Long> onSelectionChanged;
    
    /**
     * Creates a new SubjectComboBox.
     * 
     * @param labelText The label text to display above the combo box
     */
    public SubjectComboBox(String labelText) {
        setLayout(new java.awt.BorderLayout());
        setOpaque(false);
        
        // Create label
        label = new JLabel(labelText);
        label.setFont(AppTheme.PRIMARY_FONT);
        label.setForeground(AppTheme.TEXT_PRIMARY);
        label.setBorder(new EmptyBorder(0, 0, 4, 0));
        
        // Create combo box
        comboBox = new JComboBox<>();
        comboBox.setFont(AppTheme.PRIMARY_FONT);
        comboBox.setBackground(Color.WHITE);
        comboBox.setForeground(AppTheme.TEXT_PRIMARY);
        comboBox.setBorder(BorderFactory.createLineBorder(new Color(0xD1D5DB), 1));
        comboBox.setPreferredSize(new Dimension(0, 36));
        comboBox.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add selection listener
        comboBox.addActionListener(e -> {
            SubjectItem selected = (SubjectItem) comboBox.getSelectedItem();
            if (selected != null && onSelectionChanged != null) {
                onSelectionChanged.accept(selected.getId());
            }
        });
        
        // Add components
        add(label, java.awt.BorderLayout.NORTH);
        add(comboBox, java.awt.BorderLayout.CENTER);
        
        // Load subjects asynchronously
        loadSubjects();
    }
    
    /**
     * Loads subjects from the database and populates the combo box.
     */
    private void loadSubjects() {
        SwingUtilities.invokeLater(() -> {
            try {
                List<Subject> subjects = SubjectRepository.getInstance().findAll();
                DefaultComboBoxModel<SubjectItem> model = new DefaultComboBoxModel<>();
                
                // Add placeholder item
                model.addElement(new SubjectItem(null, "Select a subject..."));
                
                // Add subjects
                for (Subject subject : subjects) {
                    model.addElement(new SubjectItem(subject.getId(), subject.getName()));
                }
                
                comboBox.setModel(model);
                
            } catch (Exception e) {
                Logger.getInstance().error("Failed to load subjects: " + e.getMessage());
                // Add error state
                DefaultComboBoxModel<SubjectItem> errorModel = new DefaultComboBoxModel<>();
                errorModel.addElement(new SubjectItem(null, "Error loading subjects"));
                comboBox.setModel(errorModel);
            }
        });
    }
    
    /**
     * Sets the selection change callback.
     * 
     * @param callback The callback to call when selection changes
     */
    public void setOnSelectionChanged(Consumer<Long> callback) {
        this.onSelectionChanged = callback;
    }
    
    /**
     * Gets the selected subject ID.
     * 
     * @return The selected subject ID, or null if none selected
     */
    public Long getSelectedSubjectId() {
        SubjectItem selected = (SubjectItem) comboBox.getSelectedItem();
        return selected != null ? selected.getId() : null;
    }
    
    /**
     * Sets the selected subject by ID.
     * 
     * @param subjectId The subject ID to select
     */
    public void setSelectedSubjectId(Long subjectId) {
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            SubjectItem item = comboBox.getItemAt(i);
            if (item != null && item.getId() != null && item.getId().equals(subjectId)) {
                comboBox.setSelectedIndex(i);
                break;
            }
        }
    }
    
    /**
     * Clears the selection (selects the placeholder).
     */
    public void clearSelection() {
        if (comboBox.getItemCount() > 0) {
            comboBox.setSelectedIndex(0); // Select placeholder
        }
    }
    
    /**
     * Gets the combo box component for advanced styling if needed.
     * 
     * @return The combo box component
     */
    public JComboBox<SubjectItem> getComboBox() {
        return comboBox;
    }
    
    /**
     * Inner class representing a subject item in the combo box.
     */
    private static class SubjectItem {
        private final Long id;
        private final String name;
        
        public SubjectItem(Long id, String name) {
            this.id = id;
            this.name = name;
        }
        
        public Long getId() {
            return id;
        }
        
        public String getName() {
            return name;
        }
        
        @Override
        public String toString() {
            return name;
        }
    }
}