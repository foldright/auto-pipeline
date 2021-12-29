package com.foldright.examples.grpc.handler;

import com.foldright.examples.grpc.CallOptions;
import com.foldright.examples.grpc.ClientCall;
import com.foldright.examples.grpc.MethodDescriptor;
import com.foldright.examples.grpc.pipeline.ChannelHandler;
import com.foldright.examples.grpc.pipeline.ChannelHandlerContext;

public class DefaultChannelHandler implements ChannelHandler {

    @Override
    public <RequestT, ResponseT> ClientCall<RequestT, ResponseT> newCall(
            MethodDescriptor<RequestT, ResponseT> methodDescriptor,
            CallOptions callOptions,
            ChannelHandlerContext channelHandlerContext) {

        // return a default clientCall
        return new ClientCall<>(callOptions);
    }
}
