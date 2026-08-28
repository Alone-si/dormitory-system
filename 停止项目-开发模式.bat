@echo off
chcp 65001 >nul
REM 智能宿舍管理系统 - 停止服务脚本

echo ========================================
echo   智能宿舍管理系统 - 停止服务
echo ========================================
echo.

echo 正在查找并停止服务...
echo.

REM 停止前端服务（Vite开发服务器）
echo [1/2] 停止前端服务...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":5173"') do (
    echo 找到前端进程 PID: %%a
    taskkill /F /PID %%a >nul 2>&1
    if %ERRORLEVEL% EQU 0 (
        echo ✅ 前端服务已停止
    )
)

REM 检查是否还有Vite进程
tasklist | findstr "node.exe" | findstr "vite" >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo 正在停止Vite进程...
    taskkill /F /IM node.exe /FI "WINDOWTITLE eq 前端服务*" >nul 2>&1
)

REM 停止后端服务（Spring Boot）
echo.
echo [2/2] 停止后端服务...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8080"') do (
    echo 找到后端进程 PID: %%a
    taskkill /F /PID %%a >nul 2>&1
    if %ERRORLEVEL% EQU 0 (
        echo ✅ 后端服务已停止
    )
)

REM 检查是否还有Java进程
tasklist | findstr "java.exe" | findstr "spring-boot" >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo 正在停止Spring Boot进程...
    taskkill /F /IM java.exe /FI "WINDOWTITLE eq 后端服务*" >nul 2>&1
)

echo.
echo ========================================
echo ✅ 服务已停止
echo ========================================
echo.
echo 提示：
echo - 如果有命令行窗口未关闭，请手动关闭
echo - 如果需要重新启动，请运行"启动项目-开发模式.bat"
echo.

pause
