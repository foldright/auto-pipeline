package com.foldright.examples.grpc;

public class MethodDescriptor<ReqT, RespT> {

    private final String fullMethodName;
    private final String serviceName;

    public MethodDescriptor(String fullMethodName, String serviceName) {
        this.fullMethodName = fullMethodName;
        this.serviceName = serviceName;
    }

    public String getFullMethodName() {
        return fullMethodName;
    }

    public String getServiceName() {
        return serviceName;
    }
}
