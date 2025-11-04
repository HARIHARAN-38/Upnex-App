# UpNext App

A desktop Q&A application built with Java Swing. UpNext lets users sign in, browse and search questions, ask new questions with tags, read details, post answers, and vote on both questions and answers.

## Highlights

- Clean, modern Swing UI (FlatLaf) with a top hero bar for search and navigation
- Question feed with filters (New, Hot, Unanswered, Solved)
- Full question details page with tags, author, vote panel, answer list, and related questions
- Rich answer input with validation, character counter, and success feedback
- Voting with shared reusable component and consistent icons across the app
- Profile area and skill management (legacy feature retained)
- MySQL-backed repository layer with schema bootstrap on first run

## Tech Stack

- Language: Java (JDK 21)
- UI: Swing + FlatLaf
- Build: Maven
- Database: MySQL 8.x via MySQL Connector/J
- Testing: JUnit 5

## Getting Started

### 1) Configure the database

Copy the sample config and adjust values as needed (Windows PowerShell):

```powershell
Copy-Item config\database.properties.sample config\database.properties -Force
```

Edit `config/database.properties` if your credentials differ (host/port/db/user/password). The app will initialize the schema on first launch using the SQL files under `sql/` and `src/main/resources/db/`.

Create an empty database (if it doesn’t exist yet):

```sql
CREATE DATABASE upnex;
```

### 2) Run the app

```powershell
# build (skip tests while getting started)
mvn -q -DskipTests compile

# launch
mvn -q exec:java
```

The first run will connect to MySQL and apply schema migrations. Logs are written to the `logs/` folder.

### 3) Package a runnable JAR

```powershell
mvn -q -DskipTests package
```

The JAR will be in `target/`. You’ll need the `lib` folder on the classpath for third‑party dependencies.

## Theme

The application uses a centralized theme in `com.upnext.app.ui.theme.AppTheme`.
See `docs/AppThemePalette.md` for palette and typography.

## Folder Structure (top level)

```
CurrentRoadmap.md
pom.xml
Readme.md
config/
docs/
lib/
logs/
scripts/
sql/
src/
```

### Source code layout

```
src/main/java/com/upnext/app/
  App.java                   # Application entry point
  config/                    # App configuration and constants
  core/                      # Logger and helpers
  data/                      # Repositories and DB bootstrap
    JdbcConnectionProvider.java
    SchemaInitializer.java
    question/                # Question/Answer/Tag repositories
    validation/
  domain/                    # Domain entities and DTOs
    question/                # Question, Answer, Vote, Subject, etc.
  service/                   # Business services (Auth, Search, etc.)
  ui/                        # UI components and screens
    components/              # Reusable widgets (VotePanel, AnswerInputPanel…)
    navigation/
    screens/                 # SignIn, Home, AddQuestion, QuestionDetail, Profile…
    theme/                   # AppTheme colors/typography

src/main/resources/ui/icons/ # Up/down vote icons and other assets
src/main/resources/db/       # Embedded schema resources
sql/                         # SQL migration scripts (executed on startup)
```

## Key Screens & Flows

- SignInScreen – Email/password sign‑in and navigation to Home on success.
- HomeScreen – Three‑column layout (subjects/tags • question feed • profile), with filters.
- AddQuestionScreen – Title, description, tags; input validation and persistence.
- QuestionDetailScreen – Full question view, tags, author, VotePanel, answers list, related questions.
- ProfileScreen / SkillsetScreen – User profile and legacy skill management.

## Logging

Logs are stored in the `logs/` directory at the repo root.

## Testing

```powershell
mvn -q test
```

See `docs/TestPlan.md` for additional notes.

## Documentation

- `docs/TestPlan.md` – Test notes and procedures
- `docs/AppThemePalette.md` – Theme palette and typography
- `docs/SkillsUserGuide.md` – Legacy skills feature
- `docs/*Step*_Summary.md` – Iterative design and engineering notes

## Troubleshooting

- DB connection errors: verify `config/database.properties` and that the `upnex` DB exists.
- Tables missing: check logs; the schema is created at startup from `sql/`.
- Icons look blurry on HiDPI: adjust icon size constants in `VotePanel`/`QuestionCard`.

---

If you’re setting this up on Windows PowerShell, the commands above are copy‑paste ready.
