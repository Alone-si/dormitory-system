@echo off
REM 数据库备份脚本
REM 使用方法：双击运行或在命令行执行

echo ========================================
echo 智能宿舍管理系统 - 数据库备份工具
echo ========================================
echo.

REM 设置变量
set DB_NAME=alone
set DB_USER=root
set BACKUP_DIR=backup
set TIMESTAMP=%date:~0,4%%date:~5,2%%date:~8,2%_%time:~0,2%%time:~3,2%%time:~6,2%
set TIMESTAMP=%TIMESTAMP: =0%
set BACKUP_FILE=%BACKUP_DIR%\%DB_NAME%_%TIMESTAMP%.sql

REM 创建备份目录
if not exist %BACKUP_DIR% mkdir %BACKUP_DIR%

echo 正在备份数据库...
echo 数据库名: %DB_NAME%
echo 备份文件: %BACKUP_FILE%
echo.

REM 执行备份
mysqldump -u %DB_USER% -p %DB_NAME% > %BACKUP_FILE%

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo 备份成功！
    echo 备份文件位置: %BACKUP_FILE%
    echo ========================================
) else (
    echo.
    echo ========================================
    echo 备份失败！请检查：
    echo 1. MySQL是否正在运行
    echo 2. 用户名和密码是否正确
    echo 3. 数据库名是否正确
    echo ========================================
)

echo.
pause
