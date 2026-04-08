@echo off
REM 数据库恢复脚本
REM 使用方法：双击运行或在命令行执行

echo ========================================
echo 智能宿舍管理系统 - 数据库恢复工具
echo ========================================
echo.

REM 设置变量
set DB_NAME=alone
set DB_USER=root
set BACKUP_DIR=backup

echo 可用的备份文件：
echo.
dir /b %BACKUP_DIR%\*.sql
echo.

set /p BACKUP_FILE=请输入要恢复的备份文件名（不含路径）: 

if not exist %BACKUP_DIR%\%BACKUP_FILE% (
    echo.
    echo 错误：文件不存在！
    echo.
    pause
    exit /b 1
)

echo.
echo 警告：此操作将覆盖当前数据库中的所有数据！
set /p CONFIRM=确定要继续吗？(Y/N): 

if /i not "%CONFIRM%"=="Y" (
    echo.
    echo 操作已取消。
    echo.
    pause
    exit /b 0
)

echo.
echo 正在恢复数据库...
echo 数据库名: %DB_NAME%
echo 备份文件: %BACKUP_DIR%\%BACKUP_FILE%
echo.

REM 执行恢复
mysql -u %DB_USER% -p %DB_NAME% < %BACKUP_DIR%\%BACKUP_FILE%

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo 恢复成功！
    echo ========================================
) else (
    echo.
    echo ========================================
    echo 恢复失败！请检查：
    echo 1. MySQL是否正在运行
    echo 2. 用户名和密码是否正确
    echo 3. 数据库是否已创建
    echo 4. 备份文件是否完整
    echo ========================================
)

echo.
pause
