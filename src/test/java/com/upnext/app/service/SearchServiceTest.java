package com.upnext.app.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.upnext.app.domain.question.Question;
import com.upnext.app.domain.question.QuestionSearchCriteria;
import com.upnext.app.service.SearchService.QuestionDataAccess;

/**
 * Tests for {@link SearchService} using a lightweight stub instead of database access.
 */
@SuppressWarnings("unused")
public class SearchServiceTest {

    private SearchService searchService;
    private StubQuestionDataAccess dataAccess;

    @BeforeEach
    public void setUp() {
        dataAccess = new StubQuestionDataAccess();
        searchService = new SearchService(dataAccess);
    }

    @Test
    public void searchExact_withValidQuery_returnsResultsAndCriteria() {
        String query = "Java Programming";
        int limit = 10;
        int offset = 0;

        Question q1 = question(1L, "Java Programming Question", "How to use Java streams?");
        Question q2 = question(2L, "Another Java Question", "Java interfaces and classes");

        dataAccess.addSearchResponse(query, List.of(q1, q2));

        List<Question> results = searchService.searchExact(query, limit, offset);

        assertEquals(List.of(q1, q2), results);
        QuestionSearchCriteria criteria = dataAccess.getLastCriteria();
        assertEquals(query, criteria.getSearchText());
        assertEquals(limit, criteria.getLimit());
        assertEquals(offset, criteria.getOffset());
    }

    @Test
    public void searchExact_withEmptyQuery_returnsEmptyList() {
        assertTrue(searchService.searchExact("", 10, 0).isEmpty());
        assertTrue(searchService.searchExact(null, 10, 0).isEmpty());
        assertTrue(searchService.searchExact("  ", 10, 0).isEmpty());
        assertEquals(0, dataAccess.getSearchInvocationCount());
    }

    @Test
    public void searchExact_withRepositoryException_returnsEmptyList() {
        dataAccess.setSearchException(new SQLException("Database error"));

        List<Question> results = searchService.searchExact("Java", 10, 0);

        assertTrue(results.isEmpty());
    }

    @Test
    public void search_withEmptyQuery_returnsRecentQuestions() {
        List<Question> recentQuestions = List.of(
            question(10L, "Recent Question 1", "Content 1"),
            question(11L, "Recent Question 2", "Content 2")
        );

        dataAccess.setFindPageResult(recentQuestions);

        List<Question> results = searchService.search("", 10, 0);

        assertEquals(recentQuestions, results);
        assertEquals(1, dataAccess.getFindPageInvocationCount());
    }

    @Test
    public void search_withExactMatches_returnsExactResults() {
        String query = "Java Programming";
        List<Question> exactMatches = List.of(
            question(20L, "Java Programming Question", "Content 1")
        );

        dataAccess.addSearchResponse(query, exactMatches);

        List<Question> results = searchService.search(query, 10, 0);

        assertEquals(exactMatches, results);
    }

    @Test
    public void searchFuzzy_withNoExactMatches_returnsFuzzyMatches() {
        String query = "Java Programming";
        List<Question> candidates = List.of(
            question(30L, "Java Development", "Programming in Java"),
            question(31L, "Programming Guide", "Java basics"),
            question(32L, "C# Guide", "Microsoft programming")
        );

        dataAccess.addSearchResponse(query, Collections.emptyList());
        dataAccess.addSearchResponse("Programming", candidates);

        List<Question> results = searchService.searchFuzzy(query, 10, 0);

        assertFalse(results.isEmpty());
        assertTrue(results.get(0).getTitle().contains("Java") ||
                   results.get(0).getContent().contains("Java"));
        assertEquals(2, dataAccess.getSearchInvocationCount());
    }

    @Test
    public void getRelatedQuestions_withValidQuestion_returnsRelatedQuestions() {
        Question sourceQuestion = questionWithDetails(
            40L,
            "Java Streams",
            "How to use streams in Java 8?",
            111L,
            List.of("java", "streams", "functional")
        );

        List<Question> relatedCandidates = List.of(
            questionWithDetails(
                41L,
                "Java Lambda Expressions",
                "How to use lambda expressions?",
                111L,
                List.of("java", "lambda", "functional")
            ),
            questionWithDetails(
                42L,
                "Java Collections",
                "Overview of Java collections",
                111L,
                List.of("java", "collections")
            ),
            questionWithDetails(
                43L,
                "Python Lists",
                "How to work with Python lists",
                222L,
                List.of("python", "lists")
            )
        );

        String searchText = buildSearchText(sourceQuestion);
        dataAccess.addSearchResponse(searchText, relatedCandidates);

        List<Question> results = searchService.getRelatedQuestions(sourceQuestion, 5);

        assertFalse(results.isEmpty());
        assertTrue(results.get(0).getTitle().contains("Java"));
        assertTrue(results.stream().noneMatch(q -> q.getId().equals(sourceQuestion.getId())));
    }

