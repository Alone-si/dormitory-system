@echo off
chcp 65001 >nul
echo ========================================
echo 前端打包工具
echo ========================================
echo.

echo [1/2] 清理前端编译产物...
cd frontend
if exist node_modules rmdir /s /q node_modules
if exist dist rmdir /s /q dist
if exist .vite rmdir /s /q .vite
cd ..
echo ✓ 清理完成

echo.
echo [2/2] 打包说明
echo ========================================
echo 请手动压缩以下内容：
echo.
echo 1. frontend 文件夹 → 压缩成 frontend.zip
echo.
echo 将这个文件复制到新电脑即可
echo ========================================
echo.
echo 新电脑需要安装：
echo - Node.js 18+
echo.
pause
