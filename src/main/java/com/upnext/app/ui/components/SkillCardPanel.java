package com.upnext.app.ui.components;

import com.upnext.app.domain.Skill;
import com.upnext.app.ui.theme.AppTheme;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * A panel that manages and displays multiple skill cards.
 * Provides methods to add, remove, and update skills.
 */
public class SkillCardPanel extends JPanel {
    private final JPanel cardsPanel;
    private final List<SkillCard> skillCards;
    private Consumer<Skill> deleteCallback;
    
    /**
     * Creates a new skill card panel.
     */
    public SkillCardPanel() {
        setLayout(new BorderLayout());
        setBackground(AppTheme.BACKGROUND);
        setBorder(new EmptyBorder(10, 10, 10, 10));
        
        skillCards = new ArrayList<>();
        
        // Panel for cards with vertical layout
        cardsPanel = new JPanel();
        cardsPanel.setLayout(new BoxLayout(cardsPanel, BoxLayout.Y_AXIS));
        cardsPanel.setOpaque(false);
        
        // Wrap in scroll pane for many skills
        JScrollPane scrollPane = new JScrollPane(cardsPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        
        add(scrollPane, BorderLayout.CENTER);
    }
    
    /**
     * Adds a skill card for the specified skill.
     * 
     * @param skill The skill to add
     * @return The created skill card
     */
    public SkillCard addSkill(Skill skill) {
        SkillCard card = new SkillCard(skill);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height));
        
        // Add space between cards
        if (!skillCards.isEmpty()) {
            cardsPanel.add(Box.createVerticalStrut(10));
        }
        
        card.setDeleteActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (deleteCallback != null) {
                    deleteCallback.accept(skill);
                }
                removeSkillCard(card);
            }
        });
        
        skillCards.add(card);
        cardsPanel.add(card);
        revalidate();
        repaint();
        return card;
    }
    
    /**
     * Removes a skill card.
     * 
     * @param card The card to remove
     */
    public void removeSkillCard(SkillCard card) {
        int index = skillCards.indexOf(card);
        
        if (index >= 0) {
            skillCards.remove(index);
            
            // Need to remove the card and potentially a strut before or after it
            cardsPanel.remove(card);
            
            // If there are other components and this wasn't the last card,
            // we might need to remove a strut
            if (!skillCards.isEmpty() && index < cardsPanel.getComponentCount()) {
                Component comp = cardsPanel.getComponent(index);
                if (comp instanceof Box.Filler) {
                    cardsPanel.remove(comp);
                }
            }
            
            revalidate();
            repaint();
        }
    }
    
    /**
     * Updates a skill card with new data.
     * 
     * @param skillId The ID of the skill to update
     * @param updatedSkill The updated skill data
     */
    public void updateSkill(Long skillId, Skill updatedSkill) {
        for (SkillCard card : skillCards) {
            if (card.getSkill().getSkillId().equals(skillId)) {
                card.updateSkill(updatedSkill);
                break;
            }
        }
    }
    
    /**
     * Gets all skills currently displayed in this panel.
     * 
     * @return List of skills
     */
    public List<Skill> getSkills() {
        List<Skill> skills = new ArrayList<>();
        for (SkillCard card : skillCards) {
            skills.add(card.getSkill());
        }
        return skills;
    }
    
    /**
     * Sets all skill cards' proficiency bars to be editable or read-only.
     * 
     * @param editable Whether the proficiency bars should be editable
     */
    public void setProficiencyEditable(boolean editable) {
        for (SkillCard card : skillCards) {
            card.setProficiencyEditable(editable);
        }
    }
    
    /**
     * Clears all skill cards from this panel.
     */
    public void clear() {
        skillCards.clear();
        cardsPanel.removeAll();
        revalidate();
        repaint();
    }
    
    /**
     * Sets a callback for when a skill is deleted.
     * 
     * @param callback The callback function that receives the deleted skill
     */
    public void setDeleteCallback(Consumer<Skill> callback) {
        this.deleteCallback = callback;
    }
    
    /**
     * Gets the number of skill cards in this panel.
     * 
     * @return The count of skill cards
     */
    public int getSkillCount() {
        return skillCards.size();
    }
}