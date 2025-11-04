package com.upnext.app.ui.screens;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.upnext.app.App;
import com.upnext.app.config.DatabaseConfig;
import com.upnext.app.data.JdbcConnectionProvider;
import com.upnext.app.data.UserRepository;
import com.upnext.app.data.question.AnswerRepository;
import com.upnext.app.data.question.QuestionRepository;
import com.upnext.app.data.question.SubjectRepository;
import com.upnext.app.domain.User;
import com.upnext.app.domain.question.Answer;
import com.upnext.app.domain.question.Question;
import com.upnext.app.domain.question.Subject;
import com.upnext.app.service.AuthService;
import com.upnext.app.ui.components.AnswerInputPanel;
import com.upnext.app.ui.components.VotePanel;
import com.upnext.app.ui.navigation.ViewNavigator;

/**
 * UI integration tests covering QuestionDetailScreen behaviours as part of Step 64.
 */
class QuestionDetailScreenIntegrationTest {

    private static final String[] CLEANUP_STATEMENTS = new String[] {
        "DELETE FROM answer_votes",
        "DELETE FROM question_tags",
        "DELETE FROM tags",
        "DELETE FROM answers",
        "DELETE FROM questions",
        "DELETE FROM subjects WHERE name LIKE 'UI Integration Test%'",
        "DELETE FROM users WHERE email LIKE 'ui_integration_test_%@example.com'",
        "ALTER TABLE answers AUTO_INCREMENT = 1",
        "ALTER TABLE questions AUTO_INCREMENT = 1",
        "ALTER TABLE subjects AUTO_INCREMENT = 1",
        "ALTER TABLE tags AUTO_INCREMENT = 1"
    };

    private QuestionRepository questionRepository;
    private AnswerRepository answerRepository;
    private UserRepository userRepository;
    private SubjectRepository subjectRepository;
    private AuthService authService;

    private QuestionDetailScreen screen;
    private Question baselineQuestion;
    private Answer baselineAnswer;
    private User questionOwner;
    private User answerAuthor;

    @BeforeAll
    static void enableHeadlessMode() {
        System.setProperty("java.awt.headless", "true");
        DatabaseConfig.initialize();
    }

    @BeforeEach
    void setUp() throws Exception {
        questionRepository = QuestionRepository.getInstance();
        answerRepository = AnswerRepository.getInstance();
        userRepository = UserRepository.getInstance();
        subjectRepository = SubjectRepository.getInstance();
        authService = AuthService.getInstance();

        cleanupDatabase();

        questionOwner = createTestUser("owner");
        answerAuthor = createTestUser("answer-author");

        Subject subject = createSubject();

        Question question = new Question();
        question.setUserId(questionOwner.getId());
        question.setTitle("How can I validate responsive Swing layouts?");
        question.setContent("Looking for reliable ways to exercise breakpoints inside QuestionDetailScreen during automated tests.");
        question.setContext("Step 64 integration coverage");
        question.setSubjectId(subject.getId());
        baselineQuestion = questionRepository.saveWithTags(question, List.of("testing", "swing"));

        // Persist an initial answer for baseline UI assertions
        Answer seedAnswer = new Answer();
        seedAnswer.setQuestionId(baselineQuestion.getId());
        seedAnswer.setUserId(answerAuthor.getId());
        seedAnswer.setUserName(answerAuthor.getName());
        seedAnswer.setContent("You can resize the component and inspect the resulting layout manager instances.");
        baselineAnswer = questionRepository.saveAnswer(seedAnswer);
        answerRepository.voteAnswer(baselineAnswer.getId(), questionOwner.getId(), true);

        screen = new QuestionDetailScreen();
        initialiseNavigator();

        AtomicBoolean loaded = new AtomicBoolean(false);
        runOnEdt(() -> loaded.set(screen.loadQuestion(baselineQuestion.getId())));
        assertTrue(loaded.get(), "Question should load successfully for integration tests");
    }

    @AfterEach
    void tearDown() throws Exception {
        clearAuthenticatedUser();
        cleanupDatabase();
    }

