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
1. Navigation will occur to a future `QuestionPostScreen` (to be implemented)
2. After submitting a new question, user will return to `HomeScreen`

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

## Implementation Status

- ✅ Three-column layout
- ✅ Filter manager implementation
- ✅ Question feed with filtering
- ✅ Filter persistence
- ✅ Navigation between Home and Question Detail
- ✅ Filter state preservation during navigation
- ⬜ "Ask a Question" screen implementation
- ⬜ User Profile screen implementation

---

*Last Updated: October 17, 2025*