package com.upnext.app.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.upnext.app.domain.Skill;
import com.upnext.app.service.SkillService.SkillDataAccess;
import com.upnext.app.service.SkillService.SkillException;

/**
 * Unit tests for {@link SkillService} that exercise validation logic and data operations
 * using an in-memory implementation of {@link SkillDataAccess}.
 */
class SkillServiceTest {

    private SkillService skillService;
    private InMemorySkillDataAccess dataAccess;

    @BeforeEach
    void setUp() {
        dataAccess = new InMemorySkillDataAccess();
        skillService = new SkillService(dataAccess);
    }

    @Test
    void addSkill_withValidInput_persistsAndReturnsSkill() throws SkillException {
        Skill saved = skillService.addSkill(1L, "Java", "Core Java", 7);

        assertTrue(saved.getSkillId() != null && saved.getSkillId() > 0);
        assertEquals("Java", saved.getSkillName());
        assertEquals(7, saved.getProficiencyLevel());
        assertEquals(1, dataAccess.countForUser(1L));
    }

    @Test
    void addSkill_withNullUser_throwsException() {
        SkillException exception = assertThrows(SkillException.class,
            () -> skillService.addSkill(null, "Java", "Core", 5));
        assertEquals("User ID cannot be null", exception.getMessage());
    }

    @Test
    void addSkill_withOutOfRangeProficiency_clampsValue() throws SkillException {
        Skill low = skillService.addSkill(2L, "Scala", "FP", -3);
        Skill high = skillService.addSkill(2L, "Kotlin", "Android", 42);

        assertEquals(1, low.getProficiencyLevel());
        assertEquals(10, high.getProficiencyLevel());
    }

    @Test
    void getUserSkills_returnsOnlyRequestedUsersSkills() throws SkillException {
        skillService.addSkill(1L, "Java", "", 5);
        skillService.addSkill(1L, "SQL", "", 6);
        skillService.addSkill(2L, "Python", "", 4);

        List<Skill> user1Skills = skillService.getUserSkills(1L);
        assertEquals(2, user1Skills.size());
        assertTrue(user1Skills.stream().allMatch(skill -> skill.getUserId().equals(1L)));
    }

    @Test
    void updateSkill_whenSkillExists_updatesFields() throws SkillException {
        Skill saved = skillService.addSkill(1L, "Java", "", 5);

        Skill updated = skillService.updateSkill(saved.getSkillId(),
            "Advanced Java", "Spring", 9);

        assertEquals("Advanced Java", updated.getSkillName());
        assertEquals("Spring", updated.getDescription());
        assertEquals(9, updated.getProficiencyLevel());
    }

    @Test
    void updateSkill_whenSkillMissing_throws() {
        SkillException exception = assertThrows(SkillException.class,
            () -> skillService.updateSkill(999L, "Name", "Desc", 5));
        assertEquals("Skill not found", exception.getMessage());
    }

    @Test
    void updateSkillProficiency_onlyChangesLevel() throws SkillException {
        Skill saved = skillService.addSkill(1L, "Java", "Core", 5);

        Skill updated = skillService.updateSkillProficiency(saved.getSkillId(), 8);

        assertEquals(8, updated.getProficiencyLevel());
        assertEquals("Java", updated.getSkillName());
        assertEquals("Core", updated.getDescription());
    }

    @Test
    void deleteSkill_removesSkill() throws SkillException {
        Skill saved = skillService.addSkill(1L, "Java", "", 5);

        skillService.deleteSkill(saved.getSkillId());

        assertEquals(0, dataAccess.countForUser(1L));
        SkillException exception = assertThrows(SkillException.class,
            () -> skillService.getSkill(saved.getSkillId()));
        assertEquals("Skill not found", exception.getMessage());
    }

    @Test
    void deleteUserSkills_removesAllUserSkills() throws SkillException {
        skillService.addSkill(1L, "Java", "", 5);
        skillService.addSkill(1L, "SQL", "", 6);

        int removed = skillService.deleteUserSkills(1L);

        assertEquals(2, removed);
        assertTrue(skillService.getUserSkills(1L).isEmpty());
    }

    @Test
    void addSkills_bulkInsertionDelegatesToAddSkill() throws SkillException {
        List<Skill> newSkills = new ArrayList<>();
        newSkills.add(skill("Docker", "Containers", 6));
        newSkills.add(skill("Kubernetes", "Orchestration", 7));

        List<Skill> saved = skillService.addSkills(10L, newSkills);

        assertEquals(2, saved.size());
        assertEquals(2, dataAccess.countForUser(10L));
    }

    private Skill skill(String name, String description, int proficiency) {
        Skill skill = new Skill();
        skill.setSkillName(name);
        skill.setDescription(description);
        skill.setProficiencyLevel(proficiency);
        return skill;
    }

    private static final class InMemorySkillDataAccess implements SkillDataAccess {
        private final Map<Long, Skill> store = new HashMap<>();
        private final AtomicLong idSequence = new AtomicLong(1);

        @Override
        public Skill save(Skill skill) {
            long id = idSequence.getAndIncrement();
            Skill stored = copy(skill);
            stored.setSkillId(id);
            stored.setCreatedAt(Instant.now().toString());
            stored.setUpdatedAt(stored.getCreatedAt());
            store.put(id, stored);

            return copy(stored);
        }

        @Override
        public List<Skill> findByUserId(Long userId) {
            List<Skill> result = new ArrayList<>();
            for (Skill skill : store.values()) {
                if (skill.getUserId().equals(userId)) {
                    result.add(copy(skill));
                }
            }
            return result;
        }

        @Override
        public Optional<Skill> findById(Long skillId) {
            Skill skill = store.get(skillId);
            return Optional.ofNullable(skill == null ? null : copy(skill));
        }

        @Override
        public boolean update(Skill skill) {
            if (!store.containsKey(skill.getSkillId())) {
                return false;
            }
            Skill stored = copy(skill);
            stored.setUpdatedAt(Instant.now().toString());
            store.put(skill.getSkillId(), stored);
            return true;
        }

        @Override
        public boolean deleteById(Long skillId) {
            return store.remove(skillId) != null;
        }

        @Override
        public int deleteByUserId(Long userId) {
            List<Long> idsToRemove = new ArrayList<>();
            for (Map.Entry<Long, Skill> entry : store.entrySet()) {
                if (entry.getValue().getUserId().equals(userId)) {
                    idsToRemove.add(entry.getKey());
                }
            }
            idsToRemove.forEach(store::remove);
            return idsToRemove.size();
        }

        int countForUser(Long userId) {
            int count = 0;
            for (Skill skill : store.values()) {
                if (skill.getUserId().equals(userId)) {
                    count++;
                }
            }
            return count;
        }

        private Skill copy(Skill source) {
            Skill copy = new Skill();
            copy.setSkillId(source.getSkillId());
            copy.setUserId(source.getUserId());
            copy.setSkillName(source.getSkillName());
            copy.setDescription(source.getDescription());
            copy.setProficiencyLevel(source.getProficiencyLevel());
            copy.setCreatedAt(source.getCreatedAt());
            copy.setUpdatedAt(source.getUpdatedAt());
            return copy;
        }
    }
}