package com.upnext.app.ui.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import com.upnext.app.domain.question.QuestionSearchCriteria;
import com.upnext.app.domain.question.Subject;
import com.upnext.app.domain.question.Tag;

/**
 * Manager class that handles unified filter state across components.
 * Combines search text, subject selection, tag selections, and toolbar filters
 * into a single coherent filter state that can be applied to question feed.
 * Also handles persisting filter state between sessions.
 */
public class FilterManager {
    // Singleton instance
    private static FilterManager instance;
    
    // Filter state
    private String searchText;
    private Subject selectedSubject;
    private List<Tag> selectedTags = new ArrayList<>();
    private QuestionSearchCriteria.SortOption sortOption = QuestionSearchCriteria.SortOption.NEWEST;
    private boolean onlyUnanswered = false;
    private boolean onlySolved = false;
    
    // Listeners
    private final List<FilterChangeListener> listeners = new ArrayList<>();
    
    // Preferences for persistence
    private final Preferences preferences;
    
    // Preference keys
    private static final String PREF_SEARCH_TEXT = "filter.searchText";
    private static final String PREF_SUBJECT_ID = "filter.subjectId";
    private static final String PREF_SORT_OPTION = "filter.sortOption";
    private static final String PREF_ONLY_UNANSWERED = "filter.onlyUnanswered";
    private static final String PREF_ONLY_SOLVED = "filter.onlySolved";
    
    // Tag persistence keys (limited to 10 tags)
    private static final String PREF_TAG_COUNT = "filter.tagCount";
    private static final String PREF_TAG_PREFIX = "filter.tag.";
    private static final int MAX_PERSISTED_TAGS = 10;
    
    /**
     * Private constructor to enforce singleton pattern.
     */
    private FilterManager() {
        // Get user preferences node
        preferences = Preferences.userNodeForPackage(FilterManager.class);
        
        // Load persisted values
        loadPersistedState();
    }
    
    /**
     * Gets the singleton instance of FilterManager.
     * 
     * @return The FilterManager instance
     */
    public static synchronized FilterManager getInstance() {
        if (instance == null) {
            instance = new FilterManager();
        }
        return instance;
    }
    
    /**
     * Loads previously persisted filter state from preferences.
     */
    private void loadPersistedState() {
        searchText = preferences.get(PREF_SEARCH_TEXT, null);
        
        // Load sort option
        String sortOptionStr = preferences.get(PREF_SORT_OPTION, QuestionSearchCriteria.SortOption.NEWEST.name());
        try {
            sortOption = QuestionSearchCriteria.SortOption.valueOf(sortOptionStr);
        } catch (IllegalArgumentException e) {
            sortOption = QuestionSearchCriteria.SortOption.NEWEST;
        }
        
        // Load boolean filters
        onlyUnanswered = preferences.getBoolean(PREF_ONLY_UNANSWERED, false);
        onlySolved = preferences.getBoolean(PREF_ONLY_SOLVED, false);
        
        // Note: Subject and tags will be loaded after DB connection is established
        // as they need to be looked up by ID from repositories
    }
    
    /**
     * Persists the current filter state to preferences.
     */
    private void persistState() {
        // Persist search text
        if (searchText != null && !searchText.isEmpty()) {
            preferences.put(PREF_SEARCH_TEXT, searchText);
        } else {
            preferences.remove(PREF_SEARCH_TEXT);
        }
        
        // Persist subject ID
        if (selectedSubject != null && selectedSubject.getId() != null) {
            preferences.putLong(PREF_SUBJECT_ID, selectedSubject.getId());
        } else {
            preferences.remove(PREF_SUBJECT_ID);
        }
        
        // Persist sort option
        preferences.put(PREF_SORT_OPTION, sortOption.name());
        
        // Persist boolean filters
        preferences.putBoolean(PREF_ONLY_UNANSWERED, onlyUnanswered);
        preferences.putBoolean(PREF_ONLY_SOLVED, onlySolved);
        
        // Persist tags (up to MAX_PERSISTED_TAGS)
        preferences.putInt(PREF_TAG_COUNT, Math.min(selectedTags.size(), MAX_PERSISTED_TAGS));
        for (int i = 0; i < Math.min(selectedTags.size(), MAX_PERSISTED_TAGS); i++) {
            Tag tag = selectedTags.get(i);
            preferences.put(PREF_TAG_PREFIX + i, tag.getName());
        }
        
        // Remove any excess persisted tags
        for (int i = selectedTags.size(); i < MAX_PERSISTED_TAGS; i++) {
            preferences.remove(PREF_TAG_PREFIX + i);
        }
    }
    
