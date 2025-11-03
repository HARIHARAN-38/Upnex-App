- [x] Step 1: Finalise UI theme
  - Apply `AppTheme.apply()` within `App.java` and confirm colour, typography, and component defaults
  - Export palette reference sheet in `docs/AppThemePalette.md`
- [x] Step 2: Scaffold authentication screens
  - Create `src/main/java/com/upnext/app/ui/screens/CreateAccountScreen.java`
  - Create `src/main/java/com/upnext/app/ui/screens/SignInScreen.java`
  - Share reusable input components in `src/main/java/com/upnext/app/ui/components/forms`
- [x] Step 3: Navigation wiring
  - Implement `src/main/java/com/upnext/app/ui/navigation/ViewNavigator.java` to swap panels inside the main frame
  - Hook Sign In → Home routing once authentication succeeds
- [x] Step 4: JDBC connector setup
  - Place JDBC driver JAR under `lib/`
  - Configure build tool classpath (e.g., Maven `pom.xml`)
  - Add `src/main/java/com/upnext/app/config/DatabaseConfig.java` to load connection parameters
- [x] Step 5: Database connection utility
  - Implement `src/main/java/com/upnext/app/data/JdbcConnectionProvider.java` using credentials: db `upnex`, user `root`, password `hari`, host `127.0.0.1`
  - Add connection health check test under `src/test/java/com/upnext/app/data/JdbcConnectionProviderTest.java`
- [x] Step 6: Authentication service
  - Add `src/main/java/com/upnext/app/domain/User.java`
  - Implement `src/main/java/com/upnext/app/data/UserRepository.java` for account persistence
  - Build `src/main/java/com/upnext/app/service/AuthService.java` to encapsulate sign-up/sign-in logic
- [x] Step 7: Wire UI to services
  - Connect `CreateAccountScreen` actions to `AuthService.signUp`
  - Connect `SignInScreen` actions to `AuthService.signIn`
  - On successful sign-in navigate to placeholder Home screen (`src/main/java/com/upnext/app/ui/screens/HomeScreen.java`)
- [x] Step 8: Home screen placeholder
  - Implement `HomeScreen` layout showing dashboard widgets and welcome message
  - Add TODOs for future dashboard widgets
  - Add sign-out functionality
- [x] Step 9: Error handling & feedback
  - Created `FeedbackManager` for consistent dialog display and messaging
  - Implemented robust `Logger` class for application-wide logging
  - Enhanced error handling in authentication flows
- [x] Step 10: Documentation & QA
  - Created comprehensive `README.md` with setup instructions
  - Developed detailed test plan in `docs/TestPlan.md` with test cases
  - Added `run.bat` script for easy application startup
  - Created database schema initialization script

# New Skills Feature Implementation Roadmap

- [x] Step 11: Database Schema Update
  - Update `src/main/resources/db/schema.sql` to add skills table with columns:
    - skill_id (PRIMARY KEY)
    - user_id (FOREIGN KEY referencing users table)
    - skill_name (VARCHAR)
    - description (VARCHAR)
    - proficiency_level (INTEGER)
  - Update `SchemaInitializer.java` to ensure new tables are created

- [x] Step 12: Domain Model Updates
  - Create `src/main/java/com/upnext/app/domain/Skill.java` with properties:
    - id, userId, name, description, proficiencyLevel
    - Include proper getters and setters
  - Update `User.java` to include skills list if needed

- [x] Step 13: Data Access Layer
  - Create `src/main/java/com/upnext/app/data/SkillRepository.java` with methods:
    - save(Skill): add new skill to database
    - findByUserId(int): retrieve user's skills
    - deleteById(int): remove skill
  - Add tests in `src/test/java/com/upnext/app/data/SkillRepositoryTest.java`

- [x] Step 14: Service Layer
  - Create `src/main/java/com/upnext/app/service/SkillService.java` with methods:
    - addSkill(Skill): validate and persist skill
    - getUserSkills(int): get skills for a user
    - deleteSkill(int): remove a skill
    - updateSkillProficiency(int, int): update proficiency level
  - Update `AuthService.java` to handle skill creation during registration flow

