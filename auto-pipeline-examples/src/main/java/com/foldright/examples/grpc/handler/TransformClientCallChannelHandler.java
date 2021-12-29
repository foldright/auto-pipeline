package com.foldright.examples.grpc.handler;

import com.foldright.examples.grpc.CallOptions;
import com.foldright.examples.grpc.ClientCall;
import com.foldright.examples.grpc.MethodDescriptor;
import com.foldright.examples.grpc.pipeline.ChannelHandler;
import com.foldright.examples.grpc.pipeline.ChannelHandlerContext;

public class TransformClientCallChannelHandler implements ChannelHandler {
    @Override
    public <RequestT, ResponseT> ClientCall<RequestT, ResponseT> newCall(
            MethodDescriptor<RequestT, ResponseT> methodDescriptor,
            CallOptions callOptions,
            ChannelHandlerContext channelHandlerContext) {

        ClientCall<RequestT, ResponseT> clientCall = channelHandlerContext.newCall(methodDescriptor, callOptions);
        return new ClientCallWrapper<>(clientCall);
    }

    public static class ClientCallWrapper<Req, Res> extends ClientCall<Req, Res> {
        private final ClientCall<Req, Res> clientCall;

        public ClientCallWrapper(ClientCall<Req, Res> clientCall) {
            super(clientCall.getCallOptions());
            this.clientCall = clientCall;
        }
    }

}
