package com.foldright.example.config.pipeline

import com.foldright.example.config.DefaultConfigSourcePipeline
import com.foldright.example.config.handler.MapConfigSourceHandler
import com.foldright.example.config.handler.PlaceholderConfigSourceHandler
import com.foldright.example.config.handler.SystemConfigSourceHandler
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe

class ConfigSourceTest : AnnotationSpec() {


    @Test
    fun testMap() {
        val placeholderConfigSource = PlaceholderConfigSourceHandler()
        val mapConfigSource = MapConfigSourceHandler(mapOf("a" to "1", "b" to "2"))
        val propConfigSource = SystemConfigSourceHandler()

        val pipeline = DefaultConfigSourcePipeline()
            .addLast(placeholderConfigSource)
            .addLast(mapConfigSource)
            .addLast(propConfigSource)

        val aValue = pipeline.get("a")
        aValue shouldBe "1"
    }

    @Test
    fun testFallback() {
        val placeholderConfigSource = PlaceholderConfigSourceHandler()
        val mapConfigSource = MapConfigSourceHandler(mapOf("a" to "1", "b" to "2"))
        val propConfigSource = SystemConfigSourceHandler()

        val pipeline = DefaultConfigSourcePipeline()
            .addLast(placeholderConfigSource)
            .addLast(mapConfigSource)
            .addLast(propConfigSource)

        val aValue = pipeline.get("java.home")
        aValue shouldBe System.getProperty("java.home")
    }

    @Test
    fun testPlaceholder() {
        val placeholderConfigSource = PlaceholderConfigSourceHandler()
        val mapConfigSource = MapConfigSourceHandler(mapOf("a" to "1", "b" to "2\${a}"))
        val propConfigSource = SystemConfigSourceHandler()

        val pipeline = DefaultConfigSourcePipeline()
            .addLast(placeholderConfigSource)
            .addLast(mapConfigSource)
            .addLast(propConfigSource)

        val aValue = pipeline.get("b")
        aValue shouldBe "21"
    }

    @Test
    fun testPlaceholder_2() {
        val placeholderConfigSource = PlaceholderConfigSourceHandler()
        val mapConfigSource = MapConfigSourceHandler(mapOf("a" to "1", "b" to "2\${a}", "c" to "3\${b}"))
        val propConfigSource = SystemConfigSourceHandler()

        val pipeline = DefaultConfigSourcePipeline()
            .addLast(placeholderConfigSource)
            .addLast(mapConfigSource)
            .addLast(propConfigSource)

        val aValue = pipeline.get("c")
        aValue shouldBe "321"
    }

    @Test
    fun testOverride() {
        val placeholderConfigSource = PlaceholderConfigSourceHandler()
        val mapConfigSource = MapConfigSourceHandler(mapOf("a" to "1", "b" to "2", "java.home" to "3"))
        val propConfigSource = SystemConfigSourceHandler()

        val pipeline = DefaultConfigSourcePipeline()
            .addLast(placeholderConfigSource)
            .addLast(mapConfigSource)
            .addLast(propConfigSource)

        val aValue = pipeline.get("java.home")
        aValue shouldBe "3"
    }
}