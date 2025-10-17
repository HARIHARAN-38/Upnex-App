package com.upnext.app.service;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.upnext.app.core.Logger;
import com.upnext.app.data.question.QuestionRepository;
import com.upnext.app.domain.question.Question;
import com.upnext.app.domain.question.QuestionSearchCriteria;
import com.upnext.app.service.search.TokenUtils;

/**
 * Service class for searching and retrieving questions with advanced features.
 * Provides fuzzy search capabilities using token utilities and result ranking.
 */
public class SearchService {
    private static final Logger LOGGER = Logger.getInstance();
    private static final SearchService INSTANCE = new SearchService();
    
    // Thresholds for fuzzy matching
    private static final double TITLE_MATCH_WEIGHT = 0.7;
    private static final double CONTENT_MATCH_WEIGHT = 0.3;
    private static final double SIMILARITY_THRESHOLD = 0.5;
    
    // Maximum number of results to process for fuzzy matching
    private static final int MAX_FUZZY_CANDIDATES = 100;
    
    private final QuestionDataAccess questionDataAccess;
    
    /**
     * Private constructor to enforce singleton pattern.
     */
    private SearchService() {
        this(new RepositoryQuestionDataAccess(QuestionRepository.getInstance()));
    }

    SearchService(QuestionDataAccess questionDataAccess) {
        this.questionDataAccess = Objects.requireNonNull(questionDataAccess, "questionDataAccess");
    }
    
    /**
     * Gets the singleton instance of the search service.
     * 
     * @return The search service instance
     */
    public static SearchService getInstance() {
        return INSTANCE;
    }
    
