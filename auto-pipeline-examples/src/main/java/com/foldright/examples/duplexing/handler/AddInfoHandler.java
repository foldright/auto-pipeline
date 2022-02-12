package com.foldright.examples.duplexing.handler;

import com.foldright.examples.duplexing.RPC.Request;
import com.foldright.examples.duplexing.RPC.Response;
import com.foldright.examples.duplexing.pipeline.RPCHandler;
import com.foldright.examples.duplexing.pipeline.RPCHandlerContext;

public class AddInfoHandler implements RPCHandler {

    private final String info;

    public AddInfoHandler(String info) {
        this.info = info;
    }

    @Override
    public Response request(Request request, RPCHandlerContext rPCHandlerContext) {
        request.append(info);
        return rPCHandlerContext.request(request);
    }

    @Override
    public void onResponse(Response response, RPCHandlerContext rPCHandlerContext) {
        response.append(info);
        rPCHandlerContext.onResponse(response);
    }
}
