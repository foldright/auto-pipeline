# <div align="center"><a href="#"><img src="https://github.com/user-attachments/assets/358ed4a2-cfe2-4a63-87d5-8c7c9bf3362f" alt="auto-pipeline 🚀"></a></div>

<p align="center">
<a href="README.md">English</a> | <a href="README_zh-Hans.md">中文</a>
</p>

<p align="center">
<a href="https://github.com/foldright/auto-pipeline/actions/workflows/ci.yml"><img src="https://img.shields.io/github/actions/workflow/status/foldright/auto-pipeline/ci.yml?branch=main&logo=github&logoColor=white&label=fast ci" alt="Github Workflow Build Status"></a>
<a href="https://github.com/foldright/auto-pipeline/actions/workflows/strong-ci.yml"><img src="https://img.shields.io/github/actions/workflow/status/foldright/auto-pipeline/strong-ci.yml?branch=main&logo=github&logoColor=white&label=strong ci" alt="Github Workflow Build Status"></a>
<a href="https://codecov.io/gh/foldright/auto-pipeline"><img src="https://img.shields.io/codecov/c/github/foldright/auto-pipeline/main?logo=codecov&logoColor=white" alt="Coverage Status"></a>
<a href="https://openjdk.java.net/"><img src="https://img.shields.io/badge/Java-8+-green?logo=openjdk&logoColor=white" alt="Java support"></a>
<a href="https://www.apache.org/licenses/LICENSE-2.0.html"><img src="https://img.shields.io/github/license/foldright/auto-pipeline?color=4D7A97&logo=apache" alt="License"></a>
<a href="https://search.maven.org/search?q=g:com.foldright.auto-pipeline"><img src="https://img.shields.io/maven-central/v/com.foldright.auto-pipeline/auto-pipeline-annotations?logo=apache-maven" alt="Maven Central"></a>
<a href="https://github.com/foldright/auto-pipeline/releases"><img src="https://img.shields.io/github/release/foldright/auto-pipeline.svg" alt="GitHub release"></a>
<a href="https://github.com/foldright/auto-pipeline/stargazers"><img src="https://img.shields.io/github/stars/foldright/auto-pipeline" alt="GitHub Stars"></a>
<a href="https://github.com/foldright/auto-pipeline/fork"><img src="https://img.shields.io/github/forks/foldright/auto-pipeline" alt="GitHub Forks"></a>
<a href="https://github.com/foldright/auto-pipeline/issues"><img src="https://img.shields.io/github/issues/foldright/auto-pipeline" alt="GitHub issues"></a>
<a href="https://github.com/foldright/auto-pipeline/graphs/contributors"><img src="https://img.shields.io/github/contributors/foldright/auto-pipeline" alt="GitHub Contributors"></a>
<a href="https://github.com/foldright/auto-pipeline"><img src="https://img.shields.io/github/repo-size/foldright/auto-pipeline" alt="GitHub repo size"></a>
<a href="https://gitpod.io/#https://github.com/foldright/auto-pipeline"><img src="https://img.shields.io/badge/Gitpod-ready--to--code-green?label=gitpod&logo=gitpod&logoColor=white" alt="gitpod: Ready to Code"></a>
</p>

`auto-pipeline` 是一个代码生成器，用于自动生成组件的pipeline。帮助你保持项目更小、更简单、更具扩展性。💡

