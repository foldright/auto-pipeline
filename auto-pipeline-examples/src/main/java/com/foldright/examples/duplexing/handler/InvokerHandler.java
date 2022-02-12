package com.foldright.examples.duplexing.handler;

import com.foldright.examples.duplexing.RPC.Request;
import com.foldright.examples.duplexing.RPC.Response;
import com.foldright.examples.duplexing.pipeline.RPCHandler;
import com.foldright.examples.duplexing.pipeline.RPCHandlerContext;

public class InvokerHandler implements RPCHandler {

    @Override
    public Response request(Request request, RPCHandlerContext rPCHandlerContext) {
        Response response = new Response();
        onResponse(response, rPCHandlerContext);
        return response;
    }

    @Override
    public void onResponse(Response response, RPCHandlerContext rPCHandlerContext) {
        rPCHandlerContext.onResponse(response);
    }
}
