@echo off
setlocal enabledelayedexpansion

echo ===========================
echo UpNext Application Launcher
echo ===========================

:: Set paths
set APP_ROOT=%~dp0
set SRC_DIR=%APP_ROOT%src\main\java
set BIN_DIR=%APP_ROOT%bin
set LIB_DIR=%APP_ROOT%lib
set DB_SCRIPT=%APP_ROOT%src\main\resources\db\schema.sql
set MAIN_CLASS=com.upnext.app.App
set POM_FILE=%APP_ROOT%pom.xml
set VERIFY_SCRIPT=%APP_ROOT%src\main\resources\db\verify_tables.sql

:: Database credentials
set DB_HOST=127.0.0.1
set DB_PORT=3306
set DB_NAME=upnext
set DB_USER=root
set DB_PASS=hari

echo Setting up environment...

:: Create bin directory if it doesn't exist
if not exist "%BIN_DIR%" mkdir "%BIN_DIR%"

:: Create logs directory if it doesn't exist
if not exist "%APP_ROOT%logs" mkdir "%APP_ROOT%logs"

:: Clean previous build
echo Cleaning previous build...
if exist "%BIN_DIR%" (
    del /q /s "%BIN_DIR%\*" > nul 2>&1
)

:: Check for MySQL JDBC driver
echo Checking for JDBC driver...
set JDBC_FOUND=0
for %%F in ("%LIB_DIR%\mysql-connector-*.jar") do (
    set JDBC_FOUND=1
)

if !JDBC_FOUND! == 0 (
    echo WARNING: MySQL JDBC driver not found in lib directory
    echo Attempting to download MySQL JDBC driver...
    
    :: Create lib directory if it doesn't exist
    if not exist "%LIB_DIR%" mkdir "%LIB_DIR%"
    
    :: Try to download JDBC driver using PowerShell
    powershell -Command "& {Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.28/mysql-connector-java-8.0.28.jar' -OutFile '%LIB_DIR%\mysql-connector-java-8.0.28.jar'}"
    
    if %ERRORLEVEL% neq 0 (
        echo ERROR: Failed to download MySQL JDBC driver
        echo Please download it from https://dev.mysql.com/downloads/connector/j/
        echo and place it in the lib directory
        goto :exit
    ) else (
        echo MySQL JDBC driver downloaded successfully
        set JDBC_FOUND=1
    )
)

:: Build classpath
set "CLASSPATH=%BIN_DIR%"
for %%F in ("%LIB_DIR%\*.jar") do (
    set "CLASSPATH=!CLASSPATH!;%%~fF"
)

echo Verifying database...

:: Create database if it doesn't exist
echo -- Checking database existence...
set DB_FOUND=0
echo CREATE DATABASE IF NOT EXISTS %DB_NAME%; > "%TEMP%\create_db.sql"

:: Try with mysql client if available
mysql --version > nul 2>&1
if %ERRORLEVEL% equ 0 (
    mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASS% < "%TEMP%\create_db.sql" > nul 2>&1
    if %ERRORLEVEL% equ 0 (
        set DB_FOUND=1
        echo -- Database verified with MySQL client.
    )
)

:: If mysql client not available, verify using Java
if !DB_FOUND! equ 0 (
    echo -- MySQL client not found, verifying database using Java...
    
    :: Ensure the JDBC driver is in classpath for compilation
    set JDBC_CP=
    for %%F in ("%LIB_DIR%\mysql-connector-*.jar") do (
        set "JDBC_CP=%%~fF"
    )

    if "!JDBC_CP!"=="" (
        echo ERROR: JDBC driver not found for Java-based database verification
        goto :exit
    )
    
    :: Create a temporary Java class to verify database
    > "%TEMP%\VerifyDatabase.java" (
    echo import java.sql.*;
    echo public class VerifyDatabase {
    echo     public static void main^(String[] args^) {
    echo         try {
    echo             Connection conn = DriverManager.getConnection^("jdbc:mysql://%DB_HOST%:%DB_PORT%/?createDatabaseIfNotExist=true", "%DB_USER%", "%DB_PASS%"^);
    echo             Statement stmt = conn.createStatement^(^);
    echo             stmt.execute^("CREATE DATABASE IF NOT EXISTS %DB_NAME%"^);
    echo             System.out.println^("Database verified successfully"^);
    echo             conn.close^(^);
    echo         } catch^(Exception e^) {
    echo             System.err.println^("Database verification failed: " + e.getMessage^(^)^);
    echo             System.exit^(1^);
    echo         }
    echo     }
    echo }
    )
    
    :: Compile and run the verification class
    javac -cp "!JDBC_CP!" "%TEMP%\VerifyDatabase.java"
    if %ERRORLEVEL% equ 0 (
        java -cp "!JDBC_CP!;%TEMP%" VerifyDatabase
    ) else (
        echo -- Failed to compile database verification class
        set DB_FOUND=0
    )
    
    if %ERRORLEVEL% equ 0 (
        set DB_FOUND=1
        echo -- Database verified with Java.
    )
)

