@echo off
chcp 65001 >nul
REM 智能宿舍管理系统 - 环境检查工具

echo ========================================
echo   智能宿舍管理系统 - 环境检查工具
echo ========================================
echo.
echo 正在检查运行环境...
echo.

set ERROR_COUNT=0

REM 检查Java
echo [1/4] 检查 Java 环境
echo ----------------------------------------
java -version >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo ✅ Java 已安装
    java -version 2>&1 | findstr "version"
    
    REM 检查Java版本
    for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
        set JAVA_VERSION=%%g
    )
    echo    版本：%JAVA_VERSION%
    
    REM 简单检查是否为Java 17
    echo %JAVA_VERSION% | findstr "17" >nul
    if %ERRORLEVEL% EQU 0 (
        echo    ✅ 版本正确（需要 JDK 17）
    ) else (
        echo    ⚠️  警告：建议使用 JDK 17
        set /a ERROR_COUNT+=1
    )
) else (
    echo ❌ Java 未安装或未配置环境变量
    echo    下载地址：https://adoptium.net/
    set /a ERROR_COUNT+=1
)
echo.

REM 检查Node.js
echo [2/4] 检查 Node.js 环境
echo ----------------------------------------
node -v >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo ✅ Node.js 已安装
    for /f %%i in ('node -v') do set NODE_VERSION=%%i
    echo    版本：%NODE_VERSION%
    
    REM 检查npm
    npm -v >nul 2>&1
    if %ERRORLEVEL% EQU 0 (
        for /f %%i in ('npm -v') do set NPM_VERSION=%%i
        echo    npm 版本：%NPM_VERSION%
        echo    ✅ npm 正常
    ) else (
        echo    ⚠️  npm 未找到
        set /a ERROR_COUNT+=1
    )
) else (
    echo ❌ Node.js 未安装或未配置环境变量
    echo    下载地址：https://nodejs.org/
    set /a ERROR_COUNT+=1
)
echo.

REM 检查MySQL
echo [3/4] 检查 MySQL 环境
echo ----------------------------------------
mysql --version >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo ✅ MySQL 命令行工具已安装
    mysql --version
    
    REM 检查MySQL服务
    sc query MySQL80 >nul 2>&1
    if %ERRORLEVEL% EQU 0 (
        sc query MySQL80 | findstr "RUNNING" >nul
        if %ERRORLEVEL% EQU 0 (
            echo    ✅ MySQL 服务正在运行
        ) else (
            echo    ⚠️  MySQL 服务未运行
            echo    请启动MySQL服务
            set /a ERROR_COUNT+=1
        )
    ) else (
        echo    ⚠️  无法检测MySQL服务状态
        echo    请手动确认MySQL是否正在运行
    )
) else (
    echo ⚠️  MySQL 命令行工具未找到
    echo    请确保MySQL已安装并配置环境变量
    echo    下载地址：https://dev.mysql.com/downloads/mysql/
)
echo.

REM 检查Maven（可选）
echo [4/4] 检查 Maven 环境（可选）
echo ----------------------------------------
mvn -v >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo ✅ Maven 已安装
    mvn -v | findstr "Apache Maven"
) else (
    echo ⚠️  Maven 未安装（可选）
    echo    项目自带 mvnw，可以不安装Maven
)
echo.

REM 检查端口占用
echo ========================================
echo 检查端口占用情况
echo ========================================
echo.

echo 检查端口 8080（后端）...
netstat -ano | findstr ":8080" >nul
if %ERRORLEVEL% EQU 0 (
    echo ⚠️  端口 8080 已被占用
    echo    请关闭占用端口的程序或修改配置文件中的端口
    netstat -ano | findstr ":8080"
    set /a ERROR_COUNT+=1
) else (
    echo ✅ 端口 8080 可用
)
echo.

echo 检查端口 5173（前端）...
netstat -ano | findstr ":5173" >nul
if %ERRORLEVEL% EQU 0 (
    echo ⚠️  端口 5173 已被占用
    echo    请关闭占用端口的程序
    netstat -ano | findstr ":5173"
    set /a ERROR_COUNT+=1
) else (
    echo ✅ 端口 5173 可用
)
echo.

echo 检查端口 3306（MySQL）...
netstat -ano | findstr ":3306" >nul
if %ERRORLEVEL% EQU 0 (
    echo ✅ 端口 3306 有程序监听（应该是MySQL）
) else (
    echo ⚠️  端口 3306 未被监听
    echo    MySQL可能未启动
    set /a ERROR_COUNT+=1
)
echo.

REM 总结
echo ========================================
echo 检查结果总结
echo ========================================
echo.

if %ERROR_COUNT% EQU 0 (
    echo ✅✅✅ 环境检查通过！
    echo.
    echo 所有必需的环境都已就绪，可以运行项目了！
    echo.
    echo 下一步：
    echo 1. 确保数据库已导入
    echo 2. 在 backend/config/application-local.properties 中填写数据库密码
    echo 3. 双击运行 "启动项目.bat"
) else (
    echo ❌ 发现 %ERROR_COUNT% 个问题
    echo.
    echo 请先解决上述问题后再运行项目
    echo.
    echo 常见问题解决方案：
    echo 1. 环境变量未配置：需要将软件的bin目录添加到系统PATH
    echo 2. 端口被占用：关闭占用端口的程序或修改配置文件
    echo 3. MySQL未启动：在服务中启动MySQL服务
)
echo.

echo ========================================
echo 详细信息
echo ========================================
echo.
echo 系统信息：
systeminfo | findstr /B /C:"OS Name" /C:"OS Version"
echo.
echo 当前目录：
cd
echo.

pause
