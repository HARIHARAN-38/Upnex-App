package com.upnext.app.ui.components;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;

import com.upnext.app.data.question.SubjectRepository;
import com.upnext.app.domain.question.Subject;

/**
 * Test to verify SubjectComboBox can load subjects properly
 */
public class SubjectComboBoxTest {

    @Test
    public void testSubjectRepositoryCanLoadSubjects() {
        try {
            // Get SubjectRepository instance
            SubjectRepository subjectRepository = SubjectRepository.getInstance();
            
            // Load all subjects
            List<Subject> subjects = subjectRepository.findAll();
            
            // Verify subjects are loaded
            assertNotNull(subjects, "Subjects list should not be null");
            assertTrue(!subjects.isEmpty(), "Should have at least one subject");
            
            // Print subjects for debugging
            System.out.println("Found " + subjects.size() + " subjects:");
            for (Subject subject : subjects) {
                System.out.println("  ID: " + subject.getId() + ", Name: " + subject.getName());
            }
            
            // Look for some expected subjects
            assertTrue(subjects.stream().anyMatch(s -> s.getName().equals("General")), 
                       "Should have General subject");
            assertTrue(subjects.stream().anyMatch(s -> s.getName().contains("Programming")), 
                       "Should have a Programming-related subject");
        } catch (Exception e) {
            fail("Test failed with exception: " + e.getMessage());
        }
    }
}