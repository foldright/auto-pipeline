---
sidebar_position: 1
---
# Quick start

It's very easy to use `Auto-Pipeline`, just follow the steps.

## Step1. add dependency

`auto-pipeline` has published to maven central, click here
to [find the latest version](https://search.maven.org/search?q=g:com.foldright.auto-pipeline). current the latest version is `0.3.0`


import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

<Tabs>
  <TabItem value="Maven" label="Maven" default>
    ```xml title="pom.xml"
    <dependency>
        <groupId>com.foldright.auto-pipeline</groupId>
        <artifactId>auto-pipeline-processor</artifactId>
        <version>0.3.0</version>
        <scope>provided</scope>
    </dependency>
    ```
  </TabItem>
  <TabItem value="Gradle-Kotlin" label="Gradle (Kotlin DSL)">
    ```kotlin title="build.gradle.kts"
    // the auto-pipeline annotation will be used in your interface type
    compileOnly("com.foldright.auto-pipeline:auto-pipeline-annotations:0.3.0")
    // the auto-pipeline annotation processor will generate the pipeline classes for the interface.
    // use "annotationProcessor" scope because it's only needed at annotation processing time.
    annotationProcessor("com.foldright.auto-pipeline:auto-pipeline-processor:0.3.0")
    ```
  </TabItem>
  <TabItem value="Gradle-Gradle" label="Gradle (Groovy DSL)">
    ```groovy title="build.gradle"
    compileOnly 'com.foldright.auto-pipeline:auto-pipeline-annotations:0.3.0'
    annotationProcessor 'com.foldright.auto-pipeline:auto-pipeline-processor:0.3.0'
    ```
  </TabItem>
</Tabs>

:::info[note the dependency scope!]
The auto-pipeline annotation processor will generate the pipeline classes for the interface.

Annotation processor dependency should be "provided" scope, because it's only needed at compile time.
:::

## Step2. Using `@AutoPipeline`

Very simple and easy.

### Define Your interface
Let's define your business interface called `ConfigSource`.

The `ConfigSource` has the `get()` method, input a string as key and output a string as the value.
like this:

```java title="ConfigSource.java"
public interface ConfigSource {
    String get(String key);
}
```

### Annotation the interface

If we want `ConfigSource` has some extensibility, we can apply the `chain of responsibility` pattern to it for extensibility.

We can simply add `@AutoPipelin` to `ConfigSource` to make `ConfigSource` more extensible：

```java title="ConfigSource.java"
@AutoPipeline // add @AutoPipeline here!
public interface ConfigSource {
    String get(String key);
}
```

And then we compile our code, `auto-pipeline-processor` will auto generate pipeline java files for `ConfigSource` into subpackage `pipeline`:

- `ConfigSourceHandler.java` the responsibility interface we want to implement for extensibility
- `ConfigSourcePipeline.java` the chain
- `ConfigSourceHandlerContext.java`
- `AbstractConfigSourceHandlerContext.java`
- `DefaultConfigSourceHandlerContext.java`

Oh yeah! We add `@AutoPipeline` and we get 5 files for free. The 5 files make up our ConfigSource pipeline!

## Step3. write your business code

After generating java files, we can now add our business code to the implements.

we can implement `MapConfigSourceHandler` and `SystemConfigSourceHandler` (they are all in the [ConfigSource handler example](https://github.com/foldright/auto-pipeline/blob/main/auto-pipeline-examples/src/main/java/com/foldright/examples/config/handler)):

```java title="MapConfigSourceHandler.java"
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
```java title="SystemConfigSourceHandler.java"
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

## Step4. using your pipeline

Now you can create the `ConfigSourcePipeline` by composing `ConfigSourceHandler`s.

And using the pipeline as the `ConfigSource`:

```java title="Main.java"
public class Main {
    public static void main(String[] args) {
        Map<String, String> mapConfig = new HashMap<String, String>();
        mapConfig.put("hello", "world");
        ConfigSourceHandler mapConfigSourceHandler = new MapConfigSourceHandler(mapConfig);

        ConfigSource pipeline = new ConfigSourcePipeline()
                .addLast(mapConfigSourceHandler)
                .addLast(SystemConfigSourceHandler.INSTANCE);    
    }
}
```

:::info[the ConfigSourcePipeline!]
In fact, the `ConfigSourcePipeline.java` implements ConfigSource.

So we can use `ConfigSourcePipeline` as `ConfigSource`.
:::

Now, we can use the `pipeline.get(...)` to invoke the chain! 🎉

```java title="Main.java"
public class Main {
    public static void main(String[] args) {
        // ...
        
        pipeline.get("hello");
        // get value "world"
        // from mapConfig / mapConfigSourceHandler

        pipeline.get("java.specification.version");
        // get value "1.8"
        // from system properties / SystemConfigSourceHandler
    }
}
```

That's all, the `@AutoPipeline` help you generate the Pipeline Design Pattern Code! It's never been easier!


## What's next?
- check the runnable [test case](https://github.com/foldright/auto-pipeline/blob/main/auto-pipeline-examples/src/test/java/com/foldright/examples/config/pipeline/ConfigSourceTest.kt) for details.
