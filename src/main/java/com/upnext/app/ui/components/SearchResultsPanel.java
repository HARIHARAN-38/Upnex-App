package com.upnext.app.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;

import com.upnext.app.domain.question.Question;
import com.upnext.app.ui.theme.AppTheme;

/**
 * Panel that displays search results in a dropdown format.
 * This component renders a list of questions from search results.
 */
public class SearchResultsPanel extends JPanel {
    // Constants for styling
    private static final int MAX_VISIBLE_RESULTS = 5;
    private static final int RESULT_HEIGHT = 60;
    private static final int PADDING = 8;
    
    // UI components
    private final JList<Question> resultsList;
    private final JScrollPane scrollPane;
    private final DefaultListModel<Question> listModel;
    
    // Data
    private List<Question> questionsList;
    
    // Callback for selection
    private QuestionSelectedListener listener;
    
    /**
     * Interface for notifying when a search result is selected.
     */
    public interface QuestionSelectedListener {
        void onQuestionSelected(Question question);
    }
    
    /**
     * Creates a new search results panel.
     */
    public SearchResultsPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE2E8F0)),
                BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING)));
        setBackground(Color.WHITE);
        
        // Initialize the list model
        listModel = new DefaultListModel<>();
        questionsList = new ArrayList<>();
        
        // Initialize the JList with custom cell renderer
        resultsList = new JList<>(listModel);
        resultsList.setCellRenderer(new QuestionResultCellRenderer());
        resultsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultsList.setBorder(null);
        resultsList.setBackground(Color.WHITE);
        
        // Add mouse listener for selection
        resultsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = resultsList.locationToIndex(e.getPoint());
                if (index >= 0 && index < listModel.size()) {
                    Question selected = listModel.getElementAt(index);
                    if (listener != null) {
                        listener.onQuestionSelected(selected);
                    }
                }
            }
        });
        
        // Create scroll pane
        scrollPane = new JScrollPane(resultsList);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        // Add to panel
        add(scrollPane, BorderLayout.CENTER);
        
        // No results label for empty state
        JLabel noResultsLabel = new JLabel("No results found");
        noResultsLabel.setFont(AppTheme.PRIMARY_FONT);
        noResultsLabel.setForeground(AppTheme.TEXT_SECONDARY);
        noResultsLabel.setHorizontalAlignment(JLabel.CENTER);
        noResultsLabel.setVisible(false);
        
        add(noResultsLabel, BorderLayout.NORTH);
    }
    
    /**
     * Sets the questions to display in the results panel.
     * 
     * @param questions List of questions to display
     */
    public void setQuestions(List<Question> questions) {
        this.questionsList = questions != null ? questions : new ArrayList<>();
        updateListModel();
    }
    
    /**
     * Updates the list model with current questions.
     */
    private void updateListModel() {
        listModel.clear();
        
        for (Question question : questionsList) {
            listModel.addElement(question);
        }
        
        // Update panel size based on content
        int preferredHeight = Math.min(questionsList.size(), MAX_VISIBLE_RESULTS) * RESULT_HEIGHT 
                + 2 * PADDING + 2; // Add border thickness
        
        if (questionsList.isEmpty()) {
            preferredHeight = RESULT_HEIGHT; // Minimum height for "No results" message
        }
        
        setPreferredSize(new Dimension(getWidth(), preferredHeight));
        revalidate();
        repaint();
    }
    
    /**
     * Gets the selected question from the results list.
     * 
     * @return The selected question, or null if none selected
     */
    public Question getSelectedQuestion() {
        int index = resultsList.getSelectedIndex();
        if (index >= 0 && questionsList != null && index < questionsList.size()) {
            return questionsList.get(index);
        }
        return null;
    }
    
    /**
     * Sets the listener for question selection events.
     * 
     * @param listener The question selected listener
     */
    public void setQuestionSelectedListener(QuestionSelectedListener listener) {
        this.listener = listener;
    }
    
    /**
     * Custom cell renderer for question results.
     */
    private static class QuestionResultCellRenderer extends JPanel implements ListCellRenderer<Question> {
        private final JLabel titleLabel;
        private final JLabel subjectLabel;
        
        public QuestionResultCellRenderer() {
            setLayout(new BorderLayout(PADDING, 0));
            setBorder(new EmptyBorder(PADDING, PADDING, PADDING, PADDING));
            setOpaque(true);
            
            // Title label
            titleLabel = new JLabel();
            titleLabel.setFont(AppTheme.PRIMARY_FONT);
            titleLabel.setForeground(AppTheme.TEXT_PRIMARY);
            
            // Subject label
            subjectLabel = new JLabel();
            subjectLabel.setFont(AppTheme.PRIMARY_FONT.deriveFont(Font.ITALIC, 11f));
            subjectLabel.setForeground(AppTheme.TEXT_SECONDARY);
            
            // Add to panel
            add(titleLabel, BorderLayout.CENTER);
            add(subjectLabel, BorderLayout.SOUTH);
        }
        
        @Override
        public Component getListCellRendererComponent(JList<? extends Question> list, 
                                                      Question question, int index, 
                                                      boolean isSelected, boolean cellHasFocus) {
            // Set question title with truncation if needed
            String title = question.getTitle();
            if (title.length() > 60) {
                title = title.substring(0, 57) + "...";
            }
            titleLabel.setText(title);
            
            // Set subject name if available
            String subject = question.getSubjectName();
            if (subject != null && !subject.isEmpty()) {
                subjectLabel.setText("in " + subject);
                subjectLabel.setVisible(true);
            } else {
                subjectLabel.setVisible(false);
            }
            
            // Handle selection state
            if (isSelected) {
                setBackground(new Color(0xF0F4F8));
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            } else {
                setBackground(list.getBackground());
                setCursor(Cursor.getDefaultCursor());
            }
            
            return this;
        }
    }
}