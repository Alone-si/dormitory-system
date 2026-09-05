@echo off
chcp 65001 >nul
REM 智能宿舍管理系统 - 一键启动脚本

echo ========================================
echo   智能宿舍管理系统 - 一键启动
echo ========================================
echo.

REM 检查环境
echo [1/5] 检查运行环境...
echo.

REM 检查Java
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ❌ 错误：未检测到Java环境！
    echo    请安装 JDK 17
    echo    下载地址：https://adoptium.net/
    echo.
    pause
    exit /b 1
)
echo ✅ Java环境正常

REM 检查Node.js
node -v >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ❌ 错误：未检测到Node.js环境！
    echo    请安装 Node.js 24
    echo    下载地址：https://nodejs.org/
    echo.
    pause
    exit /b 1
)
echo ✅ Node.js环境正常

REM 检查MySQL
mysql --version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ⚠️  警告：未检测到MySQL命令行工具
    echo    请确保MySQL服务正在运行
    echo.
)

echo.
echo [2/5] 检查项目文件...
echo.

if not exist "backend\pom.xml" (
    echo ❌ 错误：未找到后端项目文件！
    echo    请确保在项目根目录运行此脚本
    echo.
    pause
    exit /b 1
)
echo ✅ 后端项目文件存在

if not exist "frontend\package.json" (
    echo ❌ 错误：未找到前端项目文件！
    echo    请确保在项目根目录运行此脚本
    echo.
    pause
    exit /b 1
)
echo ✅ 前端项目文件存在

echo.
echo [3/5] 检查前端依赖...
echo.

if not exist "frontend\node_modules" (
    echo ⚠️  首次运行，需要安装前端依赖...
    echo    这可能需要几分钟时间，请耐心等待
    echo.
    cd frontend
    call npm ci
    if %ERRORLEVEL% NEQ 0 (
        echo ❌ 前端依赖安装失败！
        echo    请检查网络连接或尝试使用淘宝镜像：
        echo    npm config set registry https://registry.npmmirror.com
        cd ..
        pause
        exit /b 1
    )
    cd ..
    echo ✅ 前端依赖安装完成
) else (
    echo ✅ 前端依赖已存在
)

echo.
echo [4/5] 启动后端服务...
echo.
echo 正在启动后端，请稍候...
echo （首次启动可能需要下载Maven依赖，请耐心等待）
echo.

cd backend
start "后端服务 - 请勿关闭" cmd /k "mvnw.cmd spring-boot:run || (echo. && echo ❌ 后端启动失败！ && echo 请检查： && echo 1. MySQL是否正在运行 && echo 2. config/application-local.properties中的数据库密码是否正确 && echo 3. 端口8080是否被占用 && pause)"
cd ..

echo ⏳ 等待后端启动（预计30-60秒）...
timeout /t 30 /nobreak >nul

echo.
echo [5/5] 启动前端服务...
echo.

cd frontend
start "前端服务 - 请勿关闭" cmd /k "npm run dev || (echo. && echo ❌ 前端启动失败！ && pause)"
cd ..

echo.
echo ========================================
echo ✅ 启动完成！
echo ========================================
echo.
echo 📌 访问地址：
echo    前端：http://localhost:5173
echo    后端：http://localhost:8080
echo.
echo 📌 注意事项：
echo    1. 请勿关闭弹出的两个命令行窗口
echo    2. 如果浏览器没有自动打开，请手动访问上述地址
echo    3. 首次启动可能需要等待1-2分钟
echo.
echo 按任意键打开浏览器...
pause >nul

start http://localhost:5173

echo.
echo 系统正在运行中...
echo 关闭此窗口不会停止服务
echo 如需停止服务，请关闭另外两个命令行窗口
echo.
