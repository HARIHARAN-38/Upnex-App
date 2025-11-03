# UpNext Application Test Plan

## Overview

This document outlines the manual test cases for the UpNext application. Each test case includes steps, expected results, and a section for noting issues or observations.

## Environment Setup

Before executing tests, ensure the following:

- MySQL database is running with correct schema
- Database credentials are properly configured
- JDBC driver is available in the lib directory
- Application has been compiled successfully

## Test Cases

### 1. Application Startup

| ID | TC-001 |
| --- | --- |
| **Test Case** | Application launches successfully |
| **Precondition** | Application compiled successfully |
| **Steps** | 1. Execute `java -cp "bin:lib/*" com.upnext.app.App` |
| **Expected Result** | - Application window appears<br>- Sign-in screen is displayed<br>- No error dialogs<br>- Log file created in logs directory |
| **Status** | ◯ Pass<br>◯ Fail |
| **Notes** |  |

### 2. User Authentication

#### 2.1 Account Creation

| ID | TC-101 |
| --- | --- |
| **Test Case** | Create new user account - Success |
| **Precondition** | Application started, on sign-in screen |
| **Steps** | 1. Click "Create Account" link<br>2. Enter valid name, email and matching passwords<br>3. Click "Create Account" button |
| **Expected Result** | - Success message displayed<br>- Redirected to sign-in screen<br>- Form fields cleared<br>- User record created in database |
| **Status** | ◯ Pass<br>◯ Fail |
| **Notes** |  |

| ID | TC-102 |
| --- | --- |
| **Test Case** | Create new user account - Email already exists |
| **Precondition** | User with email already exists in database |
| **Steps** | 1. Click "Create Account" link<br>2. Enter name with existing email<br>3. Enter matching passwords<br>4. Click "Create Account" button |
| **Expected Result** | - Error message "A user with this email already exists"<br>- Remains on create account screen<br>- Form fields preserved |
| **Status** | ◯ Pass<br>◯ Fail |
| **Notes** |  |

| ID | TC-103 |
| --- | --- |
| **Test Case** | Create new user account - Password mismatch |
| **Precondition** | Application started, on create account screen |
| **Steps** | 1. Enter name and email<br>2. Enter different passwords in password and confirm fields<br>3. Click "Create Account" button |
| **Expected Result** | - Error message "Passwords do not match"<br>- Remains on create account screen<br>- Form fields preserved |
| **Status** | ◯ Pass<br>◯ Fail |
| **Notes** |  |

#### 2.2 Sign In

| ID | TC-201 |
| --- | --- |
| **Test Case** | Sign in - Success |
| **Precondition** | User account exists in database |
| **Steps** | 1. Enter valid email and password<br>2. Click "Sign In" button |
| **Expected Result** | - Welcome message displayed<br>- Redirected to home screen<br>- Form fields cleared<br>- User name appears in welcome message on dashboard |
| **Status** | ◯ Pass<br>◯ Fail |
| **Notes** |  |

| ID | TC-202 |
| --- | --- |
| **Test Case** | Sign in - Invalid credentials |
| **Precondition** | Application started, on sign-in screen |
| **Steps** | 1. Enter invalid email/password combination<br>2. Click "Sign In" button |
| **Expected Result** | - Error message "Invalid email or password"<br>- Remains on sign-in screen<br>- Password field cleared<br>- Email field preserved |
| **Status** | ◯ Pass<br>◯ Fail |
| **Notes** |  |

| ID | TC-203 |
| --- | --- |
| **Test Case** | Sign in - Navigate to create account |
| **Precondition** | Application started, on sign-in screen |
| **Steps** | 1. Click "Create Account" link |
| **Expected Result** | - Create account screen displayed<br>- All form fields empty |
| **Status** | ◯ Pass<br>◯ Fail |
| **Notes** |  |

### 3. Dashboard Functionality

| ID | TC-301 |
| --- | --- |
| **Test Case** | Dashboard displays correctly |
| **Precondition** | User successfully signed in |
| **Steps** | 1. Observe the home screen after sign-in |
| **Expected Result** | - User name displayed in welcome message<br>- Dashboard grid with 4 placeholder widgets<br>- All UI elements properly styled |
| **Status** | ◯ Pass<br>◯ Fail |
| **Notes** |  |

