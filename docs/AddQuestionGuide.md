# UpNext Add Question User Guide

## Overview

The Add Question feature allows you to share your questions with the UpNext community and get help from other users. This guide walks you through the process of creating effective questions that will get quality responses.

## Getting Started

### Prerequisites

Before you can ask questions, you must:
1. Have a valid UpNext account
2. Be signed in to the application
3. Have completed the initial skill setup process

### Accessing the Add Question Feature

1. **From the Home Screen**: Click the "Ask a Question" button in the center of the screen
2. **Navigation**: You'll be taken to the Add Question form screen
3. **Return**: You can return to the home screen at any time using the "Cancel" button

## Step-by-Step Question Creation

### 1. Question Title

**Purpose**: Your title is the first thing users see and determines whether they'll read your full question.

**Best Practices:**
- Be specific and descriptive (minimum 5 characters, maximum 200)
- Include key technologies or concepts
- Avoid vague titles like "Help me" or "This doesn't work"

**Examples:**
- ❌ **Poor**: "Java problem"
- ✅ **Good**: "How do I handle NullPointerException in Spring Boot REST controller?"

**Validation Rules:**
- Minimum 5 characters required
- Maximum 200 characters allowed
- Title cannot be empty or contain only whitespace

### 2. Question Content

**Purpose**: The detailed description of your problem, what you've tried, and what you expect to happen.

**Structure Your Content:**
1. **Problem Description**: What are you trying to accomplish?
2. **What You've Tried**: Show your code attempts and research efforts
3. **Expected vs. Actual Results**: Describe what should happen and what actually happens
4. **Environment Details**: Mention relevant versions, tools, or configurations

**Best Practices:**
- Be thorough but concise (minimum 10 characters, maximum 5000)
- Include relevant code snippets
- Mention error messages exactly as they appear
- Describe your environment and setup

**Example Structure:**
```
I'm building a REST API with Spring Boot and need to validate user input 
before saving to the database.

**What I've tried:**
- Used @Valid annotation on the request body
- Added validation annotations to my model class

**Current code:**
[Include relevant code snippet]

**Error message:**
[Include exact error text]

**Expected result:**
The API should return a 400 status with validation errors when invalid data is submitted.

**Actual result:**
The data is saved without validation, allowing invalid entries in the database.
```

**Validation Rules:**
- Minimum 10 characters required
- Maximum 5000 characters allowed
- Content cannot be empty or contain only whitespace

### 3. Context (Optional)

**Purpose**: Additional background information that helps users understand your situation better.

**When to Use Context:**
- You're working on a specific project or assignment
- There are constraints or requirements that affect the solution
- You need to explain the business logic or use case
- The problem occurs in a specific environment or configuration

**Examples:**
- "This is for a university assignment where we cannot use external libraries"
- "I'm working on a legacy system that must maintain backward compatibility"
- "This code will be used in a high-traffic production environment"

**Best Practices:**
- Keep it relevant to the question
- Maximum 1000 characters
- Don't repeat information from the main content
- Focus on constraints, requirements, or special circumstances

### 4. Tags

**Purpose**: Tags help categorize your question and make it discoverable by users with relevant expertise.

**Tag Guidelines:**
- **Maximum 10 tags** per question
- **Maximum 50 characters** per tag
- **Use existing tags** when possible (autocomplete will suggest them)
- **Be specific**: Prefer "spring-boot" over just "spring"
- **Include technology stack**: programming language, framework, tools

**Recommended Tag Categories:**
1. **Programming Language**: `java`, `python`, `javascript`
2. **Framework/Library**: `spring-boot`, `react`, `hibernate`
3. **Technology/Tool**: `mysql`, `docker`, `git`
4. **Concept/Topic**: `rest-api`, `security`, `performance`

**Tag Examples by Question Type:**

**Java Spring Boot API Question:**
- `java`, `spring-boot`, `rest-api`, `validation`

**Database Performance Question:**
- `mysql`, `performance`, `indexing`, `query-optimization`

**React Frontend Question:**
- `javascript`, `react`, `hooks`, `state-management`

**Adding Tags:**
1. **Type in the tag input field**: Start typing your tag name
2. **Use autocomplete**: Select from existing tags when possible
3. **Press Enter or click Add**: Confirm the tag addition
4. **Remove tags**: Click the × button on any tag chip
5. **Validation**: System prevents duplicates and enforces limits

### 5. Form Validation

The Add Question form includes real-time validation to ensure quality:

