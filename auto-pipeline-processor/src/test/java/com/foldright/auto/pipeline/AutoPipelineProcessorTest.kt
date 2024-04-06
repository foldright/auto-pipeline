package com.foldright.auto.pipeline

import com.foldright.auto.pipeline.processor.AutoPipelineProcessor
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import java.io.File

/**
 * [`kotlin-compile-testing` usage](https://github.com/ZacSweers/kotlin-compile-testing)
 */
class AutoPipelineProcessorTest : AnnotationSpec() {
    @OptIn(ExperimentalCompilerApi::class)
    @Test
    fun test_AutoPipelineProcessor() {
        val compileResult = KotlinCompilation().apply {
            sources = listOf(javaSourceFileOnClassPath("test1/ConfigSource.java"))
            annotationProcessors = listOf(AutoPipelineProcessor())

            inheritClassPath = true

            // see compile diagnostics in real time?
            verbose = false

            // set work dir via `io.kotest.engine.spec.TempdirKt.tempdir`
            // so auto delete the temp dir after test
            workingDir = tempdir(AutoPipelineProcessorTest::class.qualifiedName)
        }.compile()

        compileResult.exitCode shouldBe KotlinCompilation.ExitCode.OK

        val sourcesGenerated = compileResult.sourcesGeneratedByAnnotationProcessor
        sourcesGenerated shouldHaveSize 5
        sourcesGenerated.forEach {
            println("\ncontent of generated ${it.name}(${it.canonicalPath}):\n${it.readText()}")
        }
    }

    @Suppress("SameParameterValue")
    private fun javaSourceFileOnClassPath(filePath: String): SourceFile = SourceFile.java(
        File(filePath).name,
        javaClass.classLoader.getResource(filePath)!!.readText()
    )
}
