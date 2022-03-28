package com.foldright.examples.reader.pipeline

import com.foldright.examples.reader.Reader
import com.foldright.examples.reader.handler.DefaultValueReader
import com.foldright.examples.reader.handler.FileReader
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Paths


class ConfigSourceTest : AnnotationSpec() {
    private lateinit var pipeline: Reader


    @Test
    fun testFileReader() {
        val resource = this::class.java.getResource("/reader/reader.txt")!!
        val path = Paths.get(resource.toURI())
        val fileReader = FileReader(path, Charsets.UTF_8.name())

        val defaultReader = DefaultValueReader("default")

        pipeline = ReaderPipeline()
            .addLast(fileReader)
            .addLast(defaultReader)



        pipeline.read() shouldBe resource.readText(Charsets.UTF_8)
    }

    @Test
    fun testFileNotExist() {
        val path = Paths.get("not-exist-file")
        val fileReader = FileReader(path, Charsets.UTF_8.name())

        val defaultReader = DefaultValueReader("default")

        pipeline = ReaderPipeline()
            .addLast(fileReader)
            .addLast(defaultReader)

        pipeline.read() shouldBe "default"
    }
}