**Title Validation:**
- Required field indicator
- Character count display
- Minimum length enforcement

**Content Validation:**
- Required field indicator
- Character count display
- Minimum length enforcement

**Tag Validation:**
- Duplicate prevention (case-insensitive)
- Character limit per tag (50 characters)
- Maximum tag count (10 tags)
- Invalid character detection

**Error Messages:**
- Appear immediately when validation fails
- Clear explanations of what needs to be fixed
- Persistent until the issue is resolved

## Submission Process

### Successful Submission

1. **Validation Check**: All required fields must be completed correctly
2. **Click Submit**: Press the "Ask Question" button
3. **Processing**: System creates your question and updates your profile metrics
4. **Confirmation**: You're returned to the home screen
5. **Feed Update**: Your new question appears at the top of the question feed
6. **Metrics Update**: Your "Questions Asked" count increases by 1

### Error Handling

If submission fails:
1. **Stay on Form**: You remain on the Add Question screen
2. **Error Display**: Specific error messages appear
3. **Data Preservation**: Your entered data is preserved
4. **Retry**: Fix the issues and submit again

**Common Error Scenarios:**
- **Authentication Error**: You've been signed out (sign in again)
- **Validation Error**: Form data doesn't meet requirements
- **Network Error**: Connection issues (try again)
- **Server Error**: Temporary system issue (try again later)

## Tips for Getting Better Answers

### Writing Effective Questions

1. **Do Your Research First**
   - Search existing questions for similar issues
   - Try to solve the problem yourself
   - Document what you've already attempted

2. **Be Specific and Clear**
   - Include exact error messages
   - Mention specific versions of tools/frameworks
   - Provide minimal, complete, and verifiable examples

3. **Show Your Code**
   - Include relevant code snippets (not your entire project)
   - Format code properly for readability
   - Remove sensitive or irrelevant information

4. **Explain Your Goal**
   - What are you trying to accomplish?
   - Why are you taking this particular approach?
   - Are there any constraints or requirements?

### Community Guidelines

**Be Respectful:**
- Use professional language
- Be patient when waiting for responses
- Thank users who help you

**Be Responsible:**
- Don't ask for homework solutions without showing effort
- Don't post the same question multiple times
- Follow up with your results when you solve the problem

**Be Helpful:**
- Answer questions in areas where you have expertise
- Vote on questions and answers to help the community
- Share your own solutions when you find them

## Frequently Asked Questions

### Q: How long should I wait for an answer?
**A:** Response times vary based on the complexity and popularity of your question. Most questions receive some response within 24-48 hours. More complex or niche topics may take longer.

### Q: Can I edit my question after submitting it?
**A:** Currently, question editing is not available, but it's planned for a future release. Make sure to review your question carefully before submitting.

### Q: What if my question gets downvoted?
**A:** Downvotes usually indicate that your question needs improvement. Consider:
- Adding more details or context
- Showing what you've already tried
- Making your question more specific
- Improving your code formatting

### Q: How many questions can I ask?
**A:** There's no limit on the number of questions you can ask, but focus on quality over quantity. Well-researched, specific questions get better responses.

### Q: Can I ask the same question in multiple categories?
**A:** No, avoid posting duplicate questions. Instead, use appropriate tags to ensure your question reaches the right audience.

### Q: What if I find the answer to my own question?
**A:** Great! You can share your solution by answering your own question once the answer feature is implemented, or mention the solution in the question comments.

## Technical Requirements

### Browser Compatibility
- Works in all modern browsers
- JavaScript must be enabled
- No additional plugins required

### Network Requirements
- Active internet connection required for submission
- Form data is automatically saved locally as you type
- Offline submission is not supported

### Character Limits Summary
- **Title**: 5-200 characters
- **Content**: 10-5,000 characters  
- **Context**: 0-1,000 characters (optional)
- **Tags**: 0-10 tags, 50 characters each

## Getting Help

If you encounter issues with the Add Question feature:

1. **Check this guide** for common solutions
2. **Verify your internet connection** is stable
3. **Try refreshing the page** if the form isn't responding
4. **Contact support** if problems persist

## Feature Updates

This guide reflects the current version of the Add Question feature. Future enhancements may include:

- Question editing capabilities
- Rich text formatting for code snippets
- Image and file attachments
- Question templates for common scenarios
- Advanced tag suggestions based on content analysis

---

*Last Updated: October 26, 2025*
*Feature Version: Step 36-43 Implementation*