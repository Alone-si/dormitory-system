@echo off
chcp 65001 >nul
echo ========================================
echo 智能宿舍管理系统 - 测试数据导入工具
echo ========================================
echo.

REM 设置 MySQL 路径（请根据实际安装路径修改）
set MYSQL_PATH=C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe

REM 检查 MySQL 是否存在
if not exist "%MYSQL_PATH%" (
    echo [错误] 未找到 MySQL，请修改脚本中的 MYSQL_PATH 变量
    echo 当前路径: %MYSQL_PATH%
    echo.
    echo 常见路径:
    echo   C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe
    echo   C:\xampp\mysql\bin\mysql.exe
    echo   C:\wamp64\bin\mysql\mysql8.0.x\bin\mysql.exe
    echo.
    pause
    exit /b 1
)

echo [1/3] 准备导入测试数据...
echo 数据库: alone
echo 文件: backend\src\main\resources\test-data-enhanced.sql
echo.

echo [2/3] 请输入 MySQL root 密码:
"%MYSQL_PATH%" -u root -p alone < "backend\src\main\resources\test-data-enhanced.sql"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo [3/3] ✓ 测试数据导入成功！
    echo ========================================
    echo.
    echo 测试账号:
    echo   管理员: admin / 123456
    echo   学生(男): stu2024001 / 123456
    echo   学生(女): stu2024008 / 123456
    echo.
    echo 数据统计:
    echo   - 楼栋: 4 栋
    echo   - 房间: 14 间
    echo   - 用户: 21 人 (3管理员 + 18学生)
    echo   - 报修: 7 条
    echo   - 请假: 5 条
    echo   - 通知: 5 条
    echo.
) else (
    echo.
    echo ========================================
    echo [错误] 数据导入失败！
    echo ========================================
    echo.
    echo 可能的原因:
    echo   1. MySQL 密码错误
    echo   2. 数据库 'alone' 不存在
    echo   3. SQL 文件路径错误
    echo.
)

pause
