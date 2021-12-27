# auto-pipeline-example

the example project for `auto-pipline`. Including below examples:

- implementation using `auto-pipline`
    - [`ConfigSource`](src/main/java/com/foldright/example/config/ConfigSource.java)
    - [`LoadBalancer`](src/main/java/com/foldright/example/lb/LoadBalancer.java)
    - [`Merger`](src/main/java/com/foldright/example/merge/Merger.java)
    - [`Merger2`](src/main/java/com/foldright/example/merge2/Merger2.java)
    - [`Channel`](src/main/java/com/foldright/example/grpc/Channel.java)
- usage of pipline implementation by test code:
    - [`ConfigSourceTest`](src/test/java/com/foldright/example/config/pipeline/ConfigSourceTest.kt)
    - `LoadBalancerTest` WIP
    - `MergerTest` WIP
    - `Merger2Test` WIP
    - `ChannelTest` WIP

## run examples by `maven`

simple run from root project:

```shell
# cd to the project root dir
cd ..

./mvnw install
```

## run examples by `gradle`

First install the pipeline to maven local:

```shell
# cd to the project root dir
cd ..

./mvnw install -Dmaven.test.skip
```

Then run the examples:

```shell
# cd to the example project
cd auto-pipeline-example

./gradlew test 
```
