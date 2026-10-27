---
name: accessing-jvm-from-java
description: 从 M8Test Java/Janino 脚本访问 JVM 类型、调用成员、传递 lambda 或使用宿主 wrappers 时使用。
---

# 从 Java 脚本访问 JVM

此模板由 Janino 执行，并可能把 `.java` 转为设备执行用源码。按当前生成结果写 import、局部类型和 lambda；不要把编译补全当成运行时依赖可见的证据。通用宿主 wrapper 语义见 `using-host-values-and-wrappers`。

```java
StringBuilder builder = new StringBuilder("M8Test");
builder.append(" Java");
if (!"M8Test Java".equals(builder.toString())) throw new AssertionError();

int[] raw = $arrays.intArrayOf(3, 1, 2);
var values = $objectWrappers.wrapIntArray(raw);
values.set(0, 7);
if (values.getSize() != 3 || !values.contains(7)) throw new AssertionError();
```

Java lambda 仅传给签名兼容的 SAM/function type；验证真实参数、返回和调用时机。按 Java 声明处理 primitive、boxed、数组与泛型；重载、异步 callback、异常和依赖加载必须按目标接口实测。

修改互操作示例时更新 Java JVM interop Test Control 断言，并用目标引擎验证；编译通过不证明 Janino 运行时可调用。
