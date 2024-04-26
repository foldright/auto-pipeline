# Pipeline Direction

the auto-pipeline also introduced the `@PipelineDirection` annotation for the pipeline.

## specify the direction

to specify the direction of the pipeline, which can be used on methods or interfaces(must be annotated with `@AutoPipeline`). 
the default direction is `@Direction.FORWARD`, here's the rules:

- rule1: in the `@AutoPipelin` interface's method, we can:
  - add `Direction.FORWARD` to apply the interface's method, the annotated method will be called from head to tail
  - add `Direction.REVERSE` to apply the interface's method, the annotated method will be called from tail to head
  - if there's no `Direction annotation` to the method, the direction of pipeline will follow the `Direction` in the in the `@AutoPipeline` interface, see rule2
  - `Direction.FORWARD` and `Direction.REVERSE` are mutually exclusive, they cannot coexist
- rule2: in the `@AutoPipeline` interface, we can:
  - add `Direction.FORWARD` to the `@AutoPipeline` interface, the methods declared in this interface will be called from head to tail unless the method is annotated with `Direction` in the method level
  - add `Direction.REVERSE` to the `@AutoPipeline` interface, the methods declared in this interface will be called from tail to head unless the method is annotated with `Direction` in the method level
  - if no `Direction` annotation to the `@AutoPipeline` interface, it equals to add `Direction.FORWARD` to the `@AutoPipeline` interface
  - `Direction.FORWARD` and `Direction.REVERSE` are mutually exclusive, they cannot coexist

for examples, please check the [RPC example](https://github.com/foldright/auto-pipeline/blob/main/auto-pipeline-examples/src/main/java/com/foldright/examples/duplexing/RPC.java) and the [test case](https://github.com/foldright/auto-pipeline/blob/main/auto-pipeline-examples/src/test/java/com/foldright/examples/duplexing/pipeline/RPCTest.kt).
