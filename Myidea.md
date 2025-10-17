Theme: Use AppTheme palette (PRIMARY #1F6FEB, PRIMARY_DARK #0D3A75, ACCENT #FF7B72, BACKGROUND #F5F7FB, SURFACE #FFFFFF, text colors from theme) and font stack from AppTheme (Segoe UI, 14pt base, bold 18pt headings). Apply rounded corners, soft drop shadows, gradient background similar to mock while respecting theme colors.

Global Layout: Desktop-first responsive grid with three vertical panels of equal height, subtle gradient background, central surface panel with drop shadow. Maintain spacing, paddings, and alignment matching reference mock.

Hero Bar (top)

Left: UpNex logo text using PRIMARY color.
Center: Full-width search bar with placeholder “Search questions, topics, or users…”. Implement lightweight fuzzy search: tokenize title/body/tags, lowercase keywords, count matches, support partial matches using LIKE '%term%' or trigram similarity. Typing filters the center panel list live.
Right: circular avatar/menu icon button; clicking navigates to profile page. Use SURFACE background, PRIMARY border hover states.
Main Content Panels

Subject Navigation (left column)
Card on SURFACE with shadow, 24px padding.
Heading “Subject Navigation”.
Vertical list of subjects (Mathematics, Physics, Chemistry, Biology, History, Literature, Computer Science, Economics). Active selection highlighted with PRIMARY background and white text. Clicking filters questions by subject.
Section “Trending Tags” below with pill tags (calculus, quantum, algorithms, organic-chemistry, world-war, shakespeare, machine-learning, microeconomics). Pills toggle on/off; combine with subject/search filters.
Question Feed (center column)
Top filter chips aligned horizontally: Hot (default), New, Unanswered, Solved. Toggle states change query ordering/filter.
Question cards stacked vertically with 16px separation. Each card (SURFACE, 16px radius, subtle shadow) contains:
Left vertical vote control: up arrow, score, down arrow, using PRIMARY_DARK for active states.
Content block:
Bold title.
Optional excerpt body text.
Tag row: primary subject tag badge (PRIMARY background) plus status chip (Solved green, Unanswered amber via theme variations).
Meta row: answers count badge on left, time since posted, total answers text on right.
Clicking a card opens Question Detail page showing full question and answers.
Profile & Actions (right column)
Top CTA button “Ask a Question” (PRIMARY background, ACCENT hover) linking to Question Post page.
Profile card below: SURFACE panel with avatar, username, role subtitle, and metrics list (Questions Asked, Answers Given, Total Upvotes) plus “Member Since” date. Values pulled from user model. Entire card clickable to open profile page.
Interactions & States

All filters (subject, tag, top chips, search) combine to produce current question set.
Empty states show friendly message using TEXT_SECONDARY.
Hover/pressed states align with AppTheme colors.
Ensure keyboard navigation: search focus on load, Enter triggers search.
Responsive behavior: panels stack vertically on narrow widths but preserve order (search bar remains top).
Routing

Menu/avatar → Profile screen.
Ask a Question button → Post Question screen.
Question card → Question detail screen.
Deliver design and implementation following this specification, keeping colors, typography, and spacing consistent with AppTheme.

Overall Flow: App loads with UpNex home screen using AppTheme colors/fonts. Layout is three-column grid; all filters combine (subject, trending tags, toolbar chips, search) to produce visible question set. Responsive stacking for narrow widths while preserving flow (hero, subject panel, feed, profile).

Hero Section

Logo (navigates to home).
Search bar performs lightweight fuzzy search (tokenize title/body/tags, lowercase, match count, partial LIKE '%term%' or trigram). Results update feed live; Enter triggers search.
Avatar/menu button routes to profile page.
Left Panel: Subject Navigation

Select one subject at a time; selection instantly filters feed.
“Trending Tags” pills toggle on/off (multi-select) and refine feed further.
Center Panel: Question Feed
Each question card: up/down vote controls (persisted score), bold title, optional excerpt, primary subject badge, status chip, answer count badge, timestamp,. Card click opens Question Detail page (full content + answers).
Empty-state messaging when filters produce no results.
Right Panel: Profile & Actions

“Ask a Question” button → Post Question page.
Profile card shows avatar, username, role tagline, metrics (questions asked, answers given, total upvotes), member-since date; clicking card opens profile.
Supporting Rules

All navigation uses existing router; ensure back navigation from detail/post screens returns with filters intact.
Apply AppTheme colors (PRIMARY, PRIMARY_DARK, ACCENT, BACKGROUND, SURFACE) and fonts (Segoe UI base 14pt, headings 18pt bold). Rounded corners, soft shadows, gradients per theme.
Keyboard accessibility: tab order logical, Enter in search executes, buttons focusable.
Data bindings pull live stats for profile metrics and question metadata.

there is jdbc connector jar file in lib directory create a jdbc connection file the connects database: name = upnex, pass = hari, its in 127. local host ip. and also the data that are getting from this page should be present in the user table so make sure to alter according to that


based on the above info create a detailed road map with proper file names and structure in CurrentRoadmap.md make it like [ ] step 1 so its easy to mark as done after implementation