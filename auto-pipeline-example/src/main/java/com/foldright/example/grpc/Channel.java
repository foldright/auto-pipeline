package com.foldright.example.grpc;

import com.foldright.auto.pipeline.AutoPipeline;

@AutoPipeline
public interface Channel {

    <RequestT, ResponseT> ClientCall<RequestT, ResponseT> newCall(
            MethodDescriptor<RequestT, ResponseT> methodDescriptor, CallOptions callOptions
    );

}
