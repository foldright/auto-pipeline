package com.foldright.examples.duplexing.pipeline

import com.foldright.examples.duplexing.RPC
import com.foldright.examples.duplexing.RPC.Request
import com.foldright.examples.duplexing.handler.AddInfoHandler
import com.foldright.examples.duplexing.handler.InvokerHandler
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe

class RPCTest : AnnotationSpec() {
    lateinit var pipeline: RPC


    @Test
    fun testDuplexingPipeline() {
        val handler1 = AddInfoHandler("1")
        val handler2 = AddInfoHandler("2")
        val handler3 = AddInfoHandler("3")
        val invokerHandler = InvokerHandler()

        pipeline = RPCPipeline()
            .addLast(handler1)
            .addLast(handler2)
            .addLast(handler3)
            .addLast(invokerHandler)


        val request = Request()
        val response = pipeline.request(request)

        request.infos() shouldBe listOf("1", "2", "3")
        response.infos() shouldBe listOf("3", "2", "1")
    }
}