| ID | TC-302 |
| --- | --- |
| **Test Case** | Sign out functionality |
| **Precondition** | User successfully signed in, on home screen |
| **Steps** | 1. Click "Sign Out" button<br>2. Observe the resulting behavior |
| **Expected Result** | - Sign out message displayed<br>- Redirected to sign-in screen<br>- Unable to navigate back to home screen without signing in again |
| **Status** | ◯ Pass<br>◯ Fail |
| **Notes** |  |

### 4. Skills Functionality

#### 4.1 Registration with Skills

| ID | TC-401 |
| --- | --- |
| **Test Case** | Navigate to skills screen during registration |
| **Precondition** | Application started, on create account screen |
| **Steps** | 1. Fill in name, email and matching passwords<br>2. Click "Create Account" button |
| **Expected Result** | - Redirected to skillset screen<br>- User data preserved<br>- Create account button initially disabled<br>- Back button available |
| **Status** | ◯ Pass<br>◯ Fail |
| **Notes** |  |

| ID | TC-402 |
| --- | --- |
| **Test Case** | Add skill during registration |
| **Precondition** | On skillset screen during registration |
| **Steps** | 1. Click "Add New Skill" button<br>2. Enter skill name "Java Programming"<br>3. Enter description "Core Java and frameworks"<br>4. Set proficiency to 8<br>5. Click "Add Skill" button |
| **Expected Result** | - Redirected back to skillset screen<br>- Skill card appears with correct data<br>- Create account button becomes enabled<br>- Delete option available on skill card |
| **Status** | ◯ Pass<br>◯ Fail |
| **Notes** |  |

| ID | TC-403 |
| --- | --- |
| **Test Case** | Cancel adding a skill |
| **Precondition** | On add skill screen during registration |
| **Steps** | 1. Enter some skill data<br>2. Click "Cancel" button |
| **Expected Result** | - Redirected back to skillset screen<br>- No new skill added<br>- Previous skills still displayed |
| **Status** | ◯ Pass<br>◯ Fail |
| **Notes** |  |

| ID | TC-404 |
| --- | --- |
| **Test Case** | Delete skill during registration |
| **Precondition** | On skillset screen with at least one skill added |
| **Steps** | 1. Click delete button (X) on a skill card |
| **Expected Result** | - Skill removed from display<br>- If last skill removed, create account button becomes disabled |
| **Status** | ◯ Pass<br>◯ Fail |
| **Notes** |  |

| ID | TC-405 |
| --- | --- |
| **Test Case** | Navigate back from skillset to create account screen |
| **Precondition** | On skillset screen with skills added |
| **Steps** | 1. Click "Back" button<br>2. Confirm navigation in dialog |
| **Expected Result** | - Confirmation dialog appears warning about losing skills<br>- After confirmation, redirected to create account screen<br>- Form fields still populated |
| **Status** | ◯ Pass<br>◯ Fail |
| **Notes** |  |

| ID | TC-406 |
| --- | --- |
| **Test Case** | Complete registration with skills |
| **Precondition** | On skillset screen with at least one skill added |
| **Steps** | 1. Click "Create Account" button |
| **Expected Result** | - Account created successfully<br>- Redirected to home screen<br>- Success message displayed<br>- User logged in with correct name |
| **Status** | ◯ Pass<br>◯ Fail |
| **Notes** |  |

#### 4.2 Validation of Skills

| ID | TC-407 |
| --- | --- |
| **Test Case** | Add skill with empty name |
| **Precondition** | On add skill screen during registration |
| **Steps** | 1. Leave skill name empty<br>2. Enter description<br>3. Set proficiency<br>4. Click "Add Skill" button |
| **Expected Result** | - Error message "Skill name is required"<br>- Remains on add skill screen<br>- Form fields preserved |
| **Status** | ◯ Pass<br>◯ Fail |
| **Notes** |  |

| ID | TC-408 |
| --- | --- |
| **Test Case** | Add skill with too long name |
| **Precondition** | On add skill screen during registration |
| **Steps** | 1. Enter skill name over 100 characters<br>2. Enter description<br>3. Set proficiency<br>4. Click "Add Skill" button |
| **Expected Result** | - Error message about name length<br>- Remains on add skill screen<br>- Form fields preserved |
| **Status** | ◯ Pass<br>◯ Fail |
| **Notes** |  |

