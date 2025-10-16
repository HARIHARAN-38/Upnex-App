package com.upnext.app.ui.components;

import com.upnext.app.domain.Skill;
import com.upnext.app.ui.theme.AppTheme;
import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * A UI component to display a skill card with details and actions.
 * Displays skill name, description, and proficiency level with an option to delete.
 */
public class SkillCard extends JPanel {
    private final Skill skill;
    private final JLabel skillNameLabel;
    private final JLabel descriptionLabel;
    private final ProficiencyBar proficiencyBar;
    private final JButton deleteButton;
    
    /**
     * Creates a new skill card for the given skill.
     * 
     * @param skill The skill to display
     */
    public SkillCard(Skill skill) {
        this.skill = skill;
        
        setLayout(new BorderLayout(10, 10));
        setBackground(AppTheme.SURFACE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE2E8F0)),
                new EmptyBorder(15, 15, 15, 15)));
        
        // Header panel with skill name and delete button
        JPanel headerPanel = new JPanel(new BorderLayout(5, 0));
        headerPanel.setOpaque(false);
        
        skillNameLabel = new JLabel(skill.getSkillName());
        skillNameLabel.setFont(AppTheme.HEADING_FONT.deriveFont(16f));
        skillNameLabel.setForeground(AppTheme.TEXT_PRIMARY);
        
        deleteButton = new JButton("×");  // Unicode "×" character for close
        deleteButton.setFont(new Font(deleteButton.getFont().getName(), Font.BOLD, 18));
        deleteButton.setForeground(AppTheme.TEXT_SECONDARY);
        deleteButton.setContentAreaFilled(false);
        deleteButton.setBorderPainted(false);
        deleteButton.setFocusPainted(false);
        deleteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteButton.setToolTipText("Delete skill");
        
        headerPanel.add(skillNameLabel, BorderLayout.WEST);
        headerPanel.add(deleteButton, BorderLayout.EAST);
        
        // Description
        descriptionLabel = new JLabel(skill.getDescription());
        descriptionLabel.setForeground(AppTheme.TEXT_SECONDARY);
        
        // Proficiency bar
        proficiencyBar = new ProficiencyBar(skill.getProficiencyLevel(), false);
        
        // Layout
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setOpaque(false);
        contentPanel.add(descriptionLabel, BorderLayout.NORTH);
        contentPanel.add(proficiencyBar, BorderLayout.CENTER);
        
        add(headerPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
    }
    
    /**
     * Gets the skill associated with this card.
     * 
     * @return The skill
     */
    public Skill getSkill() {
        return skill;
    }
    
    /**
     * Sets the listener for the delete button.
     * 
     * @param listener The action listener
     */
    public void setDeleteActionListener(ActionListener listener) {
        deleteButton.addActionListener(listener);
    }
    
    /**
     * Updates the displayed proficiency level.
     * 
     * @param level The new proficiency level
     */
    public void updateProficiencyLevel(int level) {
        proficiencyBar.setProficiencyLevel(level);
        skill.setProficiencyLevel(level);
    }
    
    /**
     * Makes the proficiency bar editable or read-only.
     * 
     * @param editable Whether the proficiency bar should be editable
     */
    public void setProficiencyEditable(boolean editable) {
        proficiencyBar.setEditable(editable);
    }
    
    /**
     * Updates the skill details displayed on this card.
     * 
     * @param updatedSkill The updated skill
     */
    public void updateSkill(Skill updatedSkill) {
        skillNameLabel.setText(updatedSkill.getSkillName());
        descriptionLabel.setText(updatedSkill.getDescription());
        proficiencyBar.setProficiencyLevel(updatedSkill.getProficiencyLevel());
    }
}