---
name: accessing-jvm-from-java
description: 当 Java 脚本需要访问 JVM 类、调用 Java/Kotlin 成员或传递函数 callback 时使用。
---

# 从 Java 访问 JVM

Java 项目脚本由 Janino 执行，构建时将 .java 转为设备执行用的 .javas。按 Java 编译器规则写 import、局部类型与 lambda；全局变量使用生成源码所示的注入形式。

## 类与对象

```java
StringBuilder builder = new StringBuilder("M8Test");
builder.append(" Java");
if (!"M8Test Java".equals(builder.toString())) throw new AssertionError("StringBuilder");
```

先确认依赖已加载到当前脚本 classloader。声明文件或代码补全存在不等于运行时类可见。方法重载、静态字段、嵌套类、异常等边界按目标语言的生成代码和实际设备行为验证。

## 函数 callback

Java lambda 可传给 SAM 和目标签名兼容的 Kotlin 函数类型。模板 primary.java 中 Compose 调用是 callback 实例。

## 函数 callback 示例

Java lambda 可以按目标 Kotlin 函数类型传入 callback。模板 Test Control 通过 Logger.withContext 实际验证 callback 被调用并获得参数：

```java
final boolean[] called = new boolean[] { false };
$logger.withContext(context -> { called[0] = context != null; return null; });
if (!called[0]) throw new AssertionError("callback");
```

## 类型转换边界

按 Java 声明提供准确 primitive、boxed、数组和泛型类型。编译通过不代表运行时依赖加载成功；重载及异常边界需要设备验证。

逐项记录已测试的 primitive/boxed、字符串、nullable、数组、集合、varargs、重载和异常行为。未经执行验证的转换写为待验证，不根据其他脚本语言推断。

## 验证

本模板的 java-template-jvm-interop Test Control 场景会实际调用 JVM StringBuilder，并验证宿主数组工厂和 wrapper。运行当前项目支持的选中 Test Control 任务并检查 summary 中该 requestId 为 passed；若当前模板没有注册相应任务，使用模板设备运行任务并确认成功日志。Gradle 使用 -q。
