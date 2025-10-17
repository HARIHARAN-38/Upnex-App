param(
    [switch]$SkipTests,
    [switch]$SkipSchema
)

$ErrorActionPreference = "Stop"

# Allow the script to be executed from any location.
$repoRoot = (Resolve-Path -Path (Join-Path $PSScriptRoot '..')).ProviderPath
Set-Location $repoRoot

function Write-Info($message) {
    Write-Host "[$(Get-Date -Format "HH:mm:ss")] $message"
}

function Ensure-Tool([string]$toolName) {
    if (-not (Get-Command $toolName -ErrorAction SilentlyContinue)) {
        throw "Required tool '$toolName' is not available in PATH."
    }
}

function Get-DatabaseConfig([string]$path) {
    if (-not (Test-Path $path)) {
        throw "Database configuration file not found at $path."
    }

    $properties = @{}
    foreach ($line in Get-Content -Path $path) {
        $trimmed = $line.Trim()
        if ([string]::IsNullOrWhiteSpace($trimmed) -or $trimmed.StartsWith('#')) {
            continue
        }

        $parts = $trimmed -split '=', 2
        if ($parts.Length -eq 2) {
            $properties[$parts[0].Trim()] = $parts[1].Trim()
        }
    }

    return [pscustomobject]@{
        Host      = $properties['db.host']
        Port      = if ($properties['db.port']) { $properties['db.port'] } else { '3306' }
        Name      = $properties['db.name']
        User      = $properties['db.user']
        Password  = $properties['db.password']
    }
}

$script:mysqlCliPath = $null
$script:dbConfig = $null

function Invoke-MySqlCommand {
    param(
        [Parameter(Mandatory = $true)][string]$Query,
        [switch]$UseDatabase
    )

    if (-not $script:mysqlCliPath) {
        throw "MySQL CLI path not initialised."
    }
    if (-not $script:dbConfig) {
        throw "Database configuration not initialised."
    }

    $args = @(
        '-h', $script:dbConfig.Host,
        '-P', $script:dbConfig.Port,
        '-u', $script:dbConfig.User,
        '-N',
        '-B'
    )

    if ($script:dbConfig.Password) {
        $args += "-p$($script:dbConfig.Password)"
    }

    if ($UseDatabase) {
        $args += '-D'
        $args += $script:dbConfig.Name
    }

    $args += '-e'
    $args += $Query

    $output = & $script:mysqlCliPath @args
    if ($LASTEXITCODE -ne 0) {
        throw "MySQL command failed with exit code $LASTEXITCODE while executing: $Query"
    }

    return $output
}

function Invoke-MySqlScriptFile {
    param([Parameter(Mandatory = $true)][string]$FilePath)

    $scriptContent = Get-Content -Path $FilePath -Raw
    if ([string]::IsNullOrWhiteSpace($scriptContent)) {
        Write-Info "Skipping empty SQL script: $([System.IO.Path]::GetFileName($FilePath))"
        return
    }

    Invoke-MySqlCommand -Query $scriptContent -UseDatabase
}

function Test-TableExists {
    param([Parameter(Mandatory = $true)][string]$TableName)

    $query = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '$($script:dbConfig.Name)' AND table_name = '$TableName';"
    $result = Invoke-MySqlCommand -Query $query
    if (-not $result) {
        return $false
    }

    [int]$count = $result.Trim()
    return $count -gt 0
}

function Ensure-DatabaseSchema {
    $schemaDir = Join-Path $repoRoot 'sql'
    if (-not (Test-Path $schemaDir)) {
        Write-Info "Schema directory not found at $schemaDir. Skipping schema validation."
        return
    }

    if (-not $script:dbConfig.Name) {
        throw "Database name is not configured in config\\database.properties."
    }

    $missingSettings = @()
    if (-not $script:dbConfig.Host) { $missingSettings += 'db.host' }
    if (-not $script:dbConfig.User) { $missingSettings += 'db.user' }

    if ($missingSettings.Count -gt 0) {
        $settingList = $missingSettings -join ', '
        throw "Database configuration is incomplete. Missing values for: $settingList"
    }

    Write-Info "Ensuring database '$($script:dbConfig.Name)' exists"
    Invoke-MySqlCommand -Query "CREATE DATABASE IF NOT EXISTS $($script:dbConfig.Name);"

    $sqlFiles = Get-ChildItem -Path $schemaDir -Filter '*.sql' | Sort-Object Name
    foreach ($sqlFile in $sqlFiles) {
        Write-Info "Applying schema script: $($sqlFile.Name)"
        Invoke-MySqlScriptFile -FilePath $sqlFile.FullName
    }

    $expectedTables = @(
        'users',
        'skills',
        'subjects',
        'tags',
        'questions',
        'question_tags',
        'answers'
    )

    $missingTables = @()
    foreach ($table in $expectedTables) {
        if (-not (Test-TableExists -TableName $table)) {
            $missingTables += $table
        }
    }

    if ($missingTables.Count -gt 0) {
        $missingList = $missingTables -join ', '
        throw "Schema verification failed. Missing tables: $missingList"
    }

    Write-Info "All required tables are present in database '$($script:dbConfig.Name)'."
}

Write-Info "Checking required tooling"
Ensure-Tool -toolName "mvn"
Ensure-Tool -toolName "java"
Ensure-Tool -toolName "mysql"
$script:mysqlCliPath = (Get-Command "mysql").Source

$configFile = Join-Path $repoRoot "config\database.properties"
if (-not (Test-Path $configFile)) {
    $sampleConfig = Join-Path $repoRoot "config\database.properties.sample"
    if (Test-Path $sampleConfig) {
        Copy-Item -Path $sampleConfig -Destination $configFile -Force
        Write-Info "Created config\\database.properties from the sample file. Update it with your credentials."
    } else {
        throw "Database configuration file is missing (config\\database.properties)."
    }
}

$script:dbConfig = Get-DatabaseConfig -path $configFile

if (-not $SkipSchema) {
    Write-Info "Ensuring database schema"
    Ensure-DatabaseSchema
}

$binPath = Join-Path $repoRoot "bin"
if (Test-Path $binPath) {
    Write-Info "Removing legacy bin directory"
    Remove-Item -Path $binPath -Recurse -Force
}

Write-Info "Cleaning and building the project"
$mvnArgs = @(
    "clean",
    "package",
    "dependency:copy-dependencies",
    "-DincludeScope=runtime",
    "-DoutputDirectory=target/lib"
)
if ($SkipTests) {
    $mvnArgs += "-DskipTests"
}
& mvn @mvnArgs

$jar = Get-ChildItem -Path (Join-Path $repoRoot "target") -Filter "*.jar" |
    Where-Object { $_.Name -notmatch "(sources|javadoc)" } |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1

if (-not $jar) {
    throw "Packaged application JAR not found under target/."
}

$classpath = "$($jar.FullName);$repoRoot\target\lib\*"

Write-Info "Launching UpNext App"
& java -cp $classpath com.upnext.app.App
