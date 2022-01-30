package com.foldright.examples.duplex;

import com.foldright.auto.pipeline.PipelineDirection;

public interface RpcResponseOperation<REQ, RSP> {

    @PipelineDirection(direction = PipelineDirection.Direction.REVERSE)
    void onResponse(REQ request, RSP response);

}