    /**
     * Loads the persisted subject ID from preferences and converts to Subject.
     * This must be called after database connection is established.
     * 
     * @param subjectProvider Function that provides a Subject given an ID
     */
    public void loadPersistedSubject(SubjectProvider subjectProvider) {
        if (subjectProvider == null) return;
        
        long subjectId = preferences.getLong(PREF_SUBJECT_ID, -1);
        if (subjectId != -1) {
            selectedSubject = subjectProvider.getSubjectById(subjectId);
        }
    }
    
    /**
     * Loads persisted tags from preferences.
     * This must be called after database connection is established.
     * 
     * @param tagProvider Function that provides a Tag given a name
     */
    public void loadPersistedTags(TagProvider tagProvider) {
        if (tagProvider == null) return;
        
        int tagCount = preferences.getInt(PREF_TAG_COUNT, 0);
        selectedTags.clear();
        
        for (int i = 0; i < tagCount; i++) {
            String tagName = preferences.get(PREF_TAG_PREFIX + i, null);
            if (tagName != null) {
                Tag tag = tagProvider.getTagByName(tagName);
                if (tag != null) {
                    selectedTags.add(tag);
                }
            }
        }
    }
    
    /**
     * Applies the current filter state to create a search criteria.
     * 
     * @return A QuestionSearchCriteria populated with current filter values
     */
    public QuestionSearchCriteria createSearchCriteria() {
        QuestionSearchCriteria criteria = new QuestionSearchCriteria();
        
        // Apply search text
        criteria.setSearchText(searchText);
        
        // Apply subject
        if (selectedSubject != null) {
            criteria.setSubjectId(selectedSubject.getId());
        }
        
        // Apply tags
        if (!selectedTags.isEmpty()) {
            List<String> tagNames = new ArrayList<>();
            for (Tag tag : selectedTags) {
                tagNames.add(tag.getName());
            }
            criteria.setTags(tagNames);
        }
        
        // Apply sort option
        criteria.setSortOption(sortOption);
        
        // Apply question status filters
        criteria.setOnlyUnanswered(onlyUnanswered);
        criteria.setOnlySolved(onlySolved);
        
        return criteria;
    }
    
    /**
     * Gets the current search text.
     * 
     * @return The search text
     */
    public String getSearchText() {
        return searchText;
    }
    
    /**
     * Sets the search text filter.
     * 
     * @param searchText The search text
     */
    public void setSearchText(String searchText) {
        this.searchText = searchText;
        persistState();
        notifyListeners();
    }
    
    /**
     * Gets the currently selected subject.
     * 
     * @return The selected subject, or null if none selected
     */
    public Subject getSelectedSubject() {
        return selectedSubject;
    }
    
    /**
     * Sets the selected subject filter.
     * 
     * @param subject The subject to select, or null for "All Subjects"
     */
    public void setSelectedSubject(Subject subject) {
        this.selectedSubject = subject;
        persistState();
        notifyListeners();
    }
    
    /**
     * Gets the currently selected tags.
     * 
     * @return List of selected tags
     */
    public List<Tag> getSelectedTags() {
        return new ArrayList<>(selectedTags);
    }
    
    /**
     * Sets the selected tags filter.
     * 
     * @param tags The list of tags to select
     */
    public void setSelectedTags(List<Tag> tags) {
        this.selectedTags = new ArrayList<>(tags);
        persistState();
        notifyListeners();
    }
    
    /**
     * Adds a tag to the selected tags.
     * 
     * @param tag The tag to add
     */
    public void addSelectedTag(Tag tag) {
        if (tag != null && !selectedTags.contains(tag)) {
            selectedTags.add(tag);
            persistState();
            notifyListeners();
        }
    }
    
