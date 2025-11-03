Theme: Use AppTheme palette (PRIMARY #1F6FEB, PRIMARY_DARK #0D3A75, ACCENT #FF7B72, BACKGROUND #F5F7FB, SURFACE #FFFFFF, text colors from theme) and font stack from AppTheme (Segoe UI, 14pt base, bold 18pt headings). Apply rounded corners, soft drop shadows, gradient background similar to mock while respecting theme colors.

Global Layout: Desktop-first responsive grid with three vertical panels of equal height, subtle gradient background, central surface panel with drop shadow. Maintain spacing, paddings, and alignment matching reference mock.

Hero Bar (top)

Left: UpNex logo text using PRIMARY color.
Center: Full-width search bar with placeholder “Search questions, topics, or users…”. Implement lightweight fuzzy search: tokenize title/body/tags, lowercase keywords, count matches, support partial matches using LIKE '%term%' or trigram similarity. Typing filters the center panel list live.
Right: circular avatar/menu icon button; clicking navigates to profile page. Use SURFACE background, PRIMARY border hover states.
Main Content Panels
Goal
Create an "Add Question" page for the UpNext desktop app that follows the project's AppTheme and integrates with existing JDBC data layer (database name: upnex, host: 127.0.0.1, user: root, password: hari). The page must open from the home "Ask a question" button and post new questions as question cards on the home page with tags and optional context.

UI & Visuals

Theme: Use colors and fonts from AppTheme (PRIMARY #1F6FEB, PRIMARY_DARK #0D3A75, ACCENT #FF7B72, BACKGROUND #F5F7FB, SURFACE #FFFFFF; font stack Segoe UI; base 14pt; headings bold 18pt). Use rounded corners, soft drop shadows, and a subtle gradient background for the page container.
Layout: Desktop-first responsive layout matching app grid: central surface panel with drop shadow and padding. Use spacing consistent with other screens.
Controls:
Title input: single-line text field placeholder "Enter your question…" — required, max 255 chars.
Context input: multi-line text area placeholder "Add more context (optional)" — optional, max 2000 chars.
Tag input: single-line input with plus button. When user types a tag and presses the + icon or Enter, the tag is added to a visual tag list pill. Duplicates are ignored; trim whitespace; limit to 10 tags. Each tag pill has a small × to remove it.
Buttons: "Post" primary button (PRIMARY color). "Cancel" secondary text button. Disable Post until Title is non-empty.
Accessibility: labels for inputs, focus ring using PRIMARY color, tooltips for icons.
Behavior & Validation

Title required; show inline validation message if empty on Post attempt.
Context optional.
Tag entry: pressing Enter or clicking + adds tag. Tag sanitized (lowercase) and stored as text. Multiple tags allowed.
When Post clicked:
Assemble payload: title, optional context, tags (array), user_id (current user; use logged-in user repo), created_at timestamp.
Validate on client; then call repository backend method to persist.
On success: close Add Question page/modal and insert new question card at top of home feed (live update).
On failure: show error toast with reason.
Back-end contract & data shapes

Question DTO / domain object:
id: Long (generated)
userId: Long
title: String (<=255)
context: Text / String (nullable)
upvotes: int default 0
downvotes: int default 0
answerCount: int default 0
isSolved: boolean default false
viewCount: int default 0
createdAt: Timestamp
updatedAt: Timestamp
Tag: name String
Insert semantics:
Insert question row -> get generated id.
For each tag: upsert into tags (usage_count++), then insert into question_tags (question_id, tag_id).
Use a transaction.
DB Schema changes (if needed)

Ensure questions table has context TEXT NULL. If it doesn't exist, alter:
ALTER TABLE questions ADD COLUMN context TEXT;
Ensure users.id and all FK columns use same type: BIGINT UNSIGNED or BIGINT (match existing users.id). Use BIGINT consistently.
Ensure question_tags exists as junction table with PK(question_id, tag_id), FK to questions(id) and tags(id).
Ensure tags.name is unique and tags.id is BIGINT.
Provide migration file: sql/008_add_question_context_and_constraints.sql with idempotent statements (IF NOT EXISTS / ADD COLUMN IF NOT EXISTS pattern).
JDBC & Data Layer integration

Use existing JDBC connector jar in lib. Add or confirm JdbcConnectionProvider uses:
URL: jdbc:mysql://127.0.0.1:3306/upnex
User: root
Password: hari
Expose a QuestionRepository.save(Question question) method that handles the transaction described above.
Ensure QuestionRepository uses PreparedStatement with proper Types for NULL subject_id and context.
Update or add tests: src/test/java/.../QuestionRepositoryAddTest.java for happy path and tag handling.


there is jdbc connector jar file in lib directory create a jdbc connection file the connects database: name = upnex, pass = hari, its in 127. local host ip. and also the data that are getting from this page should be present in the user table so make sure to alter according to that


based on the above info create a detailed road map with proper file names and structure in CurrentRoadmap.md make it like [ ] step 1 so its easy to mark as done after implementation

