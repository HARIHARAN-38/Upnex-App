package com.upnext.app.domain.question;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing criteria for searching and filtering questions.
 * Used to build complex queries for the question repository.
 */
public class QuestionSearchCriteria {
    private String searchText;
    private Long subjectId;
    private List<String> tags = new ArrayList<>();
    private Long userId;
    private SortOption sortOption = SortOption.NEWEST;
    private boolean onlyUnanswered = false;
    private boolean onlySolved = false;
    private int limit = 20;
    private int offset = 0;
    
    /**
     * Enum defining sort options for question search results.
     */
    public enum SortOption {
        NEWEST("created_at DESC"),
        OLDEST("created_at ASC"),
        MOST_UPVOTED("upvotes DESC"),
        MOST_VIEWED("view_count DESC"),
        MOST_ANSWERED("answer_count DESC");
        
        private final String sqlOrderBy;
        
        SortOption(String sqlOrderBy) {
            this.sqlOrderBy = sqlOrderBy;
        }
        
        /**
         * Gets the SQL ORDER BY clause for this sort option.
         * 
         * @return The SQL ORDER BY clause
         */
        public String getSqlOrderBy() {
            return sqlOrderBy;
        }
    }
    
    /**
     * Gets the search text for finding in question title or content.
     * 
     * @return The search text
     */
    public String getSearchText() {
        return searchText;
    }
    
    /**
     * Sets the search text for finding in question title or content.
     * 
     * @param searchText The search text
     * @return This criteria object for chaining
     */
    public QuestionSearchCriteria setSearchText(String searchText) {
        this.searchText = searchText;
        return this;
    }
    
    /**
     * Gets the subject ID filter.
     * 
     * @return The subject ID
     */
    public Long getSubjectId() {
        return subjectId;
    }
    
    /**
     * Sets the subject ID filter.
     * 
     * @param subjectId The subject ID
     * @return This criteria object for chaining
     */
    public QuestionSearchCriteria setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
        return this;
    }
    
    /**
     * Gets the tag filters.
     * 
     * @return The list of tags
     */
    public List<String> getTags() {
        return tags;
    }
    
    /**
     * Sets the tag filters.
     * 
     * @param tags The list of tags
     * @return This criteria object for chaining
     */
    public QuestionSearchCriteria setTags(List<String> tags) {
        this.tags = tags != null ? tags : new ArrayList<>();
        return this;
    }
    
    /**
     * Adds a tag to the filter.
     * 
     * @param tag The tag to add
     * @return This criteria object for chaining
     */
    public QuestionSearchCriteria addTag(String tag) {
        if (tag != null && !tag.trim().isEmpty()) {
            this.tags.add(tag);
        }
        return this;
    }
    
    /**
     * Gets the user ID filter for finding questions by a specific user.
     * 
     * @return The user ID
     */
    public Long getUserId() {
        return userId;
    }
    
    /**
     * Sets the user ID filter for finding questions by a specific user.
     * 
     * @param userId The user ID
     * @return This criteria object for chaining
     */
    public QuestionSearchCriteria setUserId(Long userId) {
        this.userId = userId;
        return this;
    }
    
    /**
     * Gets the sort option for results.
     * 
     * @return The sort option
     */
    public SortOption getSortOption() {
        return sortOption;
    }
    
    /**
     * Sets the sort option for results.
     * 
     * @param sortOption The sort option
     * @return This criteria object for chaining
     */
    public QuestionSearchCriteria setSortOption(SortOption sortOption) {
        this.sortOption = sortOption != null ? sortOption : SortOption.NEWEST;
        return this;
    }
    
    /**
     * Checks if the filter should only include unanswered questions.
     * 
     * @return true if only unanswered questions should be returned, false otherwise
     */
    public boolean isOnlyUnanswered() {
        return onlyUnanswered;
    }
    
    /**
     * Sets whether the filter should only include unanswered questions.
     * 
     * @param onlyUnanswered true if only unanswered questions should be returned
     * @return This criteria object for chaining
     */
    public QuestionSearchCriteria setOnlyUnanswered(boolean onlyUnanswered) {
        this.onlyUnanswered = onlyUnanswered;
        return this;
    }
    
    /**
     * Checks if the filter should only include solved questions.
     * 
     * @return true if only solved questions should be returned, false otherwise
     */
    public boolean isOnlySolved() {
        return onlySolved;
    }
    
    /**
     * Sets whether the filter should only include solved questions.
     * 
     * @param onlySolved true if only solved questions should be returned
     * @return This criteria object for chaining
     */
    public QuestionSearchCriteria setOnlySolved(boolean onlySolved) {
        this.onlySolved = onlySolved;
        return this;
    }
    
    /**
     * Gets the maximum number of results to return.
     * 
     * @return The limit
     */
    public int getLimit() {
        return limit;
    }
    
    /**
     * Sets the maximum number of results to return.
     * 
     * @param limit The limit
     * @return This criteria object for chaining
     */
    public QuestionSearchCriteria setLimit(int limit) {
        this.limit = limit > 0 ? limit : 20;
        return this;
    }
    
    /**
     * Gets the offset for pagination.
     * 
     * @return The offset
     */
    public int getOffset() {
        return offset;
    }
    
    /**
     * Sets the offset for pagination.
     * 
     * @param offset The offset
     * @return This criteria object for chaining
     */
    public QuestionSearchCriteria setOffset(int offset) {
        this.offset = offset >= 0 ? offset : 0;
        return this;
    }
}