# Quick Start

`auto-pipeline` requires java 8 or above.

## 0. add `auto-pipeline` dependencies

`auto-pipeline` has published to maven central, click here
to [find the latest version](https://search.maven.org/search?q=g:com.foldright.auto-pipeline).

### for `maven` project:

```xml
<dependencies>
    <!--
        the auto-pipeline annotation processor will generate
          the pipeline classes for the interface.
        annotation processor dependency should be "provided" scope,
          because it's only needed at compile time.
    -->
    <dependency>
        <groupId>com.foldright.auto-pipeline</groupId>
        <artifactId>auto-pipeline-processor</artifactId>
        <version>0.3.0</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

for `gradle` project:

```kotlin
/*
 * Gradle Kotlin DSL
 */
// the auto-pipeline annotation will be used in your interface type
compileOnly("com.foldright.auto-pipeline:auto-pipeline-annotations:0.3.0")
// the auto-pipeline annotation processor will generate the pipeline classes for the interface.
// use "annotationProcessor" scope because it's only needed at annotation processing time.
annotationProcessor("com.foldright.auto-pipeline:auto-pipeline-processor:0.3.0")

/*
 * Gradle Groovy DSL
 */
compileOnly 'com.foldright.auto-pipeline:auto-pipeline-annotations:0.3.0'
annotationProcessor 'com.foldright.auto-pipeline:auto-pipeline-processor:0.3.0'
```

## 1. using `@AutoPipeline` to auto generate pipeline for your interface

annotate `@AutoPipeline` to your interface, and `auto-pipeline` will generate some java files for the interface at compile time.

let's check the [`ConfigSource`](https://github.com/foldright/auto-pipeline/blob/main/auto-pipeline-examples/src/main/java/com/foldright/examples/config/ConfigSource.java) as an example:

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

- `ConfigSourceHandler` the responsibility interface we want to implement for extensibility
- `ConfigSourcePipeline` the chain
- `ConfigSourceHandlerContext`
- `AbstractConfigSourceHandlerContext`
- `DefaultConfigSourceHandlerContext`

## Implementing your handler for pipeline

we can implement `MapConfigSourceHandler` and `SystemConfigSourceHandler` (they are all in the [ConfigSource handler example](https://github.com/foldright/auto-pipeline/blob/main/auto-pipeline-examples/src/main/java/com/foldright/examples/config/handler)):

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
```

```java
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

## 3. use the pipeline

create a `ConfigSourcePipeline` by composing `ConfigSourceHandler`s which can ben an entrance of the `ConfigSource`:

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

## What's next:

