@echo off
chcp 65001 >nul
echo ========================================
echo 后端打包工具
echo ========================================
echo.

echo [1/3] 清理后端编译产物...
cd backend
if exist target rmdir /s /q target
if exist .idea rmdir /s /q .idea
if exist logs rmdir /s /q logs
if exist uploads rmdir /s /q uploads
cd ..
echo ✓ 清理完成

echo.
echo [2/3] 导出数据库...
echo 请输入 MySQL 密码：
mysqldump -u root -p alone > alone_backup.sql
if %errorlevel% equ 0 (
    echo ✓ 数据库导出完成: alone_backup.sql
) else (
    echo ✗ 数据库导出失败，请检查MySQL是否运行
)

echo.
echo [3/3] 打包说明
echo ========================================
echo 请手动压缩以下内容：
echo.
echo 1. backend 文件夹 → 压缩成 backend.zip
echo 2. alone_backup.sql → 数据库备份文件
echo.
echo 将这2个文件复制到新电脑即可
echo ========================================
echo.
echo 新电脑需要安装：
echo - JDK 17
echo - MySQL 8.0
echo.
pause
