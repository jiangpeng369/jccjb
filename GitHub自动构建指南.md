# GitHub Actions 自动构建APK指南

通过GitHub Actions，每次你推送代码到GitHub时，都会自动构建APK，无需本地配置Android环境。

---

## 步骤1：创建GitHub账号

1. 打开 https://github.com
2. 点击右上角 `Sign up`
3. 填写邮箱、密码、用户名完成注册
4. 验证邮箱

---

## 步骤2：创建新仓库

1. 登录GitHub后，点击右上角 `+` 号 → `New repository`
2. 填写仓库信息：
   - **Repository name**: `tft-helper`（或你喜欢的名字）
   - **Description**: 金铲铲之战辅助工具（可选）
   - **Visibility**: 选择 `Public`（免费）或 `Private`（私密）
   - **Initialize this repository with**: 保持默认（不勾选）
3. 点击 `Create repository`

---

## 步骤3：上传代码到GitHub

### 方式A：使用Git命令行（推荐）

1. 安装Git：https://git-scm.com/download

2. 打开命令行，进入项目目录：
```bash
cd TFT-Helper
```

3. 初始化Git仓库并提交：
```bash
# 初始化Git仓库
git init

# 添加所有文件
git add .

# 提交
git commit -m "初始提交"

# 添加远程仓库（将USERNAME替换为你的GitHub用户名）
git remote add origin https://github.com/USERNAME/tft-helper.git

# 推送到GitHub
git branch -M main
git push -u origin main
```

### 方式B：使用GitHub Desktop（图形界面）

1. 下载安装：https://desktop.github.com
2. 登录你的GitHub账号
3. 选择 `File` → `Add local repository`
4. 选择 `TFT-Helper` 文件夹
5. 填写提交信息，点击 `Commit to main`
6. 点击 `Publish repository`

### 方式C：直接上传文件（最简单）

1. 在GitHub仓库页面，点击 `uploading an existing file` 链接
2. 将 `TFT-Helper` 文件夹内的所有文件和文件夹拖入
3. 等待上传完成
4. 点击 `Commit changes`

**注意**：需要上传的文件夹包括：
- `app/` 文件夹
- `.github/` 文件夹
- `gradle/` 文件夹
- 所有根目录下的文件（build.gradle等）

---

## 步骤4：触发自动构建

代码推送到GitHub后，自动构建会立即开始：

1. 打开你的GitHub仓库页面
2. 点击上方的 `Actions` 标签
3. 你会看到 `Build APK` 工作流正在运行
4. 等待约5-10分钟（首次构建需要下载依赖）

---

## 步骤5：下载APK

构建完成后，下载APK：

### 方式1：从Artifacts下载

1. 在 `Actions` 页面，点击最新完成的构建
2. 页面下方有 `Artifacts` 区域
3. 点击 `TFT-Helper-Debug-APK` 下载
4. 解压ZIP文件即可获得APK

### 方式2：创建Release下载（推荐）

1. 在你的GitHub仓库页面，点击右侧的 `Create a new release`
2. 点击 `Choose a tag`，输入 `v1.0.0`，点击 `Create new tag`
3. 填写Release标题：`金铲铲助手 v1.0.0`
4. 点击 `Publish release`
5. 等待构建完成，APK会自动上传到Release中
6. 在Release页面即可下载APK

---

## 后续更新

当你修改代码后，重新推送即可自动构建：

```bash
cd TFT-Helper
git add .
git commit -m "更新说明"
git push
```

推送后，GitHub Actions会自动构建新的APK。

---

## 常见问题

### Q: 构建失败了怎么办？

A: 查看构建日志：
1. 打开Actions页面
2. 点击失败的构建
3. 查看错误日志
4. 常见错误：
   - 缺少文件：确保上传了所有文件
   - 编译错误：检查代码是否有语法错误

### Q: 如何加快构建速度？

A: GitHub Actions会缓存Gradle依赖，后续构建会更快。

### Q: 可以同时构建Debug和Release吗？

A: 可以，工作流已配置同时构建两个版本。

### Q: 构建的APK可以直接安装吗？

A: 
- Debug版本：可以直接安装
- Release版本：需要签名才能安装，可以使用Debug签名

---

## 快捷命令总结

```bash
# 初始化并推送
git init
git add .
git commit -m "初始提交"
git remote add origin https://github.com/用户名/仓库名.git
git push -u origin main

# 后续更新
git add .
git commit -m "更新内容"
git push
```

---

## 需要帮助？

如果在配置过程中遇到问题，请提供：
1. 错误截图或日志
2. 你执行的操作步骤
