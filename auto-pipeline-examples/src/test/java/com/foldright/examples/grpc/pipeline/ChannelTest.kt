package com.foldright.examples.grpc.pipeline

import com.foldright.examples.grpc.CallOptions
import com.foldright.examples.grpc.ClientCall
import com.foldright.examples.grpc.MethodDescriptor
import com.foldright.examples.grpc.handler.DefaultChannelHandler
import com.foldright.examples.grpc.handler.TransformClientCallChannelHandler
import com.foldright.examples.grpc.handler.TransformClientCallChannelHandler.ClientCallWrapper
import com.foldright.examples.grpc.handler.WrapCallOptionsChannelHandler
import com.foldright.examples.grpc.handler.WrapCallOptionsChannelHandler.CallOptionsWrapper
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.types.shouldBeTypeOf

class ChannelTest : AnnotationSpec() {

    private lateinit var pipeline: ChannelPipeline

    @Test
    fun testNoneHandler() {
        pipeline = ChannelPipeline()

        val newCall = pipeline.newCall(
            MethodDescriptor<String, String>("fullMethodName", "serviceName"),
            CallOptions()
        )

        newCall.shouldBeNull()
    }

    @Test
    fun testDefaultHandler() {
        pipeline = ChannelPipeline()
            .addLast(DefaultChannelHandler())

        val newCall = pipeline.newCall(
            MethodDescriptor<String, String>("fullMethodName", "serviceName"),
            CallOptions()
        )

        newCall.shouldBeTypeOf<ClientCall<*, *>>()
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

        newCall.callOptions.shouldBeTypeOf<CallOptionsWrapper>()
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

        newCall.shouldBeTypeOf<ClientCallWrapper<*, *>>()
    }
}
