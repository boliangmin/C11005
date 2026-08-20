# 本地签名目录

Debug 构建只会从本目录读取自定义签名文件。实际 keystore 和包含口令的 `keystore.properties` 均已被 Git 忽略，不得提交或推送。

默认文件名为 `C11005-debug.keystore`。将桌面生成的同名文件复制到本目录后，`app/build.gradle.kts` 会自动读取本地 `keystore.properties` 并用于 Debug 构建。

如果本地配置文件丢失，可复制 `keystore.properties.example` 为 `keystore.properties`，再填写真实值。也可以通过 `C11005_DEBUG_STORE_PASSWORD`、`C11005_DEBUG_KEY_ALIAS` 和 `C11005_DEBUG_KEY_PASSWORD` Gradle 属性或环境变量提供相应参数。

若本目录中没有 keystore，工程会使用 Android 默认 Debug 签名；若已有 keystore 但签名参数不完整，构建会直接失败，避免生成签名错误的 APK。

