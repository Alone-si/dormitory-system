@echo off
chcp 65001 >nul
echo ========================================
echo 智能宿舍管理系统 - 项目打包工具
echo ========================================
echo.

echo [1/4] 清理后端编译产物...
cd backend
if exist target rmdir /s /q target
if exist .idea rmdir /s /q .idea
if exist logs rmdir /s /q logs
cd ..
echo ✓ 后端清理完成

echo.
echo [2/4] 清理前端编译产物...
cd frontend
if exist node_modules rmdir /s /q node_modules
if exist dist rmdir /s /q dist
if exist .vite rmdir /s /q .vite
cd ..
echo ✓ 前端清理完成

echo.
echo [3/4] 导出数据库...
echo 请输入 MySQL 密码：
mysqldump -u root -p alone > alone_backup.sql
if %errorlevel% equ 0 (
    echo ✓ 数据库导出完成: alone_backup.sql
) else (
    echo ✗ 数据库导出失败，请检查MySQL是否运行
)

echo.
echo [4/4] 打包说明
echo ========================================
echo 请手动压缩以下文件夹：
echo.
echo 1. backend 文件夹 → backend.zip
echo 2. frontend 文件夹 → frontend.zip
echo 3. alone_backup.sql → 数据库备份
echo.
echo 打包完成后，将这3个文件复制到新电脑即可
echo ========================================
echo.
pause
