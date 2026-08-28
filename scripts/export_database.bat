@echo off
echo ==========================================
echo 智能宿舍管理系统 - 数据库导出工具
echo ==========================================
echo.

:: 设置变量
set DB_NAME=alone
set EXPORT_FILE=smartdorm_complete.sql
set SCHEMA_FILE=smartdorm_schema.sql

echo 请输入MySQL root密码:
set /p MYSQL_PASSWORD=

echo.
echo 正在导出完整数据库...
mysqldump -u root -p%MYSQL_PASSWORD% %DB_NAME% > %EXPORT_FILE%

if %ERRORLEVEL% EQU 0 (
    echo ✅ 完整数据库导出成功: %EXPORT_FILE%
) else (
    echo ❌ 数据库导出失败
    pause
    exit /b 1
)

echo.
echo 正在导出数据库结构...
mysqldump --no-data -u root -p%MYSQL_PASSWORD% %DB_NAME% > %SCHEMA_FILE%

if %ERRORLEVEL% EQU 0 (
    echo ✅ 数据库结构导出成功: %SCHEMA_FILE%
) else (
    echo ❌ 数据库结构导出失败
)

echo.
echo ==========================================
echo 导出完成！
echo 文件位置:
echo - 完整数据库: %CD%\%EXPORT_FILE%
echo - 仅结构: %CD%\%SCHEMA_FILE%
echo ==========================================
echo.
echo 迁移时请使用: %EXPORT_FILE%
echo.
pause
