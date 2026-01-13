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

## Overview
`auto-pipeline` is a source code generator that automatically generates pipeline components for your interfaces. It helps keep your project smaller, simpler, and more extensible by leveraging the Chain of Responsibility pattern. 💡

`auto-pipeline` is implemented as an [`annotation-processor`](https://docs.oracle.com/javase/8/docs/api/javax/annotation/processing/package-summary.html), inspired by Google's [`Auto`](https://github.com/google/auto). ❤️

## Features
- 🚀 **Zero Runtime Dependencies** - Only needed at compile time
- 🎯 **Simple to Use** - Just add one annotation to your interface
- 🔌 **Highly Extensible** - Based on Chain of Responsibility pattern
- 🛠 **Type Safe** - Generates type-safe Java code
- 📦 **Lightweight** - No reflection, no runtime overhead

## Quick Start

### Prerequisites
- Java 8 or above
- Maven or Gradle build system

### 1. Add Dependencies

for `maven` project:

```xml
<dependencies>
    <!-- The @AutoPipeline annotation -->
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
                <!-- Configure annotation processor -->
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

for `gradle` project:

```groovy
/*
 * Gradle Kotlin DSL
 */
// the auto-pipeline annotation will be used in your interface type
compileOnly("com.foldright.auto-pipeline:auto-pipeline-annotations:0.4.0")
// the auto-pipeline annotation processor will generate the pipeline classes for the interface.
// use "annotationProcessor" scope because it's only needed at annotation processing time.
annotationProcessor("com.foldright.auto-pipeline:auto-pipeline-processor:0.4.0")

/*
 * Gradle Groovy DSL
 */
compileOnly 'com.foldright.auto-pipeline:auto-pipeline-annotations:0.4.0'
annotationProcessor 'com.foldright.auto-pipeline:auto-pipeline-processor:0.4.0'
```

`auto-pipeline` has published to maven central, click here
to [find the latest version](https://search.maven.org/search?q=g:com.foldright.auto-pipeline).

### 2. Annotate Your Interface
Let's look at a simple example using a `ConfigSource` interface:

given an interface named `ConfigSource`, the `ConfigSource` has the `get()` method, input a string as key and output a string as the value.
like this:

```java
public interface ConfigSource {
    String get(String key);
}
```

say, we want `ConfigSource#get()` has some extensibility, so we decide to apply the `chain of responsibility` pattern to it for extensibility.

Now it's `auto-pipeline`'s turn to play a role, we simply add `@AutoPipelin` to `ConfigSource`：

```java
@AutoPipeline
public interface ConfigSource {
    String get(String key);
}
```

`auto-pipeline-processor` will auto generate pipeline java files for `ConfigSource` into subpackage `pipeline` when compiled:

- `ConfigSourceHandler`  
  the responsibility interface we want to implement for extensibility
- `ConfigSourcePipeline`  
  the chain
- `ConfigSourceHandlerContext`
- `AbstractConfigSourceHandlerContext`
- `DefaultConfigSourceHandlerContext`

### 3. Implement Handlers
We can implement handlers to add functionality to our pipeline:

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

### 4. Use the Pipeline
Create and use the pipeline by composing handlers:

```java
Map<String, String> mapConfig = new HashMap<String, String>();
mapConfig.put("hello", "world");
ConfigSourceHandler mapConfigSourceHandler = new MapConfigSourceHandler(mapConfig);

ConfigSource pipeline = new ConfigSourcePipeline()
        .addLast(mapConfigSourceHandler)
        .addLast(SystemConfigSourceHandler.INSTANCE);
```

now, we can use the `pipeline.get(...)` to invoke the chain! 🎉

```java
pipeline.get("hello");
// get value "world"
// from mapConfig / mapConfigSourceHandler

pipeline.get("java.specification.version")
// get value "1.8"
// from system properties / SystemConfigSourceHandler
```

check the runnable [test case](auto-pipeline-examples/src/test/java/com/foldright/examples/config/pipeline/ConfigSourceTest.kt) for details.

## Documentation
For more detailed information and advanced usage, please check out the [auto-pipeline documentation](https://foldright.io/auto-pipeline/).

## Examples
For more examples and use cases, check out our [examples project](auto-pipeline-examples) and its test cases.

## License
Apache License 2.0