if !DB_FOUND! equ 0 (
    echo ERROR: Could not connect to database or create it
    echo Please ensure MySQL is running and credentials are correct
    echo Host: %DB_HOST%, Port: %DB_PORT%, User: %DB_USER%
    goto :exit
)

:: Verify and initialize schema
echo Verifying database schema...

:: Create a temporary SQL script to verify tables
(
echo SELECT COUNT(TABLE_NAME) as table_count FROM information_schema.TABLES
echo WHERE TABLE_SCHEMA = '%DB_NAME%' AND TABLE_NAME = 'users';
) > "%TEMP%\check_tables.sql"

set TABLES_EXIST=0

:: Try with mysql client
mysql --version > nul 2>&1
if %ERRORLEVEL% equ 0 (
    set "TABLES_RESULT="
    for /f "usebackq delims=" %%i in (`mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASS% -N -B ^< "%TEMP%\check_tables.sql"`) do set "TABLES_RESULT=%%i"
    if defined TABLES_RESULT (
        for /f "delims=" %%j in ("!TABLES_RESULT!") do (
            for /f "delims=0123456789" %%k in ("%%j") do set "TABLES_RESULT="
            if defined TABLES_RESULT (
                if %%j GTR 0 (
                    set TABLES_EXIST=1
                ) else (
                    set TABLES_EXIST=0
                )
            )
        )
    )
) else (
    :: Create a temporary Java class to check tables
    > "%TEMP%\CheckTables.java" (
    echo import java.sql.*;
    echo public class CheckTables {
    echo     public static void main^(String[] args^) {
    echo         try {
    echo             Connection conn = DriverManager.getConnection^("jdbc:mysql://%DB_HOST%:%DB_PORT%/%DB_NAME%", "%DB_USER%", "%DB_PASS%"^);
    echo             Statement stmt = conn.createStatement^(^);
    echo             ResultSet rs = stmt.executeQuery^("SELECT COUNT^(TABLE_NAME^) as table_count FROM information_schema.TABLES WHERE TABLE_SCHEMA = '%DB_NAME%' AND TABLE_NAME = 'users'"^);
    echo             if^(rs.next^(^) ^&^& rs.getInt^("table_count"^) ^> 0^) {
    echo                 System.out.println^("1"^);
    echo             } else {
    echo                 System.out.println^("0"^);
    echo             }
    echo             conn.close^(^);
    echo         } catch^(Exception e^) {
    echo             System.err.println^("Table check failed: " + e.getMessage^(^)^);
    echo             System.exit^(1^);
    echo         }
    echo     }
    echo }
    )
    
    :: Compile and run the table check class
    javac -cp "!JDBC_CP!" "%TEMP%\CheckTables.java"
    
    if %ERRORLEVEL% equ 0 (
        set "TABLES_RESULT="
        for /f "usebackq delims=" %%i in (`java -cp "!JDBC_CP!;%TEMP%" CheckTables`) do set "TABLES_RESULT=%%i"
        if defined TABLES_RESULT (
            for /f "delims=" %%j in ("!TABLES_RESULT!") do (
                if %%j GTR 0 (
                    set TABLES_EXIST=1
                ) else (
                    set TABLES_EXIST=0
                )
            )
        )
    ) else (
        echo -- Failed to compile table check class
        set TABLES_EXIST=0
    )
)

:: Initialize schema if needed
if !TABLES_EXIST! equ 1 (
    echo -- Database schema already initialized. Skipping schema creation.
) else (
    echo -- Initializing database schema...
    
    :: Try with mysql client
    mysql --version > nul 2>&1
    if %ERRORLEVEL% equ 0 (
        mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASS% %DB_NAME% < "%DB_SCRIPT%"
        if %ERRORLEVEL% neq 0 (
            echo WARNING: Error initializing schema with MySQL client
            set SCHEMA_INIT_FAILED=1
        ) else (
            echo -- Schema initialized successfully with MySQL client.
        )
    ) else (
        :: Create a temporary Java class to initialize schema
        echo -- MySQL client not found, initializing schema using Java...
    > "%TEMP%\InitSchema.java" (
    echo import java.sql.*;
    echo import java.nio.file.*;
    echo public class InitSchema {
    echo     public static void main^(String[] args^) {
    echo         try {
    echo             Connection conn = DriverManager.getConnection^("jdbc:mysql://%DB_HOST%:%DB_PORT%/%DB_NAME%", "%DB_USER%", "%DB_PASS%"^);
    echo             Statement stmt = conn.createStatement^(^);
    echo             String scriptPath = "%DB_SCRIPT%";
    echo             scriptPath = scriptPath.replace^("\\", "/"^);
    echo             String sql = new String^(Files.readAllBytes^(Paths.get^(scriptPath^)^)^);
    echo             for^(String statement : sql.split^(";"^)^) {
    echo                 statement = statement.trim^(^);
    echo                 if^(!statement.isEmpty^(^)^) {
    echo                     stmt.execute^(statement^);
    echo                 }
    echo             }
    echo             System.out.println^("Schema initialized successfully"^);
    echo             conn.close^(^);
    echo         } catch^(Exception e^) {
    echo             System.err.println^("Schema initialization failed: " + e.getMessage^(^)^);
    echo             System.exit^(1^);
    echo         }
    echo     }
    echo }
    )
        
        :: Compile and run the schema initialization class
        javac -cp "!JDBC_CP!" "%TEMP%\InitSchema.java"
        if %ERRORLEVEL% equ 0 (
            java -cp "!JDBC_CP!;%TEMP%" InitSchema
        ) else (
            echo -- Failed to compile schema initialization class
            set SCHEMA_INIT_FAILED=1
        )
        
        if %ERRORLEVEL% neq 0 (
            echo WARNING: Error initializing schema with Java
            set SCHEMA_INIT_FAILED=1
        ) else (
            echo -- Schema initialized successfully with Java.
        )
    )
)

