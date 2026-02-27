# APK打包指南

本指南将帮助你把金铲铲助手项目打包成APK文件。

## 方法一：使用 Android Studio（推荐）

### 步骤1：打开项目
1. 下载并安装 [Android Studio](https://developer.android.com/studio)
2. 打开 Android Studio
3. 选择 `Open an existing Android Studio project`
4. 选择 `TFT-Helper` 文件夹

### 步骤2：配置SDK
1. 打开 `File` → `Settings` → `Appearance & Behavior` → `System Settings` → `Android SDK`
2. 安装以下组件：
   - Android SDK Platform 34
   - Android SDK Build-Tools 34
   - Android Emulator（可选）
   - Android SDK Platform-Tools

### 步骤3：同步项目
1. 打开项目后，Android Studio 会自动提示同步 Gradle
2. 点击 `Sync Now` 等待同步完成
3. 如果提示缺少依赖，点击 `Install` 安装

### 步骤4：构建APK
1. 点击菜单栏 `Build` → `Generate Signed Bundle / APK...`
2. 选择 `APK`，点击 `Next`

#### 如果是第一次打包，需要创建密钥：
1. 点击 `Create new...`
2. 填写密钥信息：
   - Key store path: 选择保存位置（如 `tft-helper.jks`）
   - Password: 设置密码（至少6位）
   - Key alias: `tft-helper`
   - Key password: 设置密码（可以与上面相同）
   - Validity: 25年
   - 填写证书信息（姓名、组织等，可以随意填写）
3. 点击 `OK`

#### 选择密钥后：
1. 点击 `Next`
2. 选择构建类型：`release`
3. 点击 `Finish`
4. 等待构建完成

### 步骤5：找到APK
构建完成后，APK文件位置：
```
TFT-Helper/app/release/app-release.apk
```

---

## 方法二：使用命令行打包

### 前提条件
1. 安装 Android SDK
2. 配置环境变量 `ANDROID_HOME` 指向 SDK 目录
3. 安装 JDK 17 或更高版本

### 步骤1：配置本地属性
编辑 `local.properties` 文件，添加你的 SDK 路径：

**Windows:**
```properties
sdk.dir=C:\\Users\\你的用户名\\AppData\\Local\\Android\\Sdk
```

**Mac:**
```properties
sdk.dir=/Users/你的用户名/Library/Android/sdk
```

**Linux:**
```properties
sdk.dir=/home/你的用户名/Android/Sdk
```

### 步骤2：打开终端
在项目根目录 `TFT-Helper` 打开终端

### 步骤3：执行打包命令

#### Debug版本（快速测试）：
```bash
./gradlew assembleDebug
```

APK输出位置：`app/build/outputs/apk/debug/app-debug.apk`

#### Release版本（正式发布）：
首先需要配置签名，创建 `keystore.properties` 文件：
```properties
storeFile=tft-helper.jks
storePassword=你的密码
keyAlias=tft-helper
keyPassword=你的密码
```

然后执行：
```bash
./gradlew assembleRelease
```

APK输出位置：`app/build/outputs/apk/release/app-release.apk`

---

## 可能遇到的问题

### 问题1：Gradle 同步失败
**现象：** 提示 `Gradle sync failed`

**解决方案：**
1. 检查网络连接（需要能访问 Google 服务）
2. 点击 `File` → `Invalidate Caches / Restart`
3. 重新同步

### 问题2：缺少 SDK
**现象：** 提示 `SDK not found`

**解决方案：**
1. 打开 `local.properties` 文件
2. 确保 `sdk.dir` 指向正确的 Android SDK 路径
3. 在 SDK Manager 中安装缺少的平台

### 问题3：依赖下载失败
**现象：** 提示 `Could not resolve dependencies`

**解决方案：**
1. 检查网络连接
2. 更换 Maven 仓库源（在 `build.gradle` 中添加阿里云镜像）
3. 重试同步

### 问题4：签名错误
**现象：** 提示 `Keystore file not found`

**解决方案：**
1. 确保创建了签名密钥
2. 检查 `keystore.properties` 中的路径和密码是否正确

---

## 安装测试

### 方式1：通过ADB安装
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 方式2：直接传输
1. 将APK文件复制到手机
2. 在手机上使用文件管理器安装
3. 需要开启 `允许安装未知来源应用`

---

## 快速检查清单

打包前请确认：
- [ ] Android SDK 已安装
- [ ] JDK 17+ 已安装
- [ ] 项目已同步 Gradle
- [ ] 已配置签名密钥（Release版本）
- [ ] 网络连接正常

---

## 需要帮助？

如果在打包过程中遇到问题，请提供：
1. 完整的错误日志
2. Android Studio 版本
3. Gradle 版本
4. 操作系统版本
