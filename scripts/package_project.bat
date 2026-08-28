@echo off
echo ==========================================
echo 智能宿舍管理系统 - 项目打包工具
echo ==========================================
echo.

:: 设置变量
set PROJECT_NAME=SmartDormSystem
set PACKAGE_DIR=%PROJECT_NAME%_Package
set CURRENT_DIR=%CD%

echo 正在创建打包目录...
if exist %PACKAGE_DIR% rmdir /s /q %PACKAGE_DIR%
mkdir %PACKAGE_DIR%

echo.
echo 正在复制项目文件...

:: 复制整个项目（排除不必要的文件）
xcopy /E /I /Y "..\backend" "%PACKAGE_DIR%\backend" /EXCLUDE:exclude_list.txt
xcopy /E /I /Y "..\frontend" "%PACKAGE_DIR%\frontend" /EXCLUDE:exclude_list.txt

:: 复制重要文件
copy "..\README.md" "%PACKAGE_DIR%\"
copy "..\DEPLOYMENT_GUIDE.md" "%PACKAGE_DIR%\"
copy "..\PROJECT_STRUCTURE.md" "%PACKAGE_DIR%\"

:: 复制脚本
mkdir "%PACKAGE_DIR%\scripts"
copy "export_database.bat" "%PACKAGE_DIR%\scripts\"
copy "export_database.sh" "%PACKAGE_DIR%\scripts\"

echo.
echo 正在导出数据库...
call export_database.bat

:: 复制数据库文件
if exist "smartdorm_complete.sql" (
    copy "smartdorm_complete.sql" "%PACKAGE_DIR%\"
    echo ✅ 数据库文件已包含
) else (
    echo ⚠️ 数据库导出失败，请手动导出
)

echo.
echo ==========================================
echo 打包完成！
echo 打包目录: %CD%\%PACKAGE_DIR%
echo ==========================================
echo.
echo 迁移步骤:
echo 1. 将整个 %PACKAGE_DIR% 文件夹复制到目标设备
echo 2. 按照 DEPLOYMENT_GUIDE.md 进行部署
echo.
pause