- [x] Step 15: UI Components for Skills
  - Create `src/main/java/com/upnext/app/ui/components/SkillCard.java`:
    - Display skill name, description, and proficiency bar
    - Include delete button with 'X' icon
  - Create `src/main/java/com/upnext/app/ui/components/ProficiencyBar.java`:
    - Visual component to display and edit skill proficiency
  - Create `src/main/java/com/upnext/app/ui/components/SkillCardPanel.java`:
    - Utility component to manage multiple skill cards

- [x] Step 16: SkillSet Screen
  - Create `src/main/java/com/upnext/app/ui/screens/SkillsetScreen.java`:
    - Container panel for displaying list of SkillCard components
    - Add "Add New Skill" button
    - Add "Back" button to return to CreateAccountScreen
    - Add "Create Account" button to finalize registration
    - Logic to handle adding/removing SkillCards
    - Reference UI design from `UI/SkillSet.png`

- [x] Step 17: SkillAdd Screen
  - Create `src/main/java/com/upnext/app/ui/screens/SkillAddScreen.java`:
    - Form fields for skill name and description
    - ProficiencyBar component for setting skill level
    - "Add" button to save and return to SkillsetScreen
    - Reference UI design from `UI/SkillAdd.png`

- [x] Step 18: Navigation Flow Update
  - Update `ViewNavigator.java` to support navigation between:
    - CreateAccountScreen → SkillsetScreen
    - SkillsetScreen → CreateAccountScreen (back)
    - SkillsetScreen → SkillAddScreen
    - SkillAddScreen → SkillsetScreen
    - SkillsetScreen → HomeScreen (on final submission)

- [x] Step 19: Registration Flow Integration
  - Update `CreateAccountScreen.java` to navigate to SkillsetScreen after initial info
  - Modify registration logic to collect skills before final account creation
  - Ensure proper validation at each step
  - Update `UserRepository.java` to handle storing user with associated skills

- [x] Step 20: Testing & Refinement
  - Create unit tests for all new components
  - Update `docs/TestPlan.md` to include tests for skills functionality
  - Perform end-to-end testing of full registration flow
  - Ensure error handling works properly at all steps

- [x] Step 21: UI Refinement and Responsiveness
  - Ensure consistent styling across all new screens
  - Verify layout adjusts properly for different window sizes
  - Apply proper spacing, alignment and visual hierarchy
  - Add loading indicators where appropriate

- [x] Step 22: Documentation Update (Completed Oct 16, 2025)
  - Update `README.md` with comprehensive feature descriptions
  - Add detailed code comments with architectural patterns explanations
  - Enhance method documentation with step-by-step process descriptions
  - Create user guide for the skills registration process
  - Document UI/UX patterns and design decisions

# Home Experience & Search Roadmap

- [x] Step 23: Schema & Metrics Update (Completed Oct 17, 2025)
  - Extend `src/main/resources/db/schema.sql` with question tables and user metric columns (questions_asked, answers_given, total_upvotes)
  - Add migration notes to `docs/DatabaseAccess.md`
  - Refresh `SchemaInitializer.java` fixtures if needed

- [x] Step 24: Repository Foundations (Completed Oct 16, 2025)
  - Scaffold domain model `src/main/java/com/upnext/app/domain/question/Question.java`
  - Implement CRUD/filter stubs in `src/main/java/com/upnext/app/data/question/QuestionRepository.java`
  - Add integration smoke test `src/test/java/com/upnext/app/data/question/QuestionRepositoryTest.java`

- [x] Step 25: JDBC Alignment & Docs
  - Reconfirm `JdbcConnectionProvider` credentials (`db=upnex`, `user=root`, `password=hari`, host `127.0.0.1`)
  - Document connection usage patterns in `docs/DatabaseAccess.md`
  - Ensure MySQL driver JAR in `lib/` is referenced by `pom.xml`

