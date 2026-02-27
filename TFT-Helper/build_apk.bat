@echo off
chcp 65001
cls
echo =====================================
echo    金铲铲助手 - APK打包工具
echo =====================================
echo.

:: 检查Gradle Wrapper是否存在
if not exist "gradlew.bat" (
    echo [错误] 找不到gradlew.bat，请确保在项目根目录运行此脚本
    pause
    exit /b 1
)

:: 检查local.properties
echo [检查] 检查SDK配置...
if not exist "local.properties" (
    echo [警告] 未找到local.properties文件
    echo 请创建此文件并添加你的Android SDK路径：
    echo   sdk.dir=C:\Users\你的用户名\AppData\Local\Android\Sdk
    echo.
    pause
    exit /b 1
)

echo [检查] SDK配置已找到
echo.

:: 显示菜单
echo 请选择打包类型：
echo   1. Debug版本（快速测试，无需签名）
echo   2. Release版本（正式发布，需要签名）
echo   3. 清理项目
echo   4. 退出
echo.

set /p choice="请输入选项(1-4): "

if "%choice%"=="1" goto build_debug
if "%choice%"=="2" goto build_release
if "%choice%"=="3" goto clean_project
if "%choice%"=="4" goto end

echo [错误] 无效选项
pause
exit /b 1

:build_debug
echo.
echo [开始] 打包Debug版本...
echo.
call gradlew.bat assembleDebug
if %errorlevel% neq 0 (
    echo.
    echo [错误] 打包失败，请检查上面的错误信息
    pause
    exit /b 1
)
echo.
echo [成功] Debug APK已生成！
echo 文件位置: app\build\outputs\apk\debug\app-debug.apk
start explorer "app\build\outputs\apk\debug"
pause
goto end

:build_release
echo.
echo [检查] 检查签名配置...
if not exist "keystore.properties" (
    echo [提示] 未找到签名配置，将使用Debug签名打包
    echo.
    echo 如需创建正式签名，请运行：
    echo   keytool -genkey -v -keystore tft-helper.jks -keyalg RSA -keysize 2048 -validity 10000 -alias tft-helper
    echo.
    choice /C YN /M "是否继续打包"
    if %errorlevel%==2 goto end
)
echo.
echo [开始] 打包Release版本...
echo.
call gradlew.bat assembleRelease
if %errorlevel% neq 0 (
    echo.
    echo [错误] 打包失败，请检查上面的错误信息
    pause
    exit /b 1
)
echo.
echo [成功] Release APK已生成！
echo 文件位置: app\build\outputs\apk\release\app-release.apk
start explorer "app\build\outputs\apk\release"
pause
goto end

:clean_project
echo.
echo [开始] 清理项目...
echo.
call gradlew.bat clean
if %errorlevel% neq 0 (
    echo [错误] 清理失败
    pause
    exit /b 1
)
echo [成功] 项目已清理
pause
goto end

:end
echo.
echo 感谢使用！
