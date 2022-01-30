package com.foldright.examples.duplex.handler;

import com.foldright.examples.duplex.pipeline.RpcBiOperationHandler;
import com.foldright.examples.duplex.pipeline.RpcBiOperationHandlerContext;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class RequestAcceptHandler implements RpcBiOperationHandler<Request, Response> {

    @Override
    public Future<Response> onRequest(Request request, RpcBiOperationHandlerContext<Request, Response> rpcBiOperationHandlerContext) {
        if (request.getUri().startsWith("/gw/")) {
            return CompletableFuture.completedFuture(new Response("accepted"));
        } else {
            return CompletableFuture.completedFuture(new Response("unsupported request"));
        }
    }

    @Override
    public void onResponse(Request request, Response response, RpcBiOperationHandlerContext<Request, Response> rpcBiOperationHandlerContext) {
        response.getHeaders().put(RequestAcceptHandler.class.getSimpleName(), "accepted".equals(response.getBody()) + "");
        rpcBiOperationHandlerContext.onResponse(request, response);
    }
}
