package com.foldright.auto.pipeline;

public interface HandlerContext<T,
        HANDLER extends Handler<T, HANDLER, CTX, PL>,
        CTX extends HandlerContext<T, HANDLER, CTX, PL>,
        PL extends Pipeline<T, HANDLER, CTX, PL>
        > {

    T service();

    PL pipeline();

    HANDLER handler();

    String name();
}
