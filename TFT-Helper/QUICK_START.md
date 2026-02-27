# 快速开始 - APK打包

## 准备工作

### 1. 安装必要软件
- [Android Studio](https://developer.android.com/studio)（推荐）或 Android SDK
- JDK 17 或更高版本

### 2. 配置环境变量（Windows）
在系统环境变量中添加：
```
ANDROID_HOME = C:\Users\你的用户名\AppData\Local\Android\Sdk
```

### 3. 创建 local.properties
在项目根目录创建 `local.properties` 文件，内容如下：

**Windows:**
```properties
sdk.dir=C:\\Users\\你的用户名\\AppData\\Local\\Android\\Sdk
```

**Mac:**
```properties
sdk.dir=/Users/你的用户名/Library/Android/sdk
```

---

## 方法一：使用一键打包脚本（最简单）

### Windows 用户：
1. 双击运行 `build_apk.bat`
2. 选择选项 `1` 打包 Debug 版本
3. 等待完成，APK 会自动打开

### Mac/Linux 用户：
1. 打开终端，进入项目目录
2. 给脚本执行权限：
   ```bash
   chmod +x build_apk.sh
   ```
3. 运行脚本：
   ```bash
   ./build_apk.sh
   ```
4. 选择选项 `1` 打包 Debug 版本

---

## 方法二：使用 Android Studio

### 步骤1：打开项目
1. 打开 Android Studio
2. 选择 `Open`，选择 `TFT-Helper` 文件夹
3. 等待 Gradle 同步完成

### 步骤2：打包
1. 点击菜单 `Build` → `Build Bundle(s) / APK(s)` → `Build APK(s)`
2. 等待构建完成
3. 右下角会弹出提示，点击 `locate` 找到 APK 文件

APK 位置：`app/build/outputs/apk/debug/app-debug.apk`

---

## 方法三：使用命令行

### Windows:
```cmd
gradlew.bat assembleDebug
```

### Mac/Linux:
```bash
./gradlew assembleDebug
```

---

## 安装到手机

### 方法1：使用 ADB
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 方法2：直接传输
1. 将 APK 复制到手机
2. 在手机上点击安装
3. 可能需要开启 `允许安装未知来源应用`

---

## 常见问题

### 问题1：找不到 gradle-wrapper.jar
**解决：** 
运行以下命令下载：
```bash
gradle wrapper
```
或从 https://services.gradle.org/distributions/gradle-8.2-bin.zip 下载并放到 gradle/wrapper/ 目录

### 问题2：SDK 路径错误
**解决：**
检查 `local.properties` 中的路径是否正确，确保使用双反斜杠（Windows）：
```properties
sdk.dir=C:\\Users\\Name\\AppData\\Local\\Android\\Sdk
```

### 问题3：Java 版本不匹配
**解决：**
确保安装了 JDK 17+，并配置 JAVA_HOME 环境变量。

---

## 下一步

安装成功后，你可以：
1. 查看阵容库，了解热门阵容
2. 使用概率计算器，计算碰对手概率
3. 使用牌库计算器，判断是否适合追三星
4. 记录对战数据，分析胜率

祝使用愉快！
