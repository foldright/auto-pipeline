package com.foldright.examples.duplex;

import java.util.concurrent.Future;

public interface RpcRequestOperation<REQ,RSP> {

    Future<RSP> onRequest(REQ request);
}
