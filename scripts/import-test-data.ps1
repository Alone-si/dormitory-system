# 智能宿舍管理系统 - 测试数据导入工具 (PowerShell)
# 编码: UTF-8

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "智能宿舍管理系统 - 测试数据导入工具" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# MySQL 可能的路径
$mysqlPaths = @(
    "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe",
    "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe",
    "C:\xampp\mysql\bin\mysql.exe",
    "C:\wamp64\bin\mysql\mysql8.0.39\bin\mysql.exe",
    "D:\MySQL\bin\mysql.exe"
)

# 查找 MySQL
$mysqlPath = $null
foreach ($path in $mysqlPaths) {
    if (Test-Path $path) {
        $mysqlPath = $path
        break
    }
}

# 如果没找到，尝试从 PATH 中查找
if (-not $mysqlPath) {
    $mysqlCmd = Get-Command mysql -ErrorAction SilentlyContinue
    if ($mysqlCmd) {
        $mysqlPath = $mysqlCmd.Source
    }
}

if (-not $mysqlPath) {
    Write-Host "[错误] 未找到 MySQL" -ForegroundColor Red
    Write-Host ""
    Write-Host "请手动指定 MySQL 路径，或将 MySQL 添加到系统 PATH" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "手动导入方法:" -ForegroundColor Green
    Write-Host "  1. 打开 Navicat/DataGrip/MySQL Workbench" -ForegroundColor Gray
    Write-Host "  2. 连接到数据库 'alone'" -ForegroundColor Gray
    Write-Host "  3. 打开文件: backend\src\main\resources\test-data-enhanced.sql" -ForegroundColor Gray
    Write-Host "  4. 执行 SQL 脚本" -ForegroundColor Gray
    Write-Host ""
    pause
    exit 1
}

Write-Host "[✓] 找到 MySQL: $mysqlPath" -ForegroundColor Green
Write-Host ""

# 检查 SQL 文件
$sqlFile = "backend\src\main\resources\test-data-enhanced.sql"
if (-not (Test-Path $sqlFile)) {
    Write-Host "[错误] SQL 文件不存在: $sqlFile" -ForegroundColor Red
    pause
    exit 1
}

Write-Host "[1/3] 准备导入测试数据..." -ForegroundColor Yellow
Write-Host "  数据库: alone" -ForegroundColor Gray
Write-Host "  文件: $sqlFile" -ForegroundColor Gray
Write-Host ""

# 获取密码
Write-Host "[2/3] 请输入 MySQL root 密码:" -ForegroundColor Yellow
$password = Read-Host -AsSecureString
$plainPassword = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [Runtime.InteropServices.Marshal]::SecureStringToBSTR($password)
)

# 执行导入
Write-Host ""
Write-Host "正在导入..." -ForegroundColor Yellow

try {
    $env:MYSQL_PWD = $plainPassword
    & $mysqlPath -u root alone --default-character-set=utf8mb4 -e "source $sqlFile" 2>&1 | Out-Null
    $env:MYSQL_PWD = $null
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "========================================" -ForegroundColor Green
        Write-Host "[3/3] ✓ 测试数据导入成功！" -ForegroundColor Green
        Write-Host "========================================" -ForegroundColor Green
        Write-Host ""
        
        Write-Host "测试账号:" -ForegroundColor Cyan
        Write-Host "  管理员: admin / 123456" -ForegroundColor White
        Write-Host "  学生(男): stu2024001 / 123456" -ForegroundColor White
        Write-Host "  学生(女): stu2024008 / 123456" -ForegroundColor White
        Write-Host ""
        
        Write-Host "数据统计:" -ForegroundColor Cyan
        Write-Host "  - 楼栋: 4 栋" -ForegroundColor White
        Write-Host "  - 房间: 14 间" -ForegroundColor White
        Write-Host "  - 用户: 21 人 (3管理员 + 18学生)" -ForegroundColor White
        Write-Host "  - 报修: 7 条" -ForegroundColor White
        Write-Host "  - 请假: 5 条" -ForegroundColor White
        Write-Host "  - 通知: 5 条" -ForegroundColor White
        Write-Host ""
        
        Write-Host "现在可以访问 http://localhost:5173 进行测试！" -ForegroundColor Green
        Write-Host ""
    } else {
        throw "MySQL 返回错误代码: $LASTEXITCODE"
    }
} catch {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "[错误] 数据导入失败！" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    Write-Host ""
    Write-Host "错误信息: $_" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "可能的原因:" -ForegroundColor Yellow
    Write-Host "  1. MySQL 密码错误" -ForegroundColor Gray
    Write-Host "  2. 数据库 'alone' 不存在" -ForegroundColor Gray
    Write-Host "  3. SQL 文件格式错误" -ForegroundColor Gray
    Write-Host "  4. 权限不足" -ForegroundColor Gray
    Write-Host ""
} finally {
    $env:MYSQL_PWD = $null
}

pause
