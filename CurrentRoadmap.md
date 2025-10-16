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
