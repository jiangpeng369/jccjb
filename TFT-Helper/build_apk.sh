#!/bin/bash

# 金铲铲助手 - APK打包脚本
# 适用于 Mac/Linux

echo "======================================"
echo "   金铲铲助手 - APK打包工具"
echo "======================================"
echo ""

# 检查Gradle Wrapper
if [ ! -f "./gradlew" ]; then
    echo "[错误] 找不到gradlew，请确保在项目根目录运行此脚本"
    exit 1
fi

# 检查local.properties
echo "[检查] 检查SDK配置..."
if [ ! -f "local.properties" ]; then
    echo "[警告] 未找到local.properties文件"
    echo "请创建此文件并添加你的Android SDK路径："
    echo "  sdk.dir=/Users/你的用户名/Library/Android/sdk"
    exit 1
fi

echo "[检查] SDK配置已找到"
echo ""

# 显示菜单
echo "请选择打包类型："
echo "  1. Debug版本（快速测试，无需签名）"
echo "  2. Release版本（正式发布，需要签名）"
echo "  3. 清理项目"
echo "  4. 退出"
echo ""

read -p "请输入选项(1-4): " choice

case $choice in
    1)
        echo ""
        echo "[开始] 打包Debug版本..."
        echo ""
        ./gradlew assembleDebug
        if [ $? -ne 0 ]; then
            echo ""
            echo "[错误] 打包失败，请检查上面的错误信息"
            exit 1
        fi
        echo ""
        echo "[成功] Debug APK已生成！"
        echo "文件位置: app/build/outputs/apk/debug/app-debug.apk"
        open app/build/outputs/apk/debug 2>/dev/null || xdg-open app/build/outputs/apk/debug 2>/dev/null
        ;;
    2)
        echo ""
        echo "[检查] 检查签名配置..."
        if [ ! -f "keystore.properties" ]; then
            echo "[提示] 未找到签名配置"
            echo "如需创建正式签名，请运行："
            echo "  keytool -genkey -v -keystore tft-helper.jks -keyalg RSA -keysize 2048 -validity 10000 -alias tft-helper"
            echo ""
            read -p "是否继续打包？(y/n): " continue
            if [ "$continue" != "y" ]; then
                exit 0
            fi
        fi
        echo ""
        echo "[开始] 打包Release版本..."
        echo ""
        ./gradlew assembleRelease
        if [ $? -ne 0 ]; then
            echo ""
            echo "[错误] 打包失败，请检查上面的错误信息"
            exit 1
        fi
        echo ""
        echo "[成功] Release APK已生成！"
        echo "文件位置: app/build/outputs/apk/release/app-release.apk"
        open app/build/outputs/apk/release 2>/dev/null || xdg-open app/build/outputs/apk/release 2>/dev/null
        ;;
    3)
        echo ""
        echo "[开始] 清理项目..."
        echo ""
        ./gradlew clean
        if [ $? -ne 0 ]; then
            echo "[错误] 清理失败"
            exit 1
        fi
        echo "[成功] 项目已清理"
        ;;
    4)
        echo "退出"
        exit 0
        ;;
    *)
        echo "[错误] 无效选项"
        exit 1
        ;;
esac

echo ""
echo "感谢使用！"