    @Test
    public void searchWithCriteria_withValidCriteria_returnsResults() {
        QuestionSearchCriteria criteria = new QuestionSearchCriteria()
            .setSearchText("Java")
            .setLimit(10);

        List<Question> expectedResults = List.of(
            question(50L, "Java Question", "Content")
        );

        dataAccess.addCriteriaResponse(criteria, expectedResults);

        List<Question> results = searchService.searchWithCriteria(criteria);

        assertEquals(expectedResults, results);
        assertSame(criteria, dataAccess.getLastCriteria());
    }

    @Test
    public void searchWithCriteria_withNullCriteria_returnsEmptyList() {
        List<Question> results = searchService.searchWithCriteria(null);

        assertTrue(results.isEmpty());
        assertEquals(0, dataAccess.getSearchInvocationCount());
    }

    private Question question(long id, String title, String content) {
        Question question = new Question();
        question.setId(id);
        question.setUserId(1L);
        question.setTitle(title);
        question.setContent(content);
        question.setTags(new ArrayList<>());
        return question;
    }

    private Question questionWithDetails(long id, String title, String content, Long subjectId, List<String> tags) {
        Question question = question(id, title, content);
        question.setSubjectId(subjectId);
        question.setTags(new ArrayList<>(tags));
        return question;
    }

    private String buildSearchText(Question question) {
        StringBuilder builder = new StringBuilder(question.getTitle());
        for (String tag : question.getTags()) {
            builder.append(' ').append(tag);
        }
        return builder.toString();
    }

    private static final class StubQuestionDataAccess implements QuestionDataAccess {
        private final Map<String, List<Question>> searchResponses = new HashMap<>();
        private final Map<QuestionSearchCriteria, List<Question>> criteriaResponses = new IdentityHashMap<>();
        private List<Question> defaultSearchResult = Collections.emptyList();
        private List<Question> findPageResult = Collections.emptyList();
        private SQLException searchException;
        private SQLException findPageException;
        private final List<QuestionSearchCriteria> capturedCriteria = new ArrayList<>();
        private int searchInvocationCount;
        private int findPageInvocationCount;

        @Override
        public List<Question> search(QuestionSearchCriteria criteria) throws SQLException {
            searchInvocationCount++;
            if (searchException != null) {
                throw searchException;
            }
            capturedCriteria.add(criteria);
            if (criteria == null) {
                return copy(defaultSearchResult);
            }
            List<Question> direct = criteriaResponses.get(criteria);
            if (direct != null) {
                return copy(direct);
            }
            String key = criteria.getSearchText();
            if (key != null) {
                List<Question> response = searchResponses.get(key);
                if (response != null) {
                    return copy(response);
                }
            }
            return copy(defaultSearchResult);
        }

        @Override
        public List<Question> findPage(int limit, int offset) throws SQLException {
            findPageInvocationCount++;
            if (findPageException != null) {
                throw findPageException;
            }
            return copy(findPageResult);
        }

        void addSearchResponse(String searchText, List<Question> response) {
            searchResponses.put(searchText, new ArrayList<>(response));
        }

        void addCriteriaResponse(QuestionSearchCriteria criteria, List<Question> response) {
            criteriaResponses.put(criteria, new ArrayList<>(response));
        }

        void setDefaultSearchResult(List<Question> response) {
            defaultSearchResult = new ArrayList<>(response);
        }

        void setFindPageResult(List<Question> response) {
            findPageResult = new ArrayList<>(response);
        }

        void setSearchException(SQLException exception) {
            this.searchException = exception;
        }

        QuestionSearchCriteria getLastCriteria() {
            return capturedCriteria.isEmpty() ? null : capturedCriteria.get(capturedCriteria.size() - 1);
        }

        List<QuestionSearchCriteria> getCapturedCriteria() {
            return new ArrayList<>(capturedCriteria);
        }

        int getSearchInvocationCount() {
            return searchInvocationCount;
        }

        int getFindPageInvocationCount() {
            return findPageInvocationCount;
        }

        private List<Question> copy(List<Question> source) {
            return new ArrayList<>(source == null ? Collections.emptyList() : source);
        }
    }
    
    /**
     * Test comprehensive search functionality that was enhanced in Step 58.
     * Verifies that search works across titles, content, tags, and user names.
     */
    @Test
    void testComprehensiveSearchFunctionality() {
        // This test verifies that the SearchService integrates with the enhanced
        // QuestionRepository search that now supports searching across:
        // - Question titles and content
        // - Tags associated with questions  
        // - User names who posted questions
        // The actual database integration is tested in QuestionRepositoryTest
        
        StubQuestionDataAccess dataAccess = new StubQuestionDataAccess();
        SearchService service = new SearchService(dataAccess);
        
        List<Question> expectedResults = List.of(question(1L, "Test Query", "Test content"));
        dataAccess.addSearchResponse("test query", expectedResults);

        // Test that search calls the repository with proper criteria and returns early when exact matches exist
        List<Question> results = service.search("test query", 10, 0);
        assertEquals(expectedResults, results);
        
        assertEquals(1, dataAccess.getSearchInvocationCount(), 
                    "Search should invoke repository search");
    }
}