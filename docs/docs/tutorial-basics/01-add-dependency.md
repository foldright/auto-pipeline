---
sidebar_position: 1
---

# Add dependency

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
