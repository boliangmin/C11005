# 自动提交规则

本项目使用 `auto_commit.ps1` 生成统一格式的 Git 提交信息，并通过
`.auto_commit_state.json` 维护递增编号。

## 编号规则

- 提交编号前缀：`C11005`
- 初始提交编号：`C11005-0001`
- 每次成功提交后，状态文件中的 `nextNumber` 自动加 1
- 提交失败时回滚编号；没有待提交变更时不消耗编号

## 提交规则

- 每次项目修改完成并验证后，使用 `auto_commit.ps1` 提交
- 默认提交工作区内全部变更；需要限制范围时使用 `-Paths`
- `Content` 概括修改目标，`Details` 列出主要修改点
- 无额外备注时，`Remark` 使用默认值 `NULL`

## 开发完成流程

每次修改完成后，必须严格按以下顺序执行：

1. 编译工程并生成最新 APK。
2. 仅在编译成功后，按照本文件的提交规则执行 `auto_commit.ps1`。
3. 仅在提交成功后，将本次编译生成的 APK 安装到已连接的手机中。

编译或提交任一步骤失败时必须停止后续流程，不得跳过失败步骤，也不得安装旧 APK。

## 提交信息格式

```text
修改日期： yyyy.MM.dd HH:mm:ss
1.修改编号： C11005-0001
2.修改内容： <Content>
3.细节：
1).<Details 第一项>
2).<Details 第二项>
4.修改备注： <Remark>
```

## 使用示例

```powershell
.\auto_commit.ps1 `
    -Content "修复应用运行配置" `
    -Details "调整最低 Android SDK|验证 Debug 构建" `
    -Remark "NULL" `
    -SkipRunApp
```

`Details` 和 `Paths` 支持使用 `|`、`;` 或 `,` 分隔。
