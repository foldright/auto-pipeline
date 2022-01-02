# <div align="center"><a href="#"><img src="docs/logo-green.png" alt="auto-pipeline 🚀"></a></div>

<p align="center">
<a href="https://ci.appveyor.com/project/oldratlee/auto-pipeline"><img src="https://img.shields.io/appveyor/ci/oldratlee/auto-pipeline/main?logo=appveyor&amp;logoColor=white" alt="Build Status"></a>
<a href="https://openjdk.java.net/"><img src="https://img.shields.io/badge/Java-8+-green?logo=java&amp;logoColor=white" alt="Java support"></a>
<a href="https://www.apache.org/licenses/LICENSE-2.0.html"><img src="https://img.shields.io/github/license/foldright/auto-pipeline?color=4D7A97" alt="License"></a>
<a href="https://github.com/foldright/auto-pipeline/stargazers"><img src="https://img.shields.io/github/stars/foldright/auto-pipeline" alt="GitHub Stars"></a>
<a href="https://github.com/foldright/auto-pipeline/fork"><img src="https://img.shields.io/github/forks/foldright/auto-pipeline" alt="GitHub Forks"></a>
<a href="https://github.com/foldright/auto-pipeline/issues"><img src="https://img.shields.io/github/issues/foldright/auto-pipeline" alt="GitHub issues"></a>
<a href="https://github.com/foldright/auto-pipeline/graphs/contributors"><img src="https://img.shields.io/github/contributors/foldright/auto-pipeline" alt="GitHub Contributors"></a>
<a href="https://github.com/foldright/auto-pipeline"><img src="https://img.shields.io/github/repo-size/foldright/auto-pipeline" alt="GitHub repo size"></a>
</p>

auto-pipeline is a source code generator that auto generate the component's pipeline. Help you to keep your project
smaller, simpler, and more extensible 💡

auto-pipeline is an annotation-processor for Pipeline generator, which is inspired by
Google's [Auto](https://github.com/google/auto). ❤️

## quick examples

please check the [examples project](auto-pipeline-examples), and it's test case.

## quick start
auto-pipeline require java 8 or above.

### auto-pipeline version

auto-pipeline has published to maven central, click here
to [find the latest version](https://search.maven.org/search?q=g:com.foldright.auto-pipeline).

### for maven project

add dependency to your project:

```xml

<dependencies>
    <!-- the auto-pipeline annotation you will use in your interface type -->
    <dependency>
        <groupId>com.foldright.auto-pipeline</groupId>
        <artifactId>auto-pipeline-annotations</artifactId>
        <version>0.1.0</version>
    </dependency>

    <!-- the auto-pipeline annotation processor, it will generate the pipeline classed for the interface -->
    <!-- the processor's scope can be provided because we only need it at compile time -->
    <dependency>
        <groupId>com.foldright.auto-pipeline</groupId>
        <artifactId>auto-pipeline-processor</artifactId>
        <version>0.1.0</version>
        <scope>provided</scope>
    </dependency>

</dependencies>
```
### using @AutoPipeline 
annotate `@AutoPipeline` to your interface, and auto-pipeline will generate some java file for the interface at compile time.

let's check the [ConfigSource](auto-pipeline-examples/src/main/java/com/foldright/examples/config/ConfigSource.java) as an example:

given an interface named `ConfigSource`, 
the `ConfigSource` has the `get` method, input a string as key and output a string as the value.
like this:

```java
public interface ConfigSource {
    String get(String key);
}
```

say, we want `ConfigSource#get` has some extensibility, so we decide to apply the `chain of responsibility` pattern to it.

Now it's `auto-pipeline`'s turn to play a role, we simply add `@AutoPipelin` to `ConfigSource`：

```java

@AutoPipeline
public interface ConfigSource {
    String get(String key);
}
```

and once's the code compiled, `auto-pipeline-processor` will generate 5 java files for us:

- `ConfigSourceHandler.java`
- `ConfigSourcePipeline.java`
- `ConfigSourceHandlerContext.java`
- `AbstractConfigSourceHandlerContext.java`
- `DefaultConfigSourceHandlerContext.java`

the most import java files are：

- `ConfigSourceHandler.java`: the responsibility interface we want to implement for extensibility
- `ConfigSourcePipeline.java`: the chain

we can implement  `MapConfigSourceHandler` and `SystemConfigSourceHandler` (they are all in the [configSource handler example](auto-pipeline-examples/src/main/java/com/foldright/examples/config/handler)):

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

then, we can create a `ConfigSourcePipeline` which can ben an entrance of the `ConfigSource`:

```java
pipeline = new ConfigSourcePipeline()
            .addLast(new MapConfigSourceHandler(new HashMap<String,String>()))
            .addLast(SystemConfigSourceHandler.INSTANCE);
```

now, we can use the `pipeline.get(...)` to invoke the chain! 🎉 
(check the [test case](auto-pipeline-examples/src/test/java/com/foldright/examples/config/pipeline/ConfigSourceTest.kt) for more detail)

## License

Apache License 2.0



