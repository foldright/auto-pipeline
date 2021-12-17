package com.foldright.auto.pipeline;

public interface Handler<T,
        HANDLER extends Handler<T, HANDLER, CTX, PL>,
        CTX extends HandlerContext<T, HANDLER, CTX, PL>,
        PL extends Pipeline<T, HANDLER, CTX, PL>
        > {

    void handlerAdded(CTX ctx) throws Exception;

    void handlerRemoved(CTX ctx) throws Exception;

}
