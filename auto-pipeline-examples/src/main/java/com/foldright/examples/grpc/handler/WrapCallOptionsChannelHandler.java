package com.foldright.examples.grpc.handler;

import com.foldright.examples.grpc.CallOptions;
import com.foldright.examples.grpc.ClientCall;
import com.foldright.examples.grpc.MethodDescriptor;
import com.foldright.examples.grpc.pipeline.ChannelHandler;
import com.foldright.examples.grpc.pipeline.ChannelHandlerContext;

public class WrapCallOptionsChannelHandler implements ChannelHandler {

    @Override
    public <RequestT, ResponseT> ClientCall<RequestT, ResponseT> newCall(
            MethodDescriptor<RequestT, ResponseT> methodDescriptor,
            CallOptions callOptions,
            ChannelHandlerContext channelHandlerContext) {

        CallOptionsWrapper callOptionsWrapper = new CallOptionsWrapper(callOptions);

        return channelHandlerContext.newCall(methodDescriptor, callOptionsWrapper);
    }


    public static class CallOptionsWrapper extends CallOptions {
        private final CallOptions callOptions;

        public CallOptionsWrapper(CallOptions callOptions) {
            this.callOptions = callOptions;
        }
    }
}
