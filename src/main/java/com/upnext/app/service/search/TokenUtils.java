package com.upnext.app.service.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Utility class for tokenizing and processing search terms.
 * Provides functionality for text normalization, token extraction,
 * deduplication, and trigram generation for fuzzy search operations.
 */
public class TokenUtils {
    
    // Pattern to match non-alphanumeric characters (excluding spaces)
    private static final Pattern CLEAN_PATTERN = Pattern.compile("[^a-zA-Z0-9\\s]");
    
    // Minimum length for a token to be considered valid
    // (filter out short stop-words like "a", "in", "to")
    private static final int MIN_TOKEN_LENGTH = 3;

    // Lightweight stop-word list for common filler terms
    private static final Set<String> STOP_WORDS = Set.of("the");
    
    // Minimum length for a token to generate trigrams
    private static final int MIN_TRIGRAM_TOKEN_LENGTH = 4;
    
    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private TokenUtils() {
        // Utility class should not be instantiated
    }
    
    /**
     * Normalizes and tokenizes a search query into individual terms.
     * 
     * @param query The search query to tokenize
     * @return A list of normalized tokens
     */
    public static List<String> tokenize(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        // Normalize and split the query into tokens
        String normalizedQuery = normalize(query);
        String[] tokens = normalizedQuery.split("\\s+");
        
        // Filter out tokens that are too short
    return Arrays.stream(tokens)
        .filter(token -> token.length() >= MIN_TOKEN_LENGTH)
        .filter(token -> !STOP_WORDS.contains(token))
        .collect(Collectors.toList());
    }
    
    /**
     * Normalizes text by converting to lowercase and removing special characters.
     * 
     * @param text The text to normalize
     * @return The normalized text
     */
    public static String normalize(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }
        
        // Handle explicit language tokens first
        String trimmed = text.trim().toLowerCase();
        if (trimmed.equals("c#")) return "csharp";
        if (trimmed.equals("c++")) return "cplusplus";

        // Convert to lowercase and remove special characters
        return CLEAN_PATTERN.matcher(trimmed).replaceAll("");
    }
    
    /**
     * Removes duplicate tokens from a list.
     * 
     * @param tokens The list of tokens to deduplicate
     * @return A new list with duplicates removed
     */
    public static List<String> removeDuplicates(List<String> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            return Collections.emptyList();
        }
        
        // Use a Set to remove duplicates while preserving order
        return new ArrayList<>(new HashSet<>(tokens));
    }
    
    /**
     * Generates trigrams for a token to support fuzzy matching.
     * Only generates trigrams for tokens longer than MIN_TRIGRAM_TOKEN_LENGTH.
     * 
     * @param token The token to generate trigrams from
     * @return A list of trigrams, or an empty list if the token is too short
     */
    public static List<String> generateTrigrams(String token) {
        if (token == null || token.length() < MIN_TRIGRAM_TOKEN_LENGTH) {
            return Collections.emptyList();
        }
        
        List<String> trigrams = new ArrayList<>();
        for (int i = 0; i <= token.length() - 3; i++) {
            trigrams.add(token.substring(i, i + 3));
        }
        
        return trigrams;
    }
    
    /**
     * Generates all trigrams for a list of tokens.
     * 
     * @param tokens The list of tokens to generate trigrams from
     * @return A list of all trigrams from all tokens
     */
    public static List<String> generateAllTrigrams(List<String> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            return Collections.emptyList();
        }
        
        Set<String> allTrigrams = new HashSet<>();
        for (String token : tokens) {
            allTrigrams.addAll(generateTrigrams(token));
        }
        
        return new ArrayList<>(allTrigrams);
    }
    
    /**
     * Processes a search query for optimal search performance.
     * Normalizes, tokenizes, deduplicates, and includes trigrams for fuzzy matching.
     * 
     * @param query The search query to process
     * @return A list of processed tokens and trigrams
     */
    public static List<String> processSearchQuery(String query) {
        // Tokenize the query
        List<String> tokens = tokenize(query);
        
        // Remove duplicates
        tokens = removeDuplicates(tokens);
        
        // Generate trigrams and add to tokens
        List<String> trigrams = generateAllTrigrams(tokens);
        
        // Combine original tokens and trigrams
        Set<String> result = new HashSet<>(tokens);
        result.addAll(trigrams);
        
        return new ArrayList<>(result);
    }
    
    /**
     * Calculates the similarity score between two tokens using trigram matching.
     * Higher score indicates greater similarity.
     * 
     * @param token1 The first token
     * @param token2 The second token
     * @return A similarity score between 0.0 (no match) and 1.0 (perfect match)
     */
    public static double calculateSimilarity(String token1, String token2) {
        if (token1 == null || token2 == null) {
            return 0.0;
        }
        
        // Normalize tokens
        token1 = normalize(token1);
        token2 = normalize(token2);
        
        // If either token is empty after normalization, no similarity
        if (token1.isEmpty() || token2.isEmpty()) {
            return 0.0;
        }
        
        // Exact match
        if (token1.equals(token2)) {
            return 1.0;
        }
        
        // Generate trigrams for both tokens
        List<String> trigrams1 = generateTrigrams(token1);
        List<String> trigrams2 = generateTrigrams(token2);
        
        // If either token is too short for trigrams, compare directly
        if (trigrams1.isEmpty() || trigrams2.isEmpty()) {
            // Simple partial match for short tokens
            if (token1.contains(token2) || token2.contains(token1)) {
                double maxLength = Math.max(token1.length(), token2.length());
                double minLength = Math.min(token1.length(), token2.length());
                return minLength / maxLength;
            }
            return 0.0;
        }
        
        // Count matching trigrams
        Set<String> set1 = new HashSet<>(trigrams1);
        Set<String> set2 = new HashSet<>(trigrams2);
        
        // Calculate intersection
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        
        // Calculate union
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);
        
        // Jaccard similarity: intersection size / union size
        return (double) intersection.size() / union.size();
    }
}