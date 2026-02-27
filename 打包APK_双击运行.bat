@echo off
chcp 65001 >nul
title 金铲铲助手 - APK打包工具
cls

echo ========================================
echo      金铲铲助手 - APK打包工具
echo ========================================
echo.

:: 检查是否安装了Android Studio
set "ANDROID_STUDIO=%PROGRAMFILES%\Android\Android Studio\bin\studio64.exe"
set "ANDROID_STUDIO_X86=%PROGRAMFILES%\Android\Android Studio\bin\studio.exe"
set "ANDROID_STUDIO_LOCAL=%LOCALAPPDATA%\Android\Android Studio\bin\studio64.exe"

if exist "%ANDROID_STUDIO%" (
    echo [✓] 发现 Android Studio
    goto found_as
)
if exist "%ANDROID_STUDIO_X86%" (
    echo [✓] 发现 Android Studio
    goto found_as
)
if exist "%ANDROID_STUDIO_LOCAL%" (
    echo [✓] 发现 Android Studio
    goto found_as
)

echo [✗] 未检测到 Android Studio
echo.
echo 请先安装 Android Studio：
echo https://developer.android.com/studio
echo.
pause
exit /b 1

:found_as
echo.
echo 请选择打包方式：
echo.
echo   [1] Debug版本（推荐测试用，无需签名）
echo   [2] Release版本（正式发布用，需要签名）
echo   [3] 打开Android Studio手动打包
echo.
set /p choice="请输入选项 (1-3): "

if "%choice%"=="1" goto build_debug
if "%choice%"=="2" goto build_release
if "%choice%"=="3" goto open_as

goto invalid

:build_debug
echo.
echo [1/3] 正在检查项目...
if not exist "TFT-Helper\gradlew.bat" (
    echo [✗] 错误：找不到项目文件，请确保此脚本在TFT-Helper文件夹的父目录中
    pause
    exit /b 1
)

echo [2/3] 正在配置SDK路径...
if not exist "TFT-Helper\local.properties" (
    echo [!] 首次使用，需要配置SDK路径
    echo.
    echo 请按以下步骤操作：
    echo 1. 打开 Android Studio
    echo 2. 点击 File → Settings → Appearance ^& Behavior → System Settings → Android SDK
    echo 3. 复制 "Android SDK Location" 路径
    echo.
    set /p sdkpath="请粘贴SDK路径: "
    echo sdk.dir=%sdkpath:\=\\% > "TFT-Helper\local.properties"
    echo [✓] SDK路径已配置
)

echo [3/3] 开始打包Debug版本...
echo 这可能需要几分钟，请耐心等待...
echo.

cd TFT-Helper
call gradlew.bat assembleDebug --console=plain

if %errorlevel% neq 0 (
    echo.
    echo [✗] 打包失败！
    echo 可能原因：
    echo   1. 网络问题（需要访问Google下载依赖）
    echo   2. SDK组件缺失
    echo   3. Java环境未配置
    echo.
    echo 建议：打开Android Studio导入项目后手动打包
    pause
    exit /b 1
)

echo.
echo [✓] 打包成功！
echo.
echo APK文件位置：
echo   TFT-Helper\app\build\outputs\apk\debug\app-debug.apk
echo.

:: 复制APK到当前目录
copy /Y "app\build\outputs\apk\debug\app-debug.apk" "..\金铲铲助手_测试版.apk" >nul 2>&1
if exist "..\金铲铲助手_测试版.apk" (
    echo [✓] 已复制到当前文件夹：金铲铲助手_测试版.apk
)

cd ..

choice /C YN /N /M "是否打开APK所在文件夹? (Y/N) "
if %errorlevel%==1 explorer "TFT-Helper\app\build\outputs\apk\debug"

goto end

:build_release
echo.
echo [Release版本打包]
echo.
echo 注意：Release版本需要签名密钥才能安装到手机
echo.
echo 请选择：
echo   [1] 使用Debug签名打包（可以安装，但不是正式版）
echo   [2] 创建新的签名密钥（正式版）
echo   [3] 返回上级菜单
echo.
set /p release_choice="请输入选项 (1-3): "

if "%release_choice%"=="1" goto build_release_debug
if "%release_choice%"=="2" goto create_keystore
if "%release_choice%"=="3" goto found_as
goto build_release

:build_release_debug
cd TFT-Helper
echo 正在打包Release版本（使用Debug签名）...
call gradlew.bat assembleRelease --console=plain
if %errorlevel% equ 0 (
    echo [✓] 打包成功！
    copy /Y "app\build\outputs\apk\release\app-release.apk" "..\金铲铲助手_正式版.apk" >nul 2>&1
    if exist "..\金铲铲助手_正式版.apk" (
        echo [✓] 已复制到当前文件夹：金铲铲助手_正式版.apk
    )
    explorer "app\build\outputs\apk\release"
) else (
    echo [✗] 打包失败
)
cd ..
pause
goto end

:create_keystore
echo.
echo 创建签名密钥...
echo 请设置密钥信息（输入密码时不会显示字符）：
echo.

set /p keystore_pass="密钥库密码 (至少6位): "
set /p key_pass="密钥密码 (至少6位，可直接回车使用同上): "
if "%key_pass%"=="" set key_pass=%keystore_pass%

cd TFT-Helper

keytool -genkey -v -keystore tft-helper.jks -keyalg RSA -keysize 2048 -validity 10000 -alias tft-helper -storepass %keystore_pass% -keypass %key_pass% -dname "CN=TFTHelper, OU=Dev, O=TFTHelper, L=Beijing, ST=Beijing, C=CN"

echo storeFile=tft-helper.jks > keystore.properties
echo storePassword=%keystore_pass% >> keystore.properties
echo keyAlias=tft-helper >> keystore.properties
echo keyPassword=%key_pass% >> keystore.properties

echo [✓] 签名密钥已创建
cd ..
pause
goto build_release

:open_as
echo 正在打开 Android Studio...
start "" "%ANDROID_STUDIO%" "%CD%\TFT-Helper"
echo.
echo 请在Android Studio中操作：
echo   1. 等待Gradle同步完成
echo   2. 点击 Build → Build Bundle(s) / APK(s) → Build APK(s)
echo   3. 等待构建完成，点击右下角的 locate 找到APK
pause
goto end

:invalid
echo [✗] 无效选项
pause
goto found_as

:end
echo.
echo 感谢使用！按任意键退出...
pause >nul
