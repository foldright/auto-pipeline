package com.foldright.examples.duplex;

import com.foldright.auto.pipeline.AutoPipeline;

@AutoPipeline
public interface RpcBiOperation<REQ, RSP> extends RpcRequestOperation<REQ, RSP>, RpcResponseOperation<REQ, RSP> {


}
