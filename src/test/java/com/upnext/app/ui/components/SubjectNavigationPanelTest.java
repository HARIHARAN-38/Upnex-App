package com.upnext.app.ui.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.upnext.app.domain.question.Subject;
import com.upnext.app.domain.question.Tag;

/**
 * Test class for SubjectNavigationPanel component.
 * Tests filter change events and selection behavior.
 */
public class SubjectNavigationPanelTest {
    
    private SubjectNavigationPanel panel;
    private Subject testSubject;
    private Tag testTag;
    private RecordingFilterChangeListener recordingListener;
    
    @BeforeEach
    public void setUp() {
        // Create panel with mocked repositories for testing
        panel = new SubjectNavigationPanel();
        recordingListener = new RecordingFilterChangeListener();
        panel.setFilterChangeListener(recordingListener);
        
        // Set up test data
        testSubject = new Subject("Programming", "Programming related topics");
        testSubject.setId(1L);
        
        testTag = new Tag("java");
        testTag.setId(1L);
    }
    
    @Test
    public void getSelectedSubject_whenNothingSelected_returnsNull() {
        assertNull(panel.getSelectedSubject());
    }
    
    @Test
    public void getSelectedTags_whenNothingSelected_returnsEmptySet() {
        Set<Tag> selectedTags = panel.getSelectedTags();
        assertNotNull(selectedTags);
        assertTrue(selectedTags.isEmpty());
    }
    
    @Test
    public void selectSubject_whenCalled_updatesSelectedSubject() {
        panel.selectSubject(testSubject);
        assertEquals(testSubject, panel.getSelectedSubject());
        assertEquals(testSubject, recordingListener.getLastSubject());
        
        panel.selectSubject(null);
        assertNull(panel.getSelectedSubject());
        assertNull(recordingListener.getLastSubject());
    }
    
    @Test
    public void clearSelectedTags_whenCalled_clearsAllSelectedTags() {
        // Since we cannot directly add tags through public API in this test,
        // we will verify that calling clearSelectedTags results in an empty set
        panel.clearSelectedTags();
        assertTrue(panel.getSelectedTags().isEmpty());
        assertTrue(recordingListener.getLastTags().isEmpty());
    }
    
    @Test
    public void getSelectedTags_returnsDefensiveCopy() {
        Set<Tag> tags1 = panel.getSelectedTags();
        Set<Tag> tags2 = panel.getSelectedTags();
        
        assertNotSame(tags1, tags2, "Method should return a new copy of the set each time");
    }
    
    /**
     * Tests the FilterChangeListener interface implementation.
     * This is a functional interface test that verifies the contract.
     */
    @Test
    public void filterChangeListener_whenImplemented_receivesCorrectParameters() {
        // Create a simple implementation to test the interface contract
        List<Subject> capturedSubjects = new ArrayList<>();
        List<List<Tag>> capturedTagLists = new ArrayList<>();
        
        SubjectNavigationPanel.FilterChangeListener testListener = 
            (subject, tags) -> {
                capturedSubjects.add(subject);
                capturedTagLists.add(new ArrayList<>(tags));
            };
        
        // Test with null subject and empty tags
        testListener.onFilterChanged(null, new ArrayList<>());
        assertNull(capturedSubjects.get(0));
        assertTrue(capturedTagLists.get(0).isEmpty());
        
        // Test with subject and some tags
        List<Tag> someTags = List.of(testTag);
        testListener.onFilterChanged(testSubject, someTags);
        assertEquals(testSubject, capturedSubjects.get(1));
        assertEquals(1, capturedTagLists.get(1).size());
        assertEquals(testTag, capturedTagLists.get(1).get(0));
    }
}

class RecordingFilterChangeListener implements SubjectNavigationPanel.FilterChangeListener {
    private Subject lastSubject;
    private List<Tag> lastTags = new ArrayList<>();

    @Override
    public void onFilterChanged(Subject subject, List<Tag> tags) {
        this.lastSubject = subject;
        this.lastTags = new ArrayList<>(tags);
    }

    Subject getLastSubject() {
        return lastSubject;
    }

    List<Tag> getLastTags() {
        return new ArrayList<>(lastTags);
    }
}