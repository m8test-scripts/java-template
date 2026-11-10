# 01: Java 模板展示双语脚本项目

**What to build:** Java 模板生成的脚本项目能根据 Development Kit 宿主语言显示本地化 metadata 和计数器 UI，并在页面中标明 Java 语言。

**Blocked by:** None.

**Status:** resolved

- [x] 添加英文和中文 BCP 47 catalog，包括项目描述、计数标签、按钮及语言标识；仅中文覆盖项目名称。
- [x] 主入口 UI 通过 `$stringResources.getString(key)` 读取文案，并在按钮下方显示语言标识。
- [x] README 记录资源位置、locale 行为、Java API 和 metadata 键。
- [x] `generateJavaGlobalVariables` 与 `rerunJava` 在 `192.168.31.207` 上通过；英文和中文 UI 均显示正确文案，按钮计数交互正常，生成目录中的 catalogs 与源资源一致。

## Answer

Java 模板现在包含英文和中文 catalogs，入口 UI 使用 `$stringResources` 本地化计数标签、按钮和按钮下方的语言标识；中文 catalog 还覆盖项目名称并翻译项目描述。`generateJavaGlobalVariables` 在 `.207` 生成了 `$stringResources` 声明。英文和 `zh-Hans-CN` 下 `rerunJava` 均退出码为 0，截图确认 UI 正确；中文按钮点击后计数从 0 变为 1。截图：`/tmp/m8test-java-en.png`、`/tmp/m8test-java-zh.png`、`/tmp/m8test-java-zh-counted.png`。验证后 Development Kit locale 已恢复为空覆盖，继续跟随设备 `zh-Hans-CN`。
