# Java 模板脚本项目 i18n

Status: resolved

## Problem Statement

Java 模板入口 UI 使用固定中文文案，项目显示名称使用单一默认值。Development Kit 切换语言后，Java 示例仍显示中文；多个语言模板的入口 UI 也缺少明确的语言标识。

## Solution

为 Java 模板添加英文和中文 BCP 47 字符串目录。入口 UI 通过宿主提供的 `$stringResources.getString(key)` 读取计数标签、按钮文案和完整的 Java 语言标识。中文目录翻译项目名称和描述，英文项目名称继续使用 `appName` 配置。README 说明资源目录、键名和 Java API。

## Requirements

1. `java/src/main/resources/strings/en.json` 与 `zh.json` 提供计数标签、按钮文案、语言标识以及项目描述。
2. `zh.json` 提供 `m8test.project.name`；`en.json` 不覆盖项目名称，以保留 `projectConfig.name` 默认值和 `-PappName`。
3. 主 UI 的所有示例文案均通过 `$stringResources.getString(key)` 获取，且按钮下方显示已本地化的 `Script language: Java` / `脚本语言：Java`。
4. `README.md` 说明 BCP 47 文件名、`res/strings` 打包约定、宿主 locale 跟随行为、Java API 与 metadata 键。
5. 保持计数器行为、side scripts、事件载荷、包名与诊断信息不变。

## Testing Decisions

- 使用 `generateJavaGlobalVariables` 在 `192.168.31.207:5555` 生成并确认 `$stringResources` 全局变量，再运行模板实际提供的 `rerunJava` 任务。
- 分别使用 Development Kit 英文 locale 和中文 locale，确认项目 metadata 与计数 UI、按钮及按钮下方语言标识正确显示。
- Java 模板没有独立的 `extensions/java` 仓库；如果 Java Language Extension 不可用，使用本模板的 `:java:installJavaExtension`，并将设备序列固定到 `.207`。
- 检查生成的 `build/project/res/strings` 包含两个目录文件，并与模板输入资源一致。
- 保存英文与中文设备 UI 截图作为可见证据；验证后恢复 Development Kit 跟随系统语言。

## Out of Scope

不修改宿主、Gradle 插件、其他模板仓库、side scripts 或 README 的文档语言。
