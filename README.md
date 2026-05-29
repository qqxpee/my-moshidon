# **🚀 MoshidonQX 定制构建指南**

本仓库是基于 Moshidon（Mastodon 高值颜客户端）的个性化定制与性能优化分支，专为 GoToSocial/Mastodon 用户打造。

它支持**自定义应用名称**、**独立包名（实现分身共存）**、**破解多图上传数量限制**以及**流畅的视频播放引擎**。

任何人只需 Fork 本项目，即可直接在线编译出专属的个性化 APK！

---

## **⚙️ 配置参数说明**

你可以自定义以下三个核心参数：
1. **应用包名 (Package Name / Application ID)**：决定了应用在手机底层的唯一标识。通过将其设为独特的值（如带有 `.qx` 尾缀），**可以让此定制版与您手机里的原版 Moshidon 完美共存，互不覆盖**。
2. **应用名称 (App Name)**：手机桌面上图标下方显示的软件名称（支持中文）。
3. **最大图片上传数量限制 (Max Images Limit)**：突破原生 4 张图的限制，允许一次性选择并上传多张图片（默认 12 张）。

### **参数配置优先级**
这三个参数支持以下三级配置，系统会按照优先级自动读取：
1. **GitHub 运行输入**：在 Actions 页面手动点击 **Run workflow** 构建时直接填写的参数（临时生效，优先级最高）。
2. **GitHub 仓库变量**：在仓库 **Settings -> Secrets and variables -> Actions -> Variables** 下添加的全局变量：`CUSTOM_APPLICATION_ID`、`CUSTOM_APP_NAME`、`MAX_IMAGES_LIMIT`（长期生效）。
3. **本地配置文件**：直接修改项目根目录下的 [config.properties](file:///e:/temp/moshidon/config.properties) 文件（长期生效）。

---

## **🛠️ 极速构建指南**

### **第一步：Fork 本仓库**
点击本仓库右上角的 **Fork** 按钮，复制一份完整的代码到您的个人 GitHub 账号下。

### **第二步：选择您的工作流并触发构建**

本项目为您设计了**两个完全独立的工作流**，分别满足您的日常测试与正式发布需求：

#### **1. 🛠️ 开发测试流：`Build MoshidonQX (Dev 测试版)`**
* **特点**：编译极其迅速，**不需要任何证书签名配置**。
* **日志支持**：此测试版内部已激活崩溃日志捕获系统，闪退时会自动在手机 `/内部存储/Android/data/<您的包名>.debug/files/crashes/` 目录下生成 `.txt` 崩溃报告，极其便于排错！
* **编译包名**：已自动启用 `.debug` 包名后缀，可与您手机里的正式版客户端完美共存。
* **操作步骤**：
  1. 进入 Actions 页签，在左侧选择 `Build MoshidonQX (Dev 测试版)`。
  2. 点击右侧 **Run workflow**（可选择输入临时定制参数，不输则自动取上述配置变量），启动构建。
  3. 运行完成后，点击该次构建记录，直接拉到页面最底部，在 **Artifacts** 区域直接下载 **`MoshidonQX-Debug-Dev-APK`** 即可。

#### **2. 💎 生产发布流：`Build MoshidonQX (Prod 正式签名版)`**
* **特点**：正式的包名（无 `.debug` 后缀）。此生产正式版完全关闭了崩溃日志本地写入功能，不占日常存储空间，极致稳定纯净。
* **发布方式**：自动使用您的正式证书完成对 APK 的签名，并自动发布至仓库的 **Releases** 页面，方便更新和下载！
* **操作步骤**：
  1. 前往仓库 **Settings -> Secrets and variables -> Actions -> Secrets**，配置以下两个敏感密钥（Repository secrets）：
     * `KEYSTORE_FILE`：您私有 `.jks` 证书文件的 Base64 字符串。
     * `KEYSTORE_PASSWORD`：您的证书密码。
  2. 进入 Actions 页签，左侧选择 `Build MoshidonQX (Prod 正式签名版)`。
  3. 点击 **Run workflow** 并启动构建。
  4. 编译完成后，直接前往您仓库的 **Releases** 页面下载打包好的正式签名版 APK！
