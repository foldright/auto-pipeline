# auto-pipeline-examples

The examples project for `auto-pipline`. Including below examples:

- implementation using `auto-pipline`
    - [`ConfigSource`](src/main/java/com/foldright/examples/config/ConfigSource.java)
    - [`RPC`](src/main/java/com/foldright/examples/duplexing/RPC.java)
    - [`Channel`](src/main/java/com/foldright/examples/grpc/Channel.java)
    - [`LoadBalancer`](src/main/java/com/foldright/examples/lb/LoadBalancer.java)
    - [`Merger`](src/main/java/com/foldright/examples/merge/Merger.java)
    - [`IntervalMerger`](src/main/java/com/foldright/examples/merge/interval/IntervalMerger.java)
    - [`Reader`](src/main/java/com/foldright/examples/reader/Reader.java)
    
- usage of pipline implementation by test code:
    - [`ConfigSourceTest`](src/test/java/com/foldright/examples/config/pipeline/ConfigSourceTest.kt)
    - [`RPC`](src/test/java/com/foldright/examples/duplexing/pipeline/RPCTest.kt)
    - [`ChannelTest`](src/test/java/com/foldright/examples/grpc/pipeline/ChannelTest.kt)
    - `LoadBalancerTest` WIP
    - `MergerTest` WIP
    - `IntervalMergerTest` WIP
    - [`ReaderTest](src/test/java/com/foldright/examples/reader/pipeline/ReaderTest.kt)
    

## Run examples by `maven`

Run from root project:

```shell
# cd to the project root dir
cd ..

./mvnw install
```

## Run examples by `gradle`

First install the pipeline to maven local:

```shell
# cd to the project root dir
cd ..

./mvnw install -Dmaven.test.skip
```

Then run the examples:

```shell
# cd to the examples project
cd auto-pipeline-examples

./gradlew test 
```
