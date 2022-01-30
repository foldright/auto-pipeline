package com.foldright.examples.duplex.handler;

import com.foldright.examples.duplex.pipeline.RpcBiOperationHandler;
import com.foldright.examples.duplex.pipeline.RpcBiOperationHandlerContext;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class HealthCheckHandler implements RpcBiOperationHandler<Request, Response> {
    public static final String HEALTH_CHECK_URI = "/healthCheck.do";

    @Override
    public Future<Response> onRequest(Request request, RpcBiOperationHandlerContext<Request, Response> rpcBiOperationHandlerContext) {
        if (HEALTH_CHECK_URI.equalsIgnoreCase(request.getUri())) {
            return CompletableFuture.completedFuture(new Response("ok"));
        }

        return rpcBiOperationHandlerContext.onRequest(request);
    }

    @Override
    public void onResponse(Request request, Response response, RpcBiOperationHandlerContext<Request, Response> rpcBiOperationHandlerContext) {
        if ("ok".equals(response.getBody())) {
            response.getHeaders().put(HealthCheckHandler.class.getSimpleName(), "true");
        }
        rpcBiOperationHandlerContext.onResponse(request, response);
    }
}