    /**
     * Searches for questions using the provided query text with exact match.
     * 
     * @param query The query text to search for
     * @param limit Maximum number of results to return
     * @param offset Offset for pagination
     * @return A list of matching questions
     */
    public List<Question> searchExact(String query, int limit, int offset) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        try {
            QuestionSearchCriteria criteria = new QuestionSearchCriteria()
                .setSearchText(query)
                .setLimit(limit)
                .setOffset(offset);
                
            return questionDataAccess.search(criteria);
        } catch (SQLException e) {
            LOGGER.logException("Error searching questions with exact match", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Searches for questions using the provided query text with fuzzy matching.
     * Uses trigrams for improved matching and ranks results by relevance.
     * 
     * @param query The query text to search for
     * @param limit Maximum number of results to return
     * @param offset Offset for pagination
     * @return A list of matching questions ordered by relevance
     */
    public List<Question> searchFuzzy(String query, int limit, int offset) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        // First try exact search
        List<Question> exactMatches = searchExact(query, limit, offset);
        if (!exactMatches.isEmpty()) {
            return exactMatches;
        }
        
        // If no exact matches, proceed with fuzzy search
        try {
            // Prepare tokens from the query
            List<String> queryTokens = TokenUtils.tokenize(query);
            if (queryTokens.isEmpty()) {
                return Collections.emptyList();
            }
            
        // Get a broader set of potential matches using LIKE with the longest token
        String longestToken = queryTokens.stream()
            .max(Comparator.comparing(String::length))
            .orElse("");

        String candidateToken = extractCandidateToken(query, longestToken);

        if (candidateToken.length() < 3) {
        candidateToken = query;
        }

        // Create a criteria with the longest token to get candidate matches
            QuestionSearchCriteria criteria = new QuestionSearchCriteria()
            .setSearchText(candidateToken)
                    .setLimit(MAX_FUZZY_CANDIDATES);
                    
            List<Question> candidates = questionDataAccess.search(criteria);
            
            // Calculate relevance scores for each candidate
            Map<Question, Double> scoreMap = new HashMap<>();
            for (Question candidate : candidates) {
                double score = calculateRelevanceScore(candidate, queryTokens);
                if (score >= SIMILARITY_THRESHOLD) {
                    scoreMap.put(candidate, score);
                }
            }
            
            // Sort by relevance score and apply pagination
            return scoreMap.entrySet().stream()
                    .sorted(Map.Entry.<Question, Double>comparingByValue().reversed())
                    .skip(offset)
                    .limit(limit)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
                    
        } catch (SQLException e) {
            LOGGER.logException("Error performing fuzzy search for questions", e);
            return Collections.emptyList();
        }
    }

        private String extractCandidateToken(String query, String normalizedToken) {
            if (query == null || query.trim().isEmpty()) {
                return normalizedToken == null ? "" : normalizedToken;
            }

            List<String> originalTokens = Arrays.stream(query.trim().split("\\s+"))
                    .map(String::trim)
                    .filter(token -> token.length() >= 3)
                    .collect(Collectors.toList());

            if (originalTokens.isEmpty()) {
                return normalizedToken == null ? query.trim() : normalizedToken;
            }

            String matchingOriginal = originalTokens.stream()
                    .filter(token -> TokenUtils.normalize(token).equals(normalizedToken))
                    .findFirst()
                    .orElse(null);

            if (matchingOriginal != null) {
                return matchingOriginal;
            }

            return originalTokens.stream()
                    .max(Comparator.comparingInt(String::length))
                    .orElse(normalizedToken == null ? query.trim() : normalizedToken);
        }
    
    /**
     * Searches for questions with combined strategies (exact + fuzzy).
     * First tries exact matching, then falls back to fuzzy matching if needed.
     * 
     * @param query The query text to search for
     * @param limit Maximum number of results to return
     * @param offset Offset for pagination
     * @return A list of matching questions
     */
    public List<Question> search(String query, int limit, int offset) {
        if (query == null || query.trim().isEmpty()) {
            try {
                // If query is empty, return recent questions
                return questionDataAccess.findPage(limit, offset);
            } catch (SQLException e) {
                LOGGER.logException("Error retrieving recent questions", e);
                return Collections.emptyList();
            }
        }
        
        // Try exact search first
        List<Question> exactMatches = searchExact(query, limit, offset);
        
        // If exact search found results, return them
        if (!exactMatches.isEmpty()) {
            return exactMatches;
        }
        
        // Otherwise, fall back to fuzzy search
        return searchFuzzy(query, limit, offset);
    }
    
    /**
     * Searches for questions using advanced filtering criteria.
     * 
     * @param criteria The search criteria to apply
     * @return A list of matching questions
     */
    public List<Question> searchWithCriteria(QuestionSearchCriteria criteria) {
        if (criteria == null) {
            return Collections.emptyList();
        }
        
        try {
            return questionDataAccess.search(criteria);
        } catch (SQLException e) {
            LOGGER.logException("Error searching questions with criteria", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Calculates a relevance score for a question based on how well it matches the query tokens.
     * Uses a weighted combination of title and content matches.
     * 
     * @param question The question to score
     * @param queryTokens The tokens from the search query
     * @return A relevance score between 0.0 and 1.0
     */
    private double calculateRelevanceScore(Question question, List<String> queryTokens) {
        Objects.requireNonNull(question, "question");
        
        if (queryTokens == null || queryTokens.isEmpty()) {
            return 0.0;
        }
        
        // Get question tokens
        List<String> titleTokens = TokenUtils.tokenize(question.getTitle());
        List<String> contentTokens = TokenUtils.tokenize(question.getContent());
        
        // Calculate best match scores for each query token against title tokens
        double titleScore = calculateTokenSetSimilarity(queryTokens, titleTokens);
        
        // Calculate best match scores for each query token against content tokens
        double contentScore = calculateTokenSetSimilarity(queryTokens, contentTokens);
        
        // Combine scores with weights
        return (TITLE_MATCH_WEIGHT * titleScore) + (CONTENT_MATCH_WEIGHT * contentScore);
    }
    
    /**
     * Calculates similarity between two sets of tokens.
     * For each query token, finds the best matching token in the document.
     * 
     * @param queryTokens The tokens from the search query
     * @param documentTokens The tokens from the document (title or content)
     * @return A similarity score between 0.0 and 1.0
     */
    private double calculateTokenSetSimilarity(List<String> queryTokens, List<String> documentTokens) {
        if (queryTokens.isEmpty() || documentTokens.isEmpty()) {
            return 0.0;
        }
        
        double totalScore = 0.0;
        for (String queryToken : queryTokens) {
            double bestMatch = 0.0;
            for (String docToken : documentTokens) {
                double similarity = TokenUtils.calculateSimilarity(queryToken, docToken);
                bestMatch = Math.max(bestMatch, similarity);
            }
            totalScore += bestMatch;
        }
        
        return totalScore / queryTokens.size();
    }
    
    /**
     * Gets recommended related questions based on a source question.
     * 
     * @param sourceQuestion The source question to find related questions for
     * @param limit Maximum number of results to return
     * @return A list of related questions
     */
    public List<Question> getRelatedQuestions(Question sourceQuestion, int limit) {
        if (sourceQuestion == null) {
            return Collections.emptyList();
        }
        
        try {
            // Create a combined search string from title and tags
            StringBuilder searchBuilder = new StringBuilder(sourceQuestion.getTitle());
            for (String tag : sourceQuestion.getTags()) {
                searchBuilder.append(" ").append(tag);
            }
            
            // Create search criteria with same subject as source question
            QuestionSearchCriteria criteria = new QuestionSearchCriteria()
                    .setSearchText(searchBuilder.toString())
                    .setSubjectId(sourceQuestion.getSubjectId())
                    .setLimit(limit * 2);  // Get more candidates for filtering
                    
            // Exclude the source question itself
            List<Question> candidates = questionDataAccess.search(criteria).stream()
                    .filter(q -> !q.getId().equals(sourceQuestion.getId()))
                    .collect(Collectors.toList());
                    
            // Score candidates by relevance
            Map<Question, Double> scoreMap = new HashMap<>();
            for (Question candidate : candidates) {
                double score = calculateSimilarityBetweenQuestions(sourceQuestion, candidate);
                scoreMap.put(candidate, score);
            }
            
            // Return top N results by score
            return scoreMap.entrySet().stream()
                    .sorted(Map.Entry.<Question, Double>comparingByValue().reversed())
                    .limit(limit)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
                    
        } catch (SQLException e) {
            LOGGER.logException("Error finding related questions", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Calculates similarity between two questions.
     * 
     * @param q1 First question
     * @param q2 Second question
     * @return A similarity score between 0.0 and 1.0
     */
    private double calculateSimilarityBetweenQuestions(Question q1, Question q2) {
        // Title similarity (weighted highest)
        double titleSimilarity = calculateTextSimilarity(q1.getTitle(), q2.getTitle());
        
        // Content similarity (weighted medium)
        double contentSimilarity = calculateTextSimilarity(q1.getContent(), q2.getContent());
        
        // Tag similarity (weighted)
        double tagSimilarity = calculateTagSimilarity(q1.getTags(), q2.getTags());
        
        // Subject similarity (bonus)
        double subjectBonus = isSameSubject(q1, q2) ? 0.2 : 0.0;
        
        // Combined weighted score
        return (0.5 * titleSimilarity) + (0.3 * contentSimilarity) + 
               (0.2 * tagSimilarity) + subjectBonus;
    }
    
    /**
     * Calculates similarity between two text strings.
     * 
     * @param text1 First text
     * @param text2 Second text
     * @return A similarity score between 0.0 and 1.0
     */
    private double calculateTextSimilarity(String text1, String text2) {
        List<String> tokens1 = TokenUtils.tokenize(text1);
        List<String> tokens2 = TokenUtils.tokenize(text2);
        return calculateTokenSetSimilarity(tokens1, tokens2);
    }
    
    /**
     * Calculates similarity between two tag sets.
     * 
     * @param tags1 First set of tags
     * @param tags2 Second set of tags
     * @return A similarity score between 0.0 and 1.0
     */
    private double calculateTagSimilarity(List<String> tags1, List<String> tags2) {
        if (tags1.isEmpty() && tags2.isEmpty()) {
            return 0.0;
        }
        
        // Count matching tags
        Set<String> set1 = new HashSet<>(tags1.stream()
                .map(TokenUtils::normalize)
                .collect(Collectors.toList()));
                
        Set<String> set2 = new HashSet<>(tags2.stream()
                .map(TokenUtils::normalize)
                .collect(Collectors.toList()));
                
        // Calculate Jaccard similarity: |intersection| / |union|
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);
        
        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }
    
    /**
     * Checks if two questions have the same subject.
     * 
     * @param q1 First question
     * @param q2 Second question
     * @return true if both questions have the same subject, false otherwise
     */
    private boolean isSameSubject(Question q1, Question q2) {
        return q1.getSubjectId() != null && 
               q2.getSubjectId() != null && 
               q1.getSubjectId().equals(q2.getSubjectId());
    }

    interface QuestionDataAccess {
        List<Question> search(QuestionSearchCriteria criteria) throws SQLException;

        List<Question> findPage(int limit, int offset) throws SQLException;
    }

    private static final class RepositoryQuestionDataAccess implements QuestionDataAccess {
        private final QuestionRepository repository;

        RepositoryQuestionDataAccess(QuestionRepository repository) {
            this.repository = Objects.requireNonNull(repository, "repository");
        }

        @Override
        public List<Question> search(QuestionSearchCriteria criteria) throws SQLException {
            return repository.search(criteria);
        }

        @Override
        public List<Question> findPage(int limit, int offset) throws SQLException {
            return repository.findPage(limit, offset);
        }
    }
}