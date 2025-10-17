package com.upnext.app.service.search;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

/**
 * Test class for the TokenUtils utility class.
 * Validates token normalization, trigram generation, and similarity calculations.
 */
public class TokenUtilsTest {

    @Test
    public void testTokenize_WithValidInput_ReturnsTokens() {
        // Arrange
        String query = "Java Programming Skills";
        
        // Act
        List<String> tokens = TokenUtils.tokenize(query);
        
        // Assert
        assertEquals(3, tokens.size());
        assertEquals("java", tokens.get(0));
        assertEquals("programming", tokens.get(1));
        assertEquals("skills", tokens.get(2));
    }
    
    @Test
    public void testTokenize_WithEmptyInput_ReturnsEmptyList() {
        // Act & Assert
        assertTrue(TokenUtils.tokenize("").isEmpty());
        assertTrue(TokenUtils.tokenize("   ").isEmpty());
        assertTrue(TokenUtils.tokenize(null).isEmpty());
    }
    
    @Test
    public void testTokenize_WithSpecialCharacters_ReturnsCleanTokens() {
        // Arrange
        String query = "Java! Programming-Skills, and #frameworks";
        
        // Act
        List<String> tokens = TokenUtils.tokenize(query);
        
        // Assert
        assertEquals(4, tokens.size());
        assertEquals("java", tokens.get(0));
        assertEquals("programmingskills", tokens.get(1));
        assertEquals("and", tokens.get(2));
        assertEquals("frameworks", tokens.get(3));
    }
    
    @Test
    public void testTokenize_FiltersTooShortTokens() {
        // Arrange
        String query = "Java a the in Programming";
        
        // Act
        List<String> tokens = TokenUtils.tokenize(query);
        
        // Assert
        assertEquals(2, tokens.size());
        assertEquals("java", tokens.get(0));
        assertEquals("programming", tokens.get(1));
    }
    
    @Test
    public void testNormalize_WithMixedCase_ReturnsLowercase() {
        // Act & Assert
        assertEquals("java", TokenUtils.normalize("Java"));
        assertEquals("programming", TokenUtils.normalize("Programming"));
        assertEquals("javaproject", TokenUtils.normalize("JavaProject"));
    }
    
    @Test
    public void testNormalize_WithSpecialChars_RemovesThem() {
        // Act & Assert
        assertEquals("java", TokenUtils.normalize("Java!"));
        assertEquals("csharp", TokenUtils.normalize("C#"));
        assertEquals("cplusplus", TokenUtils.normalize("C++"));
    }
    
    @Test
    public void testRemoveDuplicates_WithDuplicates_RemovesThem() {
        // Arrange
        List<String> tokens = Arrays.asList("java", "programming", "java", "skills", "programming");
        
        // Act
        List<String> uniqueTokens = TokenUtils.removeDuplicates(tokens);
        
        // Assert
        assertEquals(3, uniqueTokens.size());
        assertTrue(uniqueTokens.contains("java"));
        assertTrue(uniqueTokens.contains("programming"));
        assertTrue(uniqueTokens.contains("skills"));
    }
    
    @Test
    public void testRemoveDuplicates_WithEmptyList_ReturnsEmptyList() {
        // Act & Assert
        assertTrue(TokenUtils.removeDuplicates(null).isEmpty());
        assertTrue(TokenUtils.removeDuplicates(List.of()).isEmpty());
    }
    
    @Test
    public void testGenerateTrigrams_WithValidToken_ReturnsTrigrams() {
        // Act
        List<String> trigrams = TokenUtils.generateTrigrams("programming");
        
        // Assert
        assertEquals(9, trigrams.size());
        assertEquals("pro", trigrams.get(0));
        assertEquals("rog", trigrams.get(1));
        assertEquals("ogr", trigrams.get(2));
        assertEquals("gra", trigrams.get(3));
        // ... and so on
    }
    
    @Test
    public void testGenerateTrigrams_WithShortToken_ReturnsEmptyList() {
        // Act & Assert
        assertTrue(TokenUtils.generateTrigrams("abc").isEmpty());
        assertTrue(TokenUtils.generateTrigrams("ab").isEmpty());
        assertTrue(TokenUtils.generateTrigrams("a").isEmpty());
        assertTrue(TokenUtils.generateTrigrams("").isEmpty());
        assertTrue(TokenUtils.generateTrigrams(null).isEmpty());
    }
    
    @Test
    public void testGenerateAllTrigrams_WithValidTokens_ReturnsAllTrigrams() {
        // Arrange
        List<String> tokens = Arrays.asList("programming", "javascript");
        
        // Act
        List<String> allTrigrams = TokenUtils.generateAllTrigrams(tokens);
        
        // Assert
        assertEquals(17, allTrigrams.size()); // 9 unique from "programming" + 8 unique from "javascript"
    }
    
    @Test
    public void testGenerateAllTrigrams_WithEmptyList_ReturnsEmptyList() {
        // Act & Assert
        assertTrue(TokenUtils.generateAllTrigrams(null).isEmpty());
        assertTrue(TokenUtils.generateAllTrigrams(List.of()).isEmpty());
    }
    
    @Test
    public void testProcessSearchQuery_WithValidQuery_ReturnsProcessedTokens() {
        // Arrange
        String query = "Java Programming Java";
        
        // Act
        List<String> processed = TokenUtils.processSearchQuery(query);
        
        // Assert
        assertTrue(processed.contains("java"));
        assertTrue(processed.contains("programming"));
        // Should also contain trigrams for "programming"
        assertTrue(processed.contains("pro"));
        assertTrue(processed.contains("rog"));
        assertTrue(processed.contains("ogr"));
    }
    
    @Test
    public void testCalculateSimilarity_WithExactMatch_Returns1() {
        // Act & Assert
        assertEquals(1.0, TokenUtils.calculateSimilarity("programming", "programming"));
        assertEquals(1.0, TokenUtils.calculateSimilarity("Programming", "programming"));
    }
    
    @Test
    public void testCalculateSimilarity_WithSimilarTokens_ReturnsPositiveScore() {
        // Act
        double similarity = TokenUtils.calculateSimilarity("programming", "programmer");
        
        // Assert
        assertTrue(similarity > 0.5); // High similarity expected
    }
    
    @Test
    public void testCalculateSimilarity_WithDifferentTokens_ReturnsLowScore() {
        // Act
        double similarity = TokenUtils.calculateSimilarity("programming", "javascript");
        
        // Assert
        assertTrue(similarity < 0.5); // Low similarity expected
    }
    
    @Test
    public void testCalculateSimilarity_WithNullOrEmptyInput_Returns0() {
        // Act & Assert
        assertEquals(0.0, TokenUtils.calculateSimilarity(null, "programming"));
        assertEquals(0.0, TokenUtils.calculateSimilarity("programming", null));
        assertEquals(0.0, TokenUtils.calculateSimilarity(null, null));
        assertEquals(0.0, TokenUtils.calculateSimilarity("", "programming"));
        assertEquals(0.0, TokenUtils.calculateSimilarity("programming", ""));
        assertEquals(0.0, TokenUtils.calculateSimilarity("", ""));
    }
}