:: Compile the application using javac
echo Compiling application...
set COMPILE_SUCCESS=0

:: Find all Java files recursively and compile them
set JAVA_FILES=
for /r "%SRC_DIR%\com\upnext\app" %%F in (*.java) do (
    set "JAVA_FILES=!JAVA_FILES! "%%~fF""
)

if defined JAVA_FILES (
    javac -d "%BIN_DIR%" -cp "!CLASSPATH!" !JAVA_FILES!
    if %ERRORLEVEL% equ 0 (
        set COMPILE_SUCCESS=1
        echo -- Compilation successful with javac.
    ) else (
        echo -- Compilation with javac failed, trying alternative methods...
    )
) else (
    echo -- No Java files found to compile.
    echo -- Trying alternative methods...
)

:: Try Maven if javac failed and pom.xml exists
if !COMPILE_SUCCESS! equ 0 (
    if exist "%POM_FILE%" (
        echo -- Attempting to compile with Maven...
        
        :: Check if Maven is in path
        where mvn >nul 2>nul
        if %ERRORLEVEL% equ 0 (
            call mvn clean compile -f "%POM_FILE%"
            if %ERRORLEVEL% equ 0 (
                set COMPILE_SUCCESS=1
                echo -- Compilation successful with Maven.
                
                :: Copy Maven compiled classes to our bin directory
                xcopy /E /Y "%APP_ROOT%target\classes\*.*" "%BIN_DIR%\"
            ) else (
                echo -- Maven compilation failed.
            )
        ) else (
            :: Try to find Maven in common locations
            set MAVEN_FOUND=0
            
            if exist "C:\Program Files\Apache Maven\bin\mvn.cmd" (
                echo -- Found Maven in Program Files...
                call "C:\Program Files\Apache Maven\bin\mvn.cmd" clean compile -f "%POM_FILE%"
                if %ERRORLEVEL% equ 0 (
                    set COMPILE_SUCCESS=1
                    set MAVEN_FOUND=1
                    echo -- Compilation successful with Maven.
                    
                    :: Copy Maven compiled classes to our bin directory
                    xcopy /E /Y "%APP_ROOT%target\classes\*.*" "%BIN_DIR%\"
                )
            )
            
            if !MAVEN_FOUND! equ 0 (
                echo -- Maven not found in PATH. Manual compilation required.
                echo -- Attempting manual compilation...
                
                :: Create a simple manual compilation as fallback
                set MANUAL_JAVA_FILES=
                for /r "%SRC_DIR%" %%F in (*.java) do (
                    set "MANUAL_JAVA_FILES=!MANUAL_JAVA_FILES! "%%~fF""
                )
                
                javac -d "%BIN_DIR%" -cp "!CLASSPATH!" !MANUAL_JAVA_FILES!
                if %ERRORLEVEL% equ 0 (
                    set COMPILE_SUCCESS=1
                    echo -- Manual compilation successful.
                ) else (
                    echo -- Manual compilation failed.
                )
            )
        )
    )
)

:: If all compilation methods failed
if !COMPILE_SUCCESS! equ 0 (
    echo ERROR: All compilation methods failed
    goto :exit
)

:: Run the application
echo Starting UpNext application...
java -cp "!CLASSPATH!" -Dfile.encoding=UTF-8 %MAIN_CLASS%
if %ERRORLEVEL% neq 0 (
    echo ERROR: Application terminated with error code %ERRORLEVEL%
)

:exit
:: Clean up temporary files
if exist "%TEMP%\VerifyDatabase.java" del "%TEMP%\VerifyDatabase.java"
if exist "%TEMP%\VerifyDatabase.class" del "%TEMP%\VerifyDatabase.class"
if exist "%TEMP%\CheckTables.java" del "%TEMP%\CheckTables.java"
if exist "%TEMP%\CheckTables.class" del "%TEMP%\CheckTables.class"
if exist "%TEMP%\InitSchema.java" del "%TEMP%\InitSchema.java"
if exist "%TEMP%\InitSchema.class" del "%TEMP%\InitSchema.class"
if exist "%TEMP%\create_db.sql" del "%TEMP%\create_db.sql"
if exist "%TEMP%\check_tables.sql" del "%TEMP%\check_tables.sql"

echo ===========================
endlocal