    @Test
    @DisplayName("Loads question details with answers and related sections populated")
    void shouldLoadQuestionAndRenderPrimarySections() throws Exception {
        JLabel titleLabel = getPrivateField(screen, "titleLabel", JLabel.class);
        JLabel answersHeaderLabel = getPrivateField(screen, "answersHeaderLabel", JLabel.class);
        JPanel answersPanel = getPrivateField(screen, "answersPanel", JPanel.class);

        assertEquals(baselineQuestion.getTitle(), runOnEdt(titleLabel::getText));
        assertTrue(runOnEdt(() -> answersHeaderLabel.getText().contains("Answer")));

        int answerCardCount = runOnEdt(() -> countAnswerCards(answersPanel));
        assertTrue(answerCardCount >= 1, "Expected at least one rendered answer card");

        VotePanel questionVotePanel = getPrivateField(screen, "questionVotePanel", VotePanel.class);
        assertEquals(0, runOnEdt(questionVotePanel::getVoteCount));
        assertEquals(baselineQuestion.getId(), runOnEdt(questionVotePanel::getItemId));
    }

    @Test
    @DisplayName("Responsive layout switches between mobile and desktop breakpoints")
    void shouldToggleResponsiveLayoutBetweenMobileAndDesktop() throws Exception {
        JPanel mainContentPanel = getPrivateField(screen, "mainContentPanel", JPanel.class);
        Method adjustLayout = QuestionDetailScreen.class.getDeclaredMethod("adjustLayoutForScreenSize");
        adjustLayout.setAccessible(true);

        runOnEdt(() -> {
            screen.setSize(640, 900);
            try {
                adjustLayout.invoke(screen);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        assertTrue(runOnEdt(() -> mainContentPanel.getLayout() instanceof javax.swing.BoxLayout));

        runOnEdt(() -> {
            screen.setSize(1400, 900);
            try {
                adjustLayout.invoke(screen);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        assertTrue(runOnEdt(() -> mainContentPanel.getLayout() instanceof BorderLayout));
    }

    @Test
    @DisplayName("Authenticated users can submit new answers and refresh the list")
    void shouldAllowAuthenticatedUserToSubmitAnswer() throws Exception {
        User responder = createTestUser("responder");
        setAuthenticatedUser(responder);

        AnswerInputPanel inputPanel = getPrivateField(screen, "answerInputPanel", AnswerInputPanel.class);
        JTextArea answerArea = getPrivateField(inputPanel, "answerTextArea", JTextArea.class);
        JButton submitButton = getPrivateField(inputPanel, "submitButton", JButton.class);
        JLabel answersHeader = getPrivateField(screen, "answersHeaderLabel", JLabel.class);
        JPanel answersPanel = getPrivateField(screen, "answersPanel", JPanel.class);

        Method handleNewAnswer = QuestionDetailScreen.class.getDeclaredMethod("handleNewAnswer", com.upnext.app.domain.question.Answer.class);
        handleNewAnswer.setAccessible(true);

        CountDownLatch latch = new CountDownLatch(1);
        runOnEdt(() -> inputPanel.setOnAnswerSubmitted(answer -> {
            try {
                handleNewAnswer.invoke(screen, answer);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                latch.countDown();
            }
        }));

        runOnEdt(() -> {
            answerArea.setText("This is a brand new integration test answer authored during Step 64 coverage.");
            answerArea.setForeground(Color.BLACK);
        });

    waitUntil(() -> runOnEdt(submitButton::isEnabled), Duration.ofSeconds(2));

    runOnEdt(() -> submitButton.doClick());
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Answer submission callback should complete");

        assertTrue(runOnEdt(() -> answersHeader.getText().startsWith("2")), "Answer header should reflect new answer count");
        assertTrue(runOnEdt(() -> containsAnswerText(answersPanel, "integration test answer authored")),
            "Answers panel should display the newly submitted answer content");
    }

    @Test
    @DisplayName("Voting on an answer updates the displayed score")
    void shouldProcessAnswerUpvoteAndRefreshVoteCount() throws Exception {
        User voter = createTestUser("voter");
        setAuthenticatedUser(voter);

        JPanel answersPanel = getPrivateField(screen, "answersPanel", JPanel.class);
        VotePanel votePanel = runOnEdt(() -> locateFirstAnswerVotePanel(answersPanel));
        assertNotNull(votePanel, "Expected vote panel on the first answer card");

        int initialVotes = runOnEdt(votePanel::getVoteCount);
        JButton upvoteButton = getPrivateField(votePanel, "upvoteButton", JButton.class);

        runOnEdt(() -> upvoteButton.doClick());

        waitUntil(() -> runOnEdt(() -> locateFirstAnswerVotePanel(answersPanel).getVoteCount() == initialVotes + 1),
            Duration.ofSeconds(2));
    }

    @Test
    @DisplayName("Back navigation returns to the registered home screen")
    void shouldNavigateBackToHomeWhenBackButtonClicked() throws Exception {
        JButton backButton = getPrivateField(screen, "backButton", JButton.class);
        ViewNavigator navigator = ViewNavigator.getInstance();

        runOnEdt(() -> {
            navigator.navigateTo(App.QUESTION_DETAIL_SCREEN);
            backButton.doClick();
        });

        assertEquals(App.HOME_SCREEN, navigator.getCurrentScreen());
    }

    // ===== Helper Methods =====

    private void initialiseNavigator() {
        JPanel dummyContainer = new JPanel();
        ViewNavigator navigator;
        try {
            navigator = ViewNavigator.initialize(dummyContainer);
        } catch (IllegalStateException alreadyInitialised) {
            navigator = ViewNavigator.getInstance();
        }
        navigator.registerScreen(App.HOME_SCREEN, new JPanel());
        navigator.registerScreen(App.QUESTION_DETAIL_SCREEN, screen);
    }

    private void cleanupDatabase() throws SQLException {
        try (Connection connection = JdbcConnectionProvider.getInstance().getConnection();
             Statement statement = connection.createStatement()) {
            for (String sql : CLEANUP_STATEMENTS) {
                statement.execute(sql);
            }
        }
    }

    private User createTestUser(String suffix) throws SQLException {
        User user = new User();
        user.setName("UI Test " + suffix);
        user.setEmail("ui_integration_test_" + suffix + "_" + System.nanoTime() + "@example.com");
        user.setPasswordHash("hash");
        user.setSalt("salt");
        user.setActive(true);
        user.setSkills(new ArrayList<>());
        return userRepository.save(user);
    }

    private Subject createSubject() throws SQLException {
        Subject subject = new Subject();
        subject.setName("UI Integration Test Subject");
        subject.setDescription("Subject created for QuestionDetailScreen integration verification");
        return subjectRepository.save(subject);
    }

    private void setAuthenticatedUser(User user) throws Exception {
        Field currentUserField = AuthService.class.getDeclaredField("currentUser");
        currentUserField.setAccessible(true);
        currentUserField.set(authService, user);
    }

    private void clearAuthenticatedUser() throws Exception {
        setAuthenticatedUser(null);
    }

    private int countAnswerCards(JPanel answersPanel) {
        int cards = 0;
        for (Component component : answersPanel.getComponents()) {
            if (component instanceof JPanel panel && panel.getLayout() instanceof BorderLayout) {
                cards++;
            }
        }
        return cards;
    }

    private boolean containsAnswerText(JPanel answersPanel, String snippet) {
        for (Component component : answersPanel.getComponents()) {
            if (component instanceof JPanel panel && panel.getLayout() instanceof BorderLayout) {
                Component center = ((BorderLayout) panel.getLayout()).getLayoutComponent(BorderLayout.CENTER);
                if (center instanceof JPanel contentPanel) {
                    for (Component inner : contentPanel.getComponents()) {
                        if (inner instanceof JTextArea textArea && textArea.getText().contains(snippet)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private VotePanel locateFirstAnswerVotePanel(JPanel answersPanel) {
        for (Component component : answersPanel.getComponents()) {
            if (component instanceof JPanel panel && panel.getLayout() instanceof BorderLayout) {
                Component west = ((BorderLayout) panel.getLayout()).getLayoutComponent(BorderLayout.WEST);
                if (west instanceof VotePanel votePanel) {
                    return votePanel;
                }
            }
        }
        return null;
    }

    private void waitUntil(CheckedBooleanSupplier condition, Duration timeout) throws Exception {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (!condition.getAsBoolean()) {
            if (System.nanoTime() > deadline) {
                throw new AssertionError("Condition was not met within " + timeout.toMillis() + " ms");
            }
            Thread.sleep(25);
        }
    }

    private <T> T runOnEdt(Callable<T> task) throws Exception {
        if (SwingUtilities.isEventDispatchThread()) {
            return task.call();
        }
        FutureTask<T> future = new FutureTask<>(task);
        SwingUtilities.invokeAndWait(future);
        return future.get();
    }

    private void runOnEdt(Runnable runnable) throws Exception {
        runOnEdt(() -> {
            runnable.run();
            return null;
        });
    }

    private <T> T getPrivateField(Object target, String name, Class<T> type) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        Object value = field.get(target);
        return type.cast(value);
    }

    @FunctionalInterface
    private interface CheckedBooleanSupplier {
        boolean getAsBoolean() throws Exception;
    }
}
