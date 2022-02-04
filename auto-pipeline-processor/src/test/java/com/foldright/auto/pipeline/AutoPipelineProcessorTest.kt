package com.foldright.auto.pipeline

import com.foldright.auto.pipeline.processor.AutoPipelineProcessor
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.jetbrains.kotlin.konan.file.File

/**
 * [`kotlin-compile-testing` usage](https://github.com/tschuchortdev/kotlin-compile-testing)
 */
class AutoPipelineProcessorTest : AnnotationSpec() {
    @Test
    fun test_AutoPipelineProcessor() {
        val file = "test1/ConfigSource.java"

        val compileResult = KotlinCompilation().apply {
            sources = listOf(SourceFile.java(file.fileName(), file.contentFromClassPath()))
            annotationProcessors = listOf(AutoPipelineProcessor())

            inheritClassPath = true

            // see compile diagnostics in real time?
            verbose = false
        }.compile()

        compileResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

        val sourcesGenerated = compileResult.sourcesGeneratedByAnnotationProcessor
        sourcesGenerated shouldHaveSize 5
        sourcesGenerated.forEach {
            println("\ncontent of generated ${it.name}(${it.canonicalPath}):\n${it.readText()}")
        }
    }

    private fun String.fileName() = File(this).name

    private fun String.contentFromClassPath() =
        AutoPipelineProcessorTest::class.java.classLoader.getResource(this)!!.readText()
}
