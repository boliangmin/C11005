# Agent 规则

## 1. 修改完成后的编译、安装、提交与推送

每次完成代码或配置修改后，严格执行以下流程：

1. 在仓库根目录运行 `.\gradlew.bat testDebugUnitTest assembleDebug`，执行单元测试并生成最新 Debug APK。测试或编译失败时立即停止，不得安装 APK、不得提交。
2. 确认 `app/build/outputs/apk/debug/app-debug.apk` 是本次成功构建生成的文件，并确认仅有一台状态为 `device` 的目标手机连接到 ADB。
3. 使用应用包名 `com.example.money_log` 检查手机上的安装状态：
   - 手机未安装应用时，使用 `adb install app/build/outputs/apk/debug/app-debug.apk` 完成首次安装。
   - 手机已安装应用时，只能使用 `adb install -r app/build/outputs/apk/debug/app-debug.apk` 原位更新，禁止先执行 `adb uninstall` 再安装。
   - 首次切换到新签名且旧版本签名不兼容时，可以进行一次卸载后首次安装；执行前必须明确说明卸载会删除应用本地数据。除此情形外不得卸载应用。
4. APK 安装失败时立即停止，不得改装旧 APK，也不得提交本次修改。
5. 仅在测试、编译和安装全部成功后，使用 `auto_commit.ps1` 自动提交全部本次修改。提交信息必须遵循 `AUTO_COMMIT_RULES.md`，使用 `C11005-NNNN` 递增编号和“修改日期 / 修改编号 / 修改内容 / 细节 / 修改备注”格式。
6. 自动提交后保持修改只存在于本地仓库，不得自动执行 `git push`。只有用户在当前任务中明确要求 push 时，才允许推送到 GitHub。