- [x] Step 26: Search Token Utilities (Completed Oct 17, 2025)
  - Implement tokenizer helper `src/main/java/com/upnext/app/service/search/TokenUtils.java`
  - Support lowercasing, dedupe, trigram preparation
  - Cover edge cases in `src/test/java/com/upnext/app/service/search/TokenUtilsTest.java`

- [x] Step 27: Search Service Core (Completed Oct 18, 2025)
  - Build `src/main/java/com/upnext/app/service/SearchService.java` using token utilities + repository queries
  - Implement fuzzy matching (LIKE / trigram fallback) and result ranking
  - Add tests in `src/test/java/com/upnext/app/service/SearchServiceTest.java`

- [x] Step 28: Home Layout Scaffold (Completed Oct 17, 2025)
  - Restructure `HomeScreen` with three-column layout skeleton respecting `AppTheme`
  - Introduce layout constants for spacing and breakpoints

- [x] Step 29: Subject Navigation Panel (Completed Oct 17, 2025)
  - Implement `SubjectNavigationPanel` with single-select subjects and multi-select trending tags
  - Expose filter change events via listener interface
  - Add panel behaviour tests/mocks

- [x] Step 30: Question Feed & Cards (Completed Oct 18, 2025)
  - Create `QuestionCard` component (vote controls, metadata badges, navigation)
  - Implement `QuestionFeedPanel` rendering list, toolbar chips (Hot/New/Unanswered/Solved), empty states
  - Wire vote actions to repository/service stubs

- [x] Step 31: Profile Summary & Metrics (Completed Oct 17, 2025)
  - Build `ProfileSummaryCard` showing avatar, username, metrics, member-since
  - Bind data via `AuthService`/`UserRepository` metrics
  - Handle loading/placeholder states

- [x] Step 32: Hero Bar & Search Wiring (2023-07-05)
  - Create `HeroBar` (logo, search input, avatar button)
  - Connect search input to `SearchService` with debounced updates
  - Route avatar/menu button to profile screen via `ViewNavigator`

- [x] Step 33: Filter Integration Pass (2025-10-17)
  - Combine subject/tag selections, toolbar chips, and search into unified query model
  - Ensure feed refresh handles simultaneous filters and empty results gracefully
  - Persist active filter state in session/storage

- [x] Step 34: Navigation Persistence & QA (Completed Oct 17, 2025)
  - Ensure question card navigation to detail page and back retains filters
  - Confirm "Ask a Question" CTA routes to post page; avatar to profile
  - Add navigation tests `src/test/java/com/upnext/app/ui/navigation/HomeNavigationTest.java`

- [x] Step 35: Documentation & End-to-End QA (Completed Oct 17, 2025)
  - Update `docs/TestPlan.md` with new home/search cases
  - Create `docs/HomeScreenSpec.md` detailing UX flow and component interactions
  - Created `docs/End-to-End_Validation.md` with comprehensive validation plan
  - Perform manual + automated end-to-end validation for search, filters, navigation, metrics

# Add Question Feature Roadmap

- [x] Step 36: Schema Alignment & Migration Prep (Completed Oct 26, 2025)
  - Update `src/main/resources/db/schema.sql` to include nullable `context` column on `questions`, ensure `tags` and `question_tags` definitions match AppTheme requirements
  - Create idempotent migration `sql/008_add_question_context_and_constraints.sql` (guarded `ALTER`/`CREATE` statements) to sync existing databases
  - Adjust `src/main/java/com/upnext/app/data/SchemaInitializer.java` to execute the new migration and verify `users.id`/`questions.user_id` types align (BIGINT consistency)
- [x] Step 37: Domain & DTO Enhancements (Completed Oct 26, 2025)
  - Extended `src/main/java/com/upnext/app/domain/question/Question.java` with `context` field, proper getters/setters, and updated constructors
  - Created lightweight `src/main/java/com/upnext/app/domain/tag/Tag.java` domain class with id, name, usageCount fields and proper lifecycle methods
  - Updated QuestionRepository mappers to handle context field in INSERT, UPDATE, and SELECT operations with backward compatibility
