# UpNext App

A Java desktop application for personal task and schedule management with secure user authentication and professional skills tracking.

## Overview

UpNext is a desktop application built with Java Swing that allows users to:

- Create and manage personal accounts with professional skills
- Record and visualize skill proficiency levels
- Access a customized dashboard
- View and manage tasks (coming soon)
- Track schedules and events (coming soon)

## System Requirements

- Java Development Kit (JDK) 17 or newer
- MySQL 8.0 or newer
- Minimum 2GB RAM
- Minimum 100MB disk space

## Setup Instructions

### 1. Database Setup

1. Install MySQL 8.0 or newer if not already installed
2. Create a new database:
```sql
CREATE DATABASE upnext;
```
3. Use the following credentials for the application:
   - Database: `upnext`
   - Username: `root`
   - Password: `hari`
   - Host: `127.0.0.1`
   - Port: `3306` (default)

4. Run the database schema script:
```sql
-- Users Table
CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    salt VARCHAR(64) NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Skills Table
CREATE TABLE IF NOT EXISTS skills (
    skill_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    skill_name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    proficiency_level INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

### 2. Application Setup

1. Clone the repository:
```
git clone https://github.com/yourusername/upnext-app.git
```

2. Ensure the JDBC driver is available in the `lib` directory
   - The application uses MySQL Connector/J
   - If not present, download from https://dev.mysql.com/downloads/connector/j/

3. Compile the application:
```
javac -d bin -cp "lib/*" src/main/java/com/upnext/app/**/*.java
```

4. Run the application:
```
java -cp "bin:lib/*" com.upnext.app.App
```

### 3. Theme Configuration

The application uses a custom theme defined in `com.upnext.app.ui.theme.AppTheme`. 
The theme includes:

- Custom color palette (see `docs/AppThemePalette.md`)
- Custom typography with system-native and fallback fonts
- Consistent component styling

## Development

### Project Structure

- `src/main/java/com/upnext/app/` - Application source code
  - `App.java` - Main entry point
  - `config/` - Configuration classes
  - `core/` - Core utilities
  - `data/` - Data access layer
    - `JdbcConnectionProvider.java` - Database connection manager
    - `UserRepository.java` - User data management
    - `SkillRepository.java` - Skill data management
    - `SchemaInitializer.java` - Database schema setup
  - `domain/` - Domain models
    - `User.java` - User entity
    - `Skill.java` - Skill entity
  - `service/` - Business logic
    - `AuthService.java` - Authentication logic
    - `SkillService.java` - Skill management logic
  - `ui/` - User interface components
    - `components/` - Reusable UI components
      - `forms/` - Form-related components
      - `SkillCard.java` - Skill display component
      - `SkillCardPanel.java` - Container for skill cards
      - `ProficiencyBar.java` - Visual proficiency indicator
    - `screens/` - Application screens
      - `SignInScreen.java` - User sign-in
      - `CreateAccountScreen.java` - Initial account setup
      - `SkillsetScreen.java` - Skill management screen
      - `SkillAddScreen.java` - Add new skill screen
      - `HomeScreen.java` - Main application dashboard

### Logging

Logs are stored in the `logs/` directory in the application's root folder.

## Testing

See `docs/TestPlan.md` for manual testing procedures.

## Documentation

- `docs/TestPlan.md` - Comprehensive testing procedures for the application
- `docs/AppThemePalette.md` - Color palette and theme documentation
- `docs/SkillsUserGuide.md` - Guide for using the skills management feature
- `docs/UI_Refinements_Summary.md` - UI improvement details and screenshots

## License

This project is licensed under the MIT License - see the LICENSE file for details.