| ID | TC-409 |
| --- | --- |
| **Test Case** | Add skill with too long description |
| **Precondition** | On add skill screen during registration |
| **Steps** | 1. Enter valid skill name<br>2. Enter description over 255 characters<br>3. Set proficiency<br>4. Click "Add Skill" button |
| **Expected Result** | - Error message about description length<br>- Remains on add skill screen<br>- Form fields preserved |
| **Status** | ◯ Pass<br>◯ Fail |
| **Notes** |  |

| ID | TC-410 |
| --- | --- |
| **Test Case** | Create account without adding skills |
| **Precondition** | On skillset screen with no skills added |
| **Steps** | 1. Click "Create Account" button |
| **Expected Result** | - Error message "Please add at least one skill to continue"<br>- Remains on skillset screen |
| **Status** | ◯ Pass<br>◯ Fail |
| **Notes** |  |

### 5. Error Handling

| ID | TC-501 |
| --- | --- |
| **Test Case** | Database connection failure |
| **Precondition** | MySQL service stopped or credentials changed |
| **Steps** | 1. Start the application<br>2. Attempt to sign in |
| **Expected Result** | - Error message displayed<br>- Error logged in log file<br>- Application remains functional but unable to authenticate |
| **Status** | ◯ Pass<br>◯ Fail |
| **Notes** |  |

| ID | TC-502 |
| --- | --- |
| **Test Case** | Error logging |
| **Precondition** | Application started |
| **Steps** | 1. Perform various operations including errors<br>2. Check log file in logs directory |
| **Expected Result** | - Log file contains entries for application start<br>- Log file contains appropriate entries for user actions<br>- Errors are logged with timestamps and stack traces |
| **Status** | ◯ Pass<br>◯ Fail |
| **Notes** |  |

## Test Results Summary

| Category | Tests Passed | Tests Failed | Total |
| --- | --- | --- | --- |
| Application Startup | | | 1 |
| Account Creation | | | 3 |
| Sign In | | | 3 |
| Dashboard Functionality | | | 2 |
| Skills Functionality | | | 10 |
| Home Screen & Search | | | 5 |
| Error Handling | | | 2 |
| **Total** | | | 26 |

### 6. Home Screen & Search

| ID | TC-601 |
| --- | --- |
| **Test Case** | Filter preservation during navigation |
| **Precondition** | User signed in, on home screen |
| **Steps** | 1. Apply filters (select a subject, add tags, enter search text)<br>2. Click on a question to navigate to detail screen<br>3. Click back button to return to home screen |
| **Expected Result** | - All previously applied filters are still active<br>- Question feed shows the same filtered results<br>- Filter UI elements show the same selections |
| **Status** | ◯ Pass<br>◯ Fail |
| **Notes** |  |

| ID | TC-602 |
| --- | --- |
| **Test Case** | Filter reset button functionality |
| **Precondition** | User signed in, on home screen with filters applied |
| **Steps** | 1. Click "Clear Filters" button |
| **Expected Result** | - All filters are cleared<br>- Subject selection reverts to "All Subjects"<br>- No tags are selected<br>- Search text is cleared<br>- Question feed updates to show unfiltered results |
| **Status** | ◯ Pass<br>◯ Fail |
| **Notes** |  |

| ID | TC-603 |
| --- | --- |
| **Test Case** | Search functionality |
| **Precondition** | User signed in, on home screen |
| **Steps** | 1. Enter search term in search box<br>2. Wait for results to load |
| **Expected Result** | - Question feed updates to show matching results<br>- Results include questions with search term in title or content<br>- No results message shown if no matches found |
| **Status** | ◯ Pass<br>◯ Fail |
| **Notes** |  |

| ID | TC-604 |
| --- | --- |
| **Test Case** | Subject and tag filtering |
| **Precondition** | User signed in, on home screen |
| **Steps** | 1. Select a subject from the left panel<br>2. Select one or more tags<br>3. Observe question feed |
| **Expected Result** | - Question feed updates with each selection<br>- Only questions matching both subject and tags are shown<br>- Filter pills/chips show active filters |
| **Status** | ◯ Pass<br>◯ Fail |
| **Notes** |  |

