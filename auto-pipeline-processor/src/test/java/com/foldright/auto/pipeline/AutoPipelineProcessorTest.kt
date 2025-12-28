package com.foldright.auto.pipeline

import com.foldright.auto.pipeline.processor.AutoPipelineProcessor
import com.google.testing.compile.Compilation
import com.google.testing.compile.Compiler
import com.google.testing.compile.JavaFileObjects
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import javax.tools.JavaFileObject.Kind

/**
 * [`compile-testing` usage](https://github.com/google/compile-testing)
 */
class AutoPipelineProcessorTest : AnnotationSpec() {
    @Test
    fun test_AutoPipelineProcessor() {
        val compiled = Compiler.javac().withProcessors(AutoPipelineProcessor())
            .compile(JavaFileObjects.forResource("test1/ConfigSource.java"))

        compiled.errors().size shouldBe 0
        compiled.warnings().size shouldBe 0

        compiled.status() shouldBe Compilation.Status.SUCCESS
        compiled.generatedFiles().filter { it.kind == Kind.SOURCE } shouldHaveSize 5
        compiled.generatedFiles()
            .filter { it.kind == Kind.SOURCE }
            .forEach {
                println("\ncontent of generated ${it.name}:\n${it.getCharContent(false)}")
            }
    }

    @Test
    fun test_AutoPipelineProcessor_Duplex() {

        val compiled = Compiler.javac().withProcessors(AutoPipelineProcessor())
            .compile(JavaFileObjects.forResource("test1/RPC.java"))

        compiled.errors().size shouldBe 0
        compiled.warnings().size shouldBe 0

        compiled.status() shouldBe Compilation.Status.SUCCESS
        compiled.generatedFiles().filter { it.kind == Kind.SOURCE } shouldHaveSize 5
        compiled.generatedFiles()
            .filter { it.kind == Kind.SOURCE }
            .forEach {
                println("\ncontent of generated ${it.name}:\n${it.getCharContent(false)}")
            }
    }

    @Test
    fun test_AutoPipelineProcessor_Generic() {
        val compiled = Compiler.javac().withProcessors(AutoPipelineProcessor())
            .compile(JavaFileObjects.forResource("test1/Channel.java"))

        compiled.errors().size shouldBe 0

        compiled.status() shouldBe Compilation.Status.SUCCESS
        compiled.generatedFiles().filter { it.kind == Kind.SOURCE } shouldHaveSize 5
        compiled.generatedFiles()
            .filter { it.kind == Kind.SOURCE }
            .forEach {
                println("\ncontent of generated ${it.name}:\n${it.getCharContent(false)}")
            }
    }
}
