package com.foldright.examples.grpc.pipeline

import com.foldright.examples.grpc.CallOptions
import com.foldright.examples.grpc.ClientCall
import com.foldright.examples.grpc.MethodDescriptor
import com.foldright.examples.grpc.handler.DefaultChannelHandler
import com.foldright.examples.grpc.handler.TransformClientCallChannelHandler
import com.foldright.examples.grpc.handler.WrapCallOptionsChannelHandler
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe

class ChannelTest : AnnotationSpec() {

    lateinit var pipeline: ChannelPipeline

    @Test
    fun testNoneHandler() {
        pipeline = ChannelPipeline()

        val newCall = pipeline.newCall(
            MethodDescriptor<String, String>("fullMethodName", "serviceName"),
            CallOptions()
        )

        newCall shouldBe null
    }

    @Test
    fun testDefaultHandler() {
        pipeline = ChannelPipeline()
            .addLast(DefaultChannelHandler())

        val newCall = pipeline.newCall(
            MethodDescriptor<String, String>("fullMethodName", "serviceName"),
            CallOptions()
        )

        newCall.javaClass shouldBe ClientCall::class.java
    }

    @Test
    fun testWrapperCallOptionsHandler() {
        pipeline = ChannelPipeline()
            .addLast(WrapCallOptionsChannelHandler())
            .addLast(DefaultChannelHandler())


        val newCall = pipeline.newCall(
            MethodDescriptor<String, String>("fullMethodName", "serviceName"),
            CallOptions()
        )

        newCall.callOptions.javaClass shouldBe WrapCallOptionsChannelHandler.CallOptionsWrapper::class.java
    }

    @Test
    fun testTransformClientCallHandler() {
        pipeline = ChannelPipeline()
            .addLast(WrapCallOptionsChannelHandler())
            .addLast(TransformClientCallChannelHandler())
            .addLast(DefaultChannelHandler())


        val newCall = pipeline.newCall(
            MethodDescriptor<String, String>("fullMethodName", "serviceName"),
            CallOptions()
        )

        newCall.javaClass shouldBe TransformClientCallChannelHandler.ClientCallWrapper::class.java
    }
}