| ID | TC-605 |
| --- | --- |
| **Test Case** | Filter persistence between sessions |
| **Precondition** | User signed in, on home screen with filters applied |
| **Steps** | 1. Close the application<br>2. Re-open the application and sign in<br>3. Navigate to home screen |
| **Expected Result** | - Previously applied filters are still active<br>- Question feed shows same filtered results<br>- Filter UI elements show same selections |
| **Status** | ◯ Pass<br>◯ Fail |
| **Notes** |  |

## Add Question Feature Testing

### 7.1 Question Creation Flow (Unit & Integration Tests)

| **Test Case ID** | **Test Description** | **Steps** | **Expected Result** | **Status** | **Notes** |
|---|---|---|---|---|---|
| AQ-UNIT-001 | Validate question creation with valid data | 1. Call QuestionService.createQuestion() with valid Question object<br>2. Verify database transaction commits<br>3. Check telemetry logging | - Question saved to database<br>- User metrics updated<br>- Operation logged with marker | ◯ Pass<br>◯ Fail |  |
| AQ-UNIT-002 | Validate question with tags association | 1. Create question with existing tags<br>2. Verify tag linking in question_tags table<br>3. Check tag analytics update | - Question-tag relationships created<br>- Tag usage counts incremented<br>- Analytics logged | ◯ Pass<br>◯ Fail |  |
| AQ-UNIT-003 | Test question validation rules | 1. Attempt creation with invalid data (empty text, null subject)<br>2. Verify validation errors<br>3. Check no database changes | - Validation errors returned<br>- No database modifications<br>- Error logged appropriately | ◯ Pass<br>◯ Fail |  |
| AQ-INT-001 | End-to-end question creation | 1. Authenticate user<br>2. Create question through service layer<br>3. Verify in database and search index | - Question appears in user's questions<br>- Question searchable immediately<br>- Full audit trail created | ◯ Pass<br>◯ Fail |  |

### 7.2 User Interface Testing

| **Test Case ID** | **Test Description** | **Steps** | **Expected Result** | **Status** | **Notes** |
|---|---|---|---|---|---|
| AQ-UI-001 | Add Question dialog opening | 1. Navigate to Home Screen<br>2. Click "Ask a Question" button<br>3. Verify dialog opens | - Add Question dialog displays<br>- All form fields visible and enabled<br>- Focus set to question text field | ◯ Pass<br>◯ Fail |  |
| AQ-UI-002 | Question text input validation | 1. Open Add Question dialog<br>2. Enter various question text lengths<br>3. Test with empty, short, and maximum length text | - Validation feedback shown in real-time<br>- Submit button enabled/disabled appropriately<br>- Character count displayed | ◯ Pass<br>◯ Fail |  |
| AQ-UI-003 | Subject selection functionality | 1. Click subject dropdown<br>2. Select different subjects<br>3. Verify selection persistence | - Dropdown shows all available subjects<br>- Selection updates form state<br>- Selected subject displayed correctly | ◯ Pass<br>◯ Fail |  |
| AQ-UI-004 | Tag management interface | 1. Add new tags via input field<br>2. Remove existing tags<br>3. Test tag suggestions | - Tags added/removed correctly<br>- Tag suggestions appear based on input<br>- Visual feedback for tag operations | ◯ Pass<br>◯ Fail |  |
| AQ-UI-005 | Form submission success flow | 1. Fill valid question data<br>2. Click Submit button<br>3. Verify success feedback | - Success message displayed<br>- Dialog closes automatically<br>- Question appears in home screen feed | ◯ Pass<br>◯ Fail |  |

### 7.3 Error Handling & Validation