- [x] Step 38: Repository Transaction Support
  - Implemented transactional `saveWithTags` in `src/main/java/com/upnext/app/data/question/QuestionRepository.java` (insert question → upsert tags → populate `question_tags`), handling generated keys and duplicate tag detection
  - Enhanced existing `src/main/java/com/upnext/app/data/question/TagRepository.java` with better transaction support and tag management
  - Created comprehensive integration test `src/test/java/com/upnext/app/data/question/QuestionRepositoryAddTest.java` covering happy path, duplicate tag handling, validation edge cases, and transactional rollback scenarios
- [x] Step 39: Service Layer & Validation
  - Created comprehensive `src/main/java/com/upnext/app/service/QuestionService.java` with validation (title required, max lengths, ≤10 tags) and delegation to repository + `AuthService` for current user lookup
  - Implemented robust error handling contracts with `QuestionException` checked exception for UI inline messaging, and proper SQL exception propagation for transaction rollback scenarios
  - Added unit tests in `src/test/java/com/upnext/app/service/QuestionServiceTest.java` covering validation rules, error handling, and authentication requirements
- [x] Step 40: Add Question UI Screen
  - Built comprehensive `src/main/java/com/upnext/app/ui/screens/AddQuestionScreen.java` matching layout/spacing from `Myidea.md` (Hero bar alignment, gradient background, SURFACE card with proper centered layout)
  - Created supporting components in `src/main/java/com/upnext/app/ui/components/questions/` including `TagInputField` with autocomplete functionality and `TagChipList` for removable tag chips
  - Applied AppTheme colours, Segoe typography, focus rings, and hover behaviours with proper form validation and QuestionService integration
- [x] Step 41: Tag Entry UX & State Management (Completed Oct 26, 2025)
  - Implemented comprehensive tag entry controller in `AddQuestionViewModel.java` handling lowercase normalization, duplicate prevention (case-insensitive), +/- interactions, and 10-tag limit enforcement with validation feedback
  - Created robust ViewModel state management pattern with event listeners for UI updates, validation errors, and form lifecycle management for testability and future extensibility
  - Developed comprehensive unit test suite with `TagInputFieldTest.java` (12 test methods for UI automation) and `AddQuestionViewModelTest.java` (15 test methods for business logic) covering all edge cases and interaction flows
- [x] Step 42: Navigation & Home Refresh Wiring (Completed Oct 26, 2025)
  - Hooked Home "Ask a Question" CTA to navigate to AddQuestionScreen via `ViewNavigator` with proper constants and action listener
  - Implemented question feed refresh system with callback pattern: AddQuestionScreen notifies HomeScreen on successful creation, triggering feed refresh through FilterManager
  - Created comprehensive regression tests in `AddQuestionFlowTest.java` covering navigation flow, callback handling, feed refresh, and navigation stack integrity (5 test methods, all passing)
- [x] Step 43: Logging, Metrics & Telemetry
  - ✅ Instrumented `QuestionService` with comprehensive structured logging using markers for create success/failure, tag usage analytics, and performance timing
  - ✅ Updated metrics calculations so user profile (`total_upvotes`, `questions_asked`) refresh after question submission through UserRepository integration
  - ✅ Enhanced `AddQuestionViewModel` with UI action logging for tag management, form validation, and question creation flows
  - ✅ Created `TestLogCapture` utility for comprehensive telemetry validation with pattern matching and flow verification
  - ✅ Extended telemetry system with user metrics integration, operation timing, and structured error logging
- [x] Step 44: Documentation & QA Pass
  - ✅ Updated `docs/HomeScreenSpec.md` with Add Question button integration, dialog interactions, and navigation flow documentation
  - ✅ Enhanced `docs/DatabaseAccess.md` with comprehensive schema documentation for questions table, tag relationships, and transactional patterns
  - ✅ Created new `docs/AddQuestionGuide.md` with comprehensive user-facing instructions, step-by-step guide, and troubleshooting tips
  - ✅ Expanded `docs/TestPlan.md` with comprehensive Add Question test scenarios covering unit tests, integration tests, UI tests, error handling, transaction rollback, and telemetry validation (25+ new test cases)
