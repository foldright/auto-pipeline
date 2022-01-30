package com.foldright.examples.duplex

import com.foldright.examples.duplex.handler.HealthCheckHandler
import com.foldright.examples.duplex.handler.Request
import com.foldright.examples.duplex.handler.RequestAcceptHandler
import com.foldright.examples.duplex.handler.Response
import com.foldright.examples.duplex.pipeline.RpcBiOperationPipeline
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe

class RpcBiOperationTest : AnnotationSpec() {

    lateinit var pipeline: RpcBiOperationPipeline<Request, Response>

    @BeforeAll
    fun setup() {
        pipeline = RpcBiOperationPipeline<Request, Response>()
            .addLast(HealthCheckHandler())
            .addLast(RequestAcceptHandler())
    }

    @Test
    fun testHealthCheck() {
        val request = Request(HealthCheckHandler.HEALTH_CHECK_URI)
        val onRequestFuture = pipeline.onRequest(request)
        val response = onRequestFuture.get()
        response.body shouldBe "ok"
        response.headers[HealthCheckHandler::class.java.simpleName] shouldBe null
        response.headers[RequestAcceptHandler::class.java.simpleName] shouldBe null

        pipeline.onResponse(request, response)
        response.body shouldBe "ok"
        response.headers[HealthCheckHandler::class.java.simpleName] shouldBe "true"
        response.headers[RequestAcceptHandler::class.java.simpleName] shouldBe null
    }

    @Test
    fun testRequestAccept() {
        val request = Request("/gw/abc.do")
        val onRequestFuture = pipeline.onRequest(request)
        val response = onRequestFuture.get()
        response.body shouldBe "accepted"
        response.headers[HealthCheckHandler::class.java.simpleName] shouldBe null
        response.headers[RequestAcceptHandler::class.java.simpleName] shouldBe null

        pipeline.onResponse(request, response)
        response.body shouldBe "accepted"
        response.headers[HealthCheckHandler::class.java.simpleName] shouldBe null
        response.headers[RequestAcceptHandler::class.java.simpleName] shouldBe "true"
    }

    @Test
    fun testRequestReject() {
        val request = Request("/h5/abc.do")
        val onRequestFuture = pipeline.onRequest(request)
        val response = onRequestFuture.get()
        response.body shouldBe "unsupported request"
        response.headers[HealthCheckHandler::class.java.simpleName] shouldBe null
        response.headers[RequestAcceptHandler::class.java.simpleName] shouldBe null

        pipeline.onResponse(request, response)
        response.body shouldBe "unsupported request"
        response.headers[HealthCheckHandler::class.java.simpleName] shouldBe null
        response.headers[RequestAcceptHandler::class.java.simpleName] shouldBe "false"
    }
}
