# Home Screen Specification

## Overview

This document details the UX flow, component interactions, and navigation patterns for the UpNext application's Home Screen and its related views.

## Components Architecture

### Three-Column Layout

The Home Screen is structured as a responsive three-column layout:

1. **Left Column (22% width)**: Subject navigation and tag filters
2. **Center Column (55% width)**: Question feed with toolbar filters
3. **Right Column (23% width)**: User profile summary and metrics

### Hero Bar

The Hero Bar appears at the top of the screen and contains:

- Application logo
- Search input with autocomplete/dropdown
- User avatar with dropdown menu for profile navigation
- Sign-out button

## State Management

### Filter Manager

The HomeScreen implements a unified filter state manager (`FilterManager`) that:

- Combines subject selection, tag selection, search text, and toolbar filters
- Persists filter state between sessions using `Preferences`
- Broadcasts filter changes to all subscribed components
- Converts filter state into `QuestionSearchCriteria` for repository queries

### Filter Persistence

Filter state is automatically persisted:

1. When a user selects filters on the home screen
2. When navigating to the question detail screen
3. When returning from the question detail screen
4. Between application sessions

## Navigation Flows

### Home → Question Detail

When a user clicks on a question card:
1. `HomeScreen.navigateToQuestionDetail()` is called with question ID
2. Navigation occurs to `QuestionDetailScreen`
3. `QuestionDetailScreen.loadQuestion()` loads the question
4. Filter state is automatically preserved by `FilterManager`

### Question Detail → Home

When a user clicks the back button:
1. `QuestionDetailScreen.navigateBack()` is called
2. Navigation occurs back to `HomeScreen`
3. `HomeScreen` automatically applies the preserved filters
4. Question feed is refreshed with the same filters as before

### Ask a Question

When a user clicks the "Ask a Question" button:
1. `HomeScreen.navigateToAddQuestion()` is called via CTA action listener
2. Navigation occurs to `AddQuestionScreen` using ViewNavigator
3. User fills out question form (title, content, context, tags)
4. On successful question creation, callback triggers home screen refresh
5. User returns to `HomeScreen` with updated question feed including their new question

#### Add Question Flow Details

The Add Question integration uses a callback pattern for seamless user experience:

**Navigation Setup:**
```java
// HomeScreen sets up navigation to AddQuestionScreen
askQuestionButton.addActionListener(e -> {
    AddQuestionScreen addScreen = new AddQuestionScreen();
    addScreen.setOnQuestionCreated(this::addNewQuestionToFeed);
    navigator.navigateTo("AddQuestionScreen", addScreen);
});
```

**Callback Implementation:**
```java
// AddQuestionScreen notifies HomeScreen on successful creation
private void addNewQuestionToFeed(Question newQuestion) {
    // Add question to top of feed for immediate visibility
    questionFeedPanel.addQuestionToTop(newQuestion);
    
    // Refresh feed to show updated data with proper filtering
    refreshQuestionFeed();
    
    // Log successful integration
    Logger.getInstance().info("Adding new question to feed: " + newQuestion.getTitle());
}
```

**Feed Refresh Integration:**
- New questions appear at the top of the feed immediately
- Feed respects current filter state (subject, tags, search terms)
- User metrics are updated in the profile summary card
- Proper telemetry logging for analytics and debugging

### Avatar → Profile

When a user clicks their avatar:
1. Navigation will occur to a future `UserProfileScreen` (to be implemented)
2. User can view and edit their profile details

## Responsive Behavior

The HomeScreen adapts to different window sizes using the following breakpoints:
- **Small**: < 800px 
- **Medium**: 800px - 1200px
- **Large**: 1200px - 1600px
- **Extra Large**: > 1600px

## Testing Strategy

### Unit Tests

Unit tests focus on testing individual components:
- `FilterManager` state management and persistence
- `QuestionFeedPanel` rendering and filtering
- `SubjectNavigationPanel` selection and events

### Integration Tests

Integration tests focus on component interactions:
- Filter selection → question feed updates
- Search input → filter application

### Navigation Tests

Navigation tests focus on screen transitions and state preservation:
- Home → Detail → Home navigation with filter persistence
- "Ask a Question" button navigation
- Avatar → Profile navigation

## Future Enhancements

1. **Recently Viewed Questions**: Add a section showing recently viewed questions
2. **User Notifications**: Add notification panel for question answers and updates
3. **Advanced Search**: Expand search capabilities with date range and other filters
4. **Saved Searches**: Allow users to save and quickly access favorite searches
5. **Custom Filter Layouts**: Allow users to customize the layout and visibility of filters

## Add Question Integration Features

### UI Components

**AddQuestionScreen Integration:**
- Full-screen form with Hero bar alignment and gradient background
- Comprehensive form fields: title, content, context (optional), tags
- Real-time tag input with autocomplete from existing tags
- Tag chip display with removal capabilities (max 10 tags)
- Form validation with inline error messaging
- Cancel and Submit buttons with proper navigation flow

**Tag Management System:**
- `TagInputField` with autocomplete dropdown from existing tags
- `TagChipList` for visual tag management with removal buttons
- Duplicate prevention (case-insensitive)
- Length validation (max 50 characters per tag)
- Tag limit enforcement (max 10 tags per question)

**State Management:**
- `AddQuestionViewModel` handles form state and validation
- Event-driven architecture with listeners for UI updates
- Comprehensive validation with user-friendly error messages
- Integration with `QuestionService` for business logic

### Navigation Architecture

**HomeScreen → AddQuestionScreen:**
- Triggered by "Ask a Question" CTA button click
- Uses ViewNavigator for screen transition management
- Callback setup for feed refresh on successful creation

**AddQuestionScreen → HomeScreen:**
- Auto-navigation after successful question creation
- Cancel button returns to home without creating question
- Error handling maintains user on form with validation messages

### Data Flow Integration

**Question Creation Pipeline:**
1. User input → AddQuestionViewModel validation
2. ViewModel → QuestionService business logic validation  
3. QuestionService → QuestionRepository transactional persistence
4. Success callback → HomeScreen feed refresh
5. User metrics update → Profile card refresh

**Telemetry Integration:**
- Structured logging for all user interactions and system operations
- Performance timing for question creation operations
- User metrics tracking (questions_asked counter)
- Tag analytics for popular tags and usage patterns
- Error tracking with detailed failure categorization

## Implementation Status

- ✅ Three-column layout
- ✅ Filter manager implementation
- ✅ Question feed with filtering
- ✅ Filter persistence
- ✅ Navigation between Home and Question Detail
- ✅ Filter state preservation during navigation
- ✅ "Ask a Question" screen implementation (Steps 36-43)
- ✅ Add Question navigation and callback integration
- ✅ Question creation with tag management and validation
- ✅ Feed refresh system with new question integration
- ✅ User metrics and telemetry integration
- ⬜ User Profile screen implementation

---

*Last Updated: October 26, 2025 - Added Add Question Integration (Step 44)*