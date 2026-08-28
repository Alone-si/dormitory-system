# Smart Dorm System - Import Test Data
# UTF-8 Encoding

Write-Host "========================================"
Write-Host "Smart Dorm System - Data Import Tool"
Write-Host "========================================"
Write-Host ""

# Find MySQL
$mysqlPaths = @(
    "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe",
    "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe",
    "C:\xampp\mysql\bin\mysql.exe",
    "C:\wamp64\bin\mysql\mysql8.0.39\bin\mysql.exe"
)

$mysqlPath = $null
foreach ($path in $mysqlPaths) {
    if (Test-Path $path) {
        $mysqlPath = $path
        break
    }
}

if (-not $mysqlPath) {
    $mysqlCmd = Get-Command mysql -ErrorAction SilentlyContinue
    if ($mysqlCmd) {
        $mysqlPath = $mysqlCmd.Source
    }
}

if (-not $mysqlPath) {
    Write-Host "[ERROR] MySQL not found!" -ForegroundColor Red
    Write-Host ""
    Write-Host "Manual import method:" -ForegroundColor Yellow
    Write-Host "  1. Open Navicat/DataGrip/MySQL Workbench"
    Write-Host "  2. Connect to database 'alone'"
    Write-Host "  3. Open file: backend\src\main\resources\test-data-enhanced.sql"
    Write-Host "  4. Execute the SQL script"
    Write-Host ""
    pause
    exit 1
}

Write-Host "[OK] MySQL found: $mysqlPath" -ForegroundColor Green
Write-Host ""

$sqlFile = "backend\src\main\resources\test-data-enhanced.sql"
if (-not (Test-Path $sqlFile)) {
    Write-Host "[ERROR] SQL file not found: $sqlFile" -ForegroundColor Red
    pause
    exit 1
}

Write-Host "Database: alone"
Write-Host "SQL File: $sqlFile"
Write-Host ""
Write-Host "Please enter MySQL root password:"
$password = Read-Host -AsSecureString
$plainPassword = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [Runtime.InteropServices.Marshal]::SecureStringToBSTR($password)
)

Write-Host ""
Write-Host "Importing data..." -ForegroundColor Yellow

try {
    $env:MYSQL_PWD = $plainPassword
    & $mysqlPath -u root alone --default-character-set=utf8mb4 -e "source $sqlFile"
    $exitCode = $LASTEXITCODE
    $env:MYSQL_PWD = $null
    
    if ($exitCode -eq 0) {
        Write-Host ""
        Write-Host "========================================"
        Write-Host "SUCCESS! Data imported successfully!"
        Write-Host "========================================"
        Write-Host ""
        Write-Host "Test Accounts:"
        Write-Host "  Admin: admin / 123456"
        Write-Host "  Student(Male): stu2024001 / 123456"
        Write-Host "  Student(Female): stu2024008 / 123456"
        Write-Host ""
        Write-Host "Data Summary:"
        Write-Host "  - Buildings: 4"
        Write-Host "  - Rooms: 14"
        Write-Host "  - Users: 21 (3 admins + 18 students)"
        Write-Host "  - Repairs: 7"
        Write-Host "  - Leaves: 5"
        Write-Host "  - Notices: 5"
        Write-Host ""
        Write-Host "Visit http://localhost:5173 to test!" -ForegroundColor Green
        Write-Host ""
    } else {
        throw "MySQL exit code: $exitCode"
    }
} catch {
    Write-Host ""
    Write-Host "========================================"
    Write-Host "ERROR! Import failed!"
    Write-Host "========================================"
    Write-Host ""
    Write-Host "Error: $_" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Possible reasons:"
    Write-Host "  1. Wrong MySQL password"
    Write-Host "  2. Database 'alone' does not exist"
    Write-Host "  3. SQL file format error"
    Write-Host "  4. Insufficient permissions"
    Write-Host ""
} finally {
    $env:MYSQL_PWD = $null
}

pause