    /**
     * Removes a tag from the selected tags.
     * 
     * @param tag The tag to remove
     */
    public void removeSelectedTag(Tag tag) {
        if (tag != null && selectedTags.remove(tag)) {
            persistState();
            notifyListeners();
        }
    }
    
    /**
     * Gets the current sort option.
     * 
     * @return The sort option
     */
    public QuestionSearchCriteria.SortOption getSortOption() {
        return sortOption;
    }
    
    /**
     * Sets the sort option filter.
     * 
     * @param sortOption The sort option to use
     */
    public void setSortOption(QuestionSearchCriteria.SortOption sortOption) {
        if (sortOption != null) {
            this.sortOption = sortOption;
            persistState();
            notifyListeners();
        }
    }
    
    /**
     * Checks if only unanswered questions filter is active.
     * 
     * @return true if only unanswered questions should be shown
     */
    public boolean isOnlyUnanswered() {
        return onlyUnanswered;
    }
    
    /**
     * Sets the only unanswered questions filter.
     * 
     * @param onlyUnanswered true to show only unanswered questions
     */
    public void setOnlyUnanswered(boolean onlyUnanswered) {
        if (onlyUnanswered && this.onlySolved) {
            this.onlySolved = false; // Can't have both filters active
        }
        this.onlyUnanswered = onlyUnanswered;
        persistState();
        notifyListeners();
    }
    
    /**
     * Checks if only solved questions filter is active.
     * 
     * @return true if only solved questions should be shown
     */
    public boolean isOnlySolved() {
        return onlySolved;
    }
    
    /**
     * Sets the only solved questions filter.
     * 
     * @param onlySolved true to show only solved questions
     */
    public void setOnlySolved(boolean onlySolved) {
        if (onlySolved && this.onlyUnanswered) {
            this.onlyUnanswered = false; // Can't have both filters active
        }
        this.onlySolved = onlySolved;
        persistState();
        notifyListeners();
    }
    
    /**
     * Clears all active filters.
     */
    public void clearAllFilters() {
        searchText = null;
        selectedSubject = null;
        selectedTags.clear();
        sortOption = QuestionSearchCriteria.SortOption.NEWEST;
        onlyUnanswered = false;
        onlySolved = false;
        persistState();
        notifyListeners();
    }
    
    /**
     * Adds a listener for filter change events.
     * 
     * @param listener The listener to add
     */
    public void addFilterChangeListener(FilterChangeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Removes a listener for filter change events.
     * 
     * @param listener The listener to remove
     */
    public void removeFilterChangeListener(FilterChangeListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Notifies all registered listeners about filter changes.
     */
    private void notifyListeners() {
        // Create search criteria for listeners
        QuestionSearchCriteria criteria = createSearchCriteria();
        
        // Create filter state map for UI updates
        Map<String, Object> filterState = new HashMap<>();
        filterState.put("searchText", searchText);
        filterState.put("selectedSubject", selectedSubject);
        filterState.put("selectedTags", new ArrayList<>(selectedTags));
        filterState.put("sortOption", sortOption);
        filterState.put("onlyUnanswered", onlyUnanswered);
        filterState.put("onlySolved", onlySolved);
        
        // Notify all listeners
        for (FilterChangeListener listener : new ArrayList<>(listeners)) {
            listener.onFilterChanged(criteria, filterState);
        }
    }
    
    /**
     * Interface for retrieving Subject objects by ID.
     */
    public interface SubjectProvider {
        Subject getSubjectById(long id);
    }
    
    /**
     * Interface for retrieving Tag objects by name.
     */
    public interface TagProvider {
        Tag getTagByName(String name);
    }
    
    /**
     * Interface for listening to filter change events.
     */
    public interface FilterChangeListener {
        /**
         * Called when filters are changed.
         * 
         * @param criteria The updated search criteria
         * @param filterState Map of current filter values for UI state synchronization
         */
        void onFilterChanged(QuestionSearchCriteria criteria, Map<String, Object> filterState);
    }
}