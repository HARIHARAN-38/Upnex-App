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

### 4. Error Handling

| ID | TC-401 |
| --- | --- |
| **Test Case** | Database connection failure |
| **Precondition** | MySQL service stopped or credentials changed |
| **Steps** | 1. Start the application<br>2. Attempt to sign in |
| **Expected Result** | - Error message displayed<br>- Error logged in log file<br>- Application remains functional but unable to authenticate |
| **Status** | ◯ Pass<br>◯ Fail |
| **Notes** |  |

| ID | TC-402 |
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
| Error Handling | | | 2 |
| **Total** | | | 11 |

## Follow-up Items

1. 
2. 
3. 

## Test Environment

- OS: 
- Java Version:
- MySQL Version:
- Date Tested:
- Tester:

---

*Last Updated: October 16, 2025*