| **Test Case ID** | **Test Description** | **Steps** | **Expected Result** | **Status** | **Notes** |
|---|---|---|---|---|---|
| AQ-ERR-001 | Database connection failure | 1. Simulate database unavailability<br>2. Attempt question creation<br>3. Verify error handling | - User-friendly error message shown<br>- Operation logged as failure<br>- Form data preserved for retry | ◯ Pass<br>◯ Fail |  |
| AQ-ERR-002 | Validation error display | 1. Submit form with invalid data<br>2. Verify error messages<br>3. Test error message clearing | - Specific validation errors shown<br>- Errors clear when data corrected<br>- Focus returned to problem fields | ◯ Pass<br>◯ Fail |  |
| AQ-ERR-003 | Concurrent modification handling | 1. Open Add Question dialog in multiple instances<br>2. Submit from both simultaneously<br>3. Verify proper handling | - Both submissions processed or proper conflict resolution<br>- No data corruption<br>- Appropriate feedback to users | ◯ Pass<br>◯ Fail |  |
| AQ-ERR-004 | Network timeout scenarios | 1. Simulate slow network during submission<br>2. Test timeout handling<br>3. Verify retry mechanisms | - Timeout handled gracefully<br>- User informed of delay/failure<br>- Retry option available | ◯ Pass<br>◯ Fail |  |

### 7.4 Transaction & Rollback Testing

| **Test Case ID** | **Test Description** | **Steps** | **Expected Result** | **Status** | **Notes** |
|---|---|---|---|---|---|
| AQ-TXN-001 | Successful transaction commit | 1. Create question with tags<br>2. Verify all database changes committed atomically<br>3. Check transaction logging | - Question and tags saved together<br>- User metrics updated<br>- Transaction marked complete | ◯ Pass<br>◯ Fail |  |
| AQ-TXN-002 | Transaction rollback on failure | 1. Simulate failure during question creation<br>2. Verify rollback behavior<br>3. Check database consistency | - No partial data saved<br>- Database state unchanged<br>- Error logged with rollback marker | ◯ Pass<br>◯ Fail |  |
| AQ-TXN-003 | Tag creation rollback | 1. Create question with new tags<br>2. Simulate failure after tag creation<br>3. Verify complete rollback | - New tags not persisted<br>- Question not saved<br>- Clean rollback logged | ◯ Pass<br>◯ Fail |  |

### 7.5 Performance & Telemetry Validation

| **Test Case ID** | **Test Description** | **Steps** | **Expected Result** | **Status** | **Notes** |
|---|---|---|---|---|---|
| AQ-PERF-001 | Question creation performance | 1. Create questions under normal load<br>2. Measure response times<br>3. Verify performance telemetry | - Creation completes within 2 seconds<br>- Performance metrics logged<br>- No memory leaks detected | ◯ Pass<br>◯ Fail |  |
| AQ-TEL-001 | Telemetry marker verification | 1. Perform various Add Question operations<br>2. Verify telemetry markers in logs<br>3. Check metric accuracy | - All operations logged with proper markers<br>- User metrics updated correctly<br>- Analytics data accurate | ◯ Pass<br>◯ Fail |  |
| AQ-TEL-002 | UI action logging validation | 1. Interact with Add Question dialog<br>2. Verify UI action telemetry<br>3. Check action correlation | - UI interactions logged appropriately<br>- Action flows traceable<br>- Performance timing captured | ◯ Pass<br>◯ Fail |  |

## Follow-up Items

1. Create automated tests for filter persistence
2. Implement continuous integration tests for Add Question feature
3. Document filter state persistence behavior
4. Add performance benchmarking for question creation operations

## Test Environment

- OS: 
- Java Version:
- MySQL Version:
- Date Tested:
- Tester:

## Summary

This test plan provides comprehensive coverage for the UpNext-App application, including all core functionality and the recently implemented Add Question feature. The test cases cover:

- **Unit Tests**: Service layer validation, business logic testing, data integrity checks
- **Integration Tests**: End-to-end workflows, database transactions, service interactions  
- **UI Tests**: User interface functionality, form validation, navigation flows
- **Error Handling**: Exception scenarios, rollback verification, user feedback
- **Performance**: Response time validation, telemetry verification, resource usage

### Test Execution Priority

1. **Critical Path**: Authentication, Question Creation, Core Navigation
2. **Feature Validation**: Skills Management, Search Functionality, Add Question Flow
3. **Error Scenarios**: Database failures, Network issues, Validation errors
4. **Performance**: Load testing, Telemetry validation, Memory usage

### Test Environment Requirements

- Java 11 or higher
- MySQL 8.0+
- Test database with sample data
- Network simulation tools for timeout testing
- Log monitoring tools for telemetry validation

---

*Last Updated: January 27, 2025*
*Add Question Feature Testing Added: Step 44 Implementation*