`auto-pipeline` 是一个用于生成 `Pipeline` 的[`注解处理器`](https://docs.oracle.com/javase/8/docs/api/javax/annotation/processing/package-summary.html)，灵感来自于 Google 的 [`Auto`](https://github.com/google/auto)。❤️

更多信息，请查看 [auto-pipeline 文档](https://foldright.io/auto-pipeline/)。

## 快速示例

以下是一个简要介绍。详细信息请查看 [示例项目](auto-pipeline-examples) 及其测试用例。

## 快速开始

`auto-pipeline` 需要 Java 8 或更高版本。

### 0. 添加 `auto-pipeline` 依赖

对于 `Maven` 项目：

```xml
<dependencies>
    <!-- @AutoPipeline 注解 -->
    <dependency>
        <groupId>com.foldright.auto-pipeline</groupId>
        <artifactId>auto-pipeline-annotations</artifactId>
        <version>0.4.0</version>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <!-- 配置注解处理器 -->
                <annotationProcessorPaths>
                    <path>
                        <groupId>com.foldright.auto-pipeline</groupId>
                        <artifactId>auto-pipeline-processor</artifactId>
                        <version>0.4.0</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
```

对于 `Gradle` 项目：

```groovy
/*
 * Gradle Kotlin DSL
 */
compileOnly("com.foldright.auto-pipeline:auto-pipeline-annotations:0.4.0")
// auto-pipeline 注解处理器将为接口生成管道类
annotationProcessor("com.foldright.auto-pipeline:auto-pipeline-processor:0.4.0")

/*
 * Gradle Groovy DSL
 */
compileOnly 'com.foldright.auto-pipeline:auto-pipeline-annotations:0.4.0'
annotationProcessor 'com.foldright.auto-pipeline:auto-pipeline-processor:0.4.0'
```

`auto-pipeline` 已发布到 Maven 中央仓库，[查看最新版本](https://search.maven.org/search?q=g:com.foldright.auto-pipeline)。

### 1. 使用 `@AutoPipeline` 为接口自动生成 Pipeline

在你的接口上添加 `@AutoPipeline` 注解，`auto-pipeline` 将在编译时为该接口生成一些 Java 文件。

以 [`ConfigSource`](auto-pipeline-examples/src/main/java/com/foldright/examples/config/ConfigSource.java) 为例：

比如名为 `ConfigSource` 的接口，该接口有 `get()` 方法，像这样：

```java
public interface ConfigSource {
    String get(String key);
}
```

假设我们希望 `ConfigSource#get()` 具有一些扩展性，所以我们决定对其应用`Pipeline`模式以实现扩展。

现在就让 `auto-pipeline` 出场，我们只需要在 `ConfigSource` 上添加 `@AutoPipeline`：

```java
@AutoPipeline
public interface ConfigSource {
    String get(String key);
}
```

`auto-pipeline-processor` 在编译时会自动为 `ConfigSource` 在 `pipeline` package 中生成 Java 文件：

- `ConfigSourceHandler`  
  我们想要实现的责任接口以实现扩展性
- `ConfigSourcePipeline`  
  责任链
- `ConfigSourceHandlerContext`
- `AbstractConfigSourceHandlerContext`
- `DefaultConfigSourceHandlerContext`

### 2. 为 Pipeline 实现你的处理器

我们可以实现 `MapConfigSourceHandler` 和 `SystemConfigSourceHandler`（它们都在 [ConfigSource handler 示例](auto-pipeline-examples/src/main/java/com/foldright/examples/config/handler) 中）：

```java
public class MapConfigSourceHandler implements ConfigSourceHandler {
    private final Map<String, String> map;

    public MapConfigSourceHandler(Map<String, String> map) {
        this.map = map;
    }

    @Override
    public String get(String key, ConfigSourceHandlerContext context) {
        String value = map.get(key);
        if (StringUtils.isNotBlank(value)) {
            return value;
        }
        return context.get(key);
    }
}

public class SystemConfigSourceHandler implements ConfigSourceHandler {
    public static final SystemConfigSourceHandler INSTANCE = new SystemConfigSourceHandler();

    @Override
    public String get(String key, ConfigSourceHandlerContext context) {
        String value = System.getProperty(key);
        if (StringUtils.isNotBlank(value)) {
            return value;
        }
        return context.get(key);
    }
}
```

### 3. 使用 Pipeline

通过组合 `ConfigSourceHandler` 创建一个 `ConfigSourcePipeline`，可以作为 `ConfigSource` 的入口：

```java
Map<String, String> mapConfig = new HashMap<String, String>();
mapConfig.put("hello", "world");
ConfigSourceHandler mapConfigSourceHandler = new MapConfigSourceHandler(mapConfig);

ConfigSource pipeline = new ConfigSourcePipeline()
        .addLast(mapConfigSourceHandler)
        .addLast(SystemConfigSourceHandler.INSTANCE);
```

现在，我们可以使用 `pipeline.get(...)` 来调用责任链了！🎉

```java
pipeline.get("hello");
//  "world"

pipeline.get("java.specification.version")
// "1.8"
```

详细信息请查看可运行的[单测用例](auto-pipeline-examples/src/test/java/com/foldright/examples/config/pipeline/ConfigSourceTest.kt)
