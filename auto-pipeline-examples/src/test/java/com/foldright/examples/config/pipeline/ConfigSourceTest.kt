package com.foldright.examples.config.pipeline

import com.foldright.examples.config.ConfigSource
import com.foldright.examples.config.handler.MapConfigSourceHandler
import com.foldright.examples.config.handler.PlaceholderConfigSourceHandler
import com.foldright.examples.config.handler.SystemConfigSourceHandler
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

private const val NOT_EXISTED = "not_existed_key"

class ConfigSourceTest : AnnotationSpec() {
    private lateinit var pipeline: ConfigSource

    @AfterEach
    fun afterEach() {
        pipeline.get(NOT_EXISTED).shouldBeNull()
    }

    @Test
    fun testMap() {
        val placeholderConfigSource = PlaceholderConfigSourceHandler()
        val mapConfigSource = MapConfigSourceHandler(mapOf("a" to "1", "b" to "2"))

        pipeline = ConfigSourcePipeline()
            .addLast(placeholderConfigSource)
            .addLast(mapConfigSource)
            .addLast(SystemConfigSourceHandler.INSTANCE)

        pipeline.get("a") shouldBe "1"
    }

    @Test
    fun testFallback() {
        val placeholderConfigSource = PlaceholderConfigSourceHandler()
        val mapConfigSource = MapConfigSourceHandler(mapOf("a" to "1", "b" to "2"))

        pipeline = ConfigSourcePipeline()
            .addLast(placeholderConfigSource)
            .addLast(mapConfigSource)
            .addLast(SystemConfigSourceHandler.INSTANCE)

        pipeline.get("java.home") shouldBe System.getProperty("java.home")
    }

    @Test
    fun testPlaceholder() {
        val placeholderConfigSource = PlaceholderConfigSourceHandler()
        val mapConfigSource = MapConfigSourceHandler(mapOf("a" to "1", "b" to "2\${a}"))

        pipeline = ConfigSourcePipeline()
            .addLast(placeholderConfigSource)
            .addLast(mapConfigSource)
            .addLast(SystemConfigSourceHandler.INSTANCE)

        pipeline.get("b") shouldBe "21"
        pipeline.get("a") shouldBe "1"
    }

    @Test
    fun testPlaceholder_2_jumps() {
        val placeholderConfigSource = PlaceholderConfigSourceHandler()
        val mapConfigSource = MapConfigSourceHandler(mapOf("a" to "1", "b" to "2\${a}", "c" to "3\${b}"))

        pipeline = ConfigSourcePipeline()
            .addLast(placeholderConfigSource)
            .addLast(mapConfigSource)
            .addLast(SystemConfigSourceHandler.INSTANCE)

        pipeline.get("c") shouldBe "321"
    }

    @Test
    fun testOverride() {
        val placeholderConfigSource = PlaceholderConfigSourceHandler()
        val mapConfigSource = MapConfigSourceHandler(mapOf("a" to "1", "b" to "2", "java.home" to "3"))

        val pipeline = ConfigSourcePipeline()
            .addLast(placeholderConfigSource)
            .addLast(mapConfigSource)
            .addLast(SystemConfigSourceHandler.INSTANCE)

        val aValue = pipeline.get("java.home")
        aValue shouldBe "3"
    }
}
