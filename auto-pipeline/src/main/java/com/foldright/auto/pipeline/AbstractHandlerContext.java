package com.foldright.auto.pipeline;

public abstract class AbstractHandlerContext<T,
        HANDLER extends Handler<T, HANDLER, CTX, PL>,
        CTX extends AbstractHandlerContext<T, HANDLER, CTX, PL>,
        PL extends AbstractPipeline<T, HANDLER, CTX, PL>> implements HandlerContext<T, HANDLER, CTX, PL> {

    private final PL pipeline;
    private final String name;
    private final HANDLER handler;

    volatile CTX next;
    volatile CTX prev;


    public AbstractHandlerContext(PL pipeline, String name, HANDLER handler) {
        this.pipeline = pipeline;
        this.name = name;
        this.handler = handler;
    }

    @Override
    public PL pipeline() {
        return this.pipeline;
    }

    @Override
    public T service() {
        return pipeline().service();
    }

    @Override
    public HANDLER handler() {
        return this.handler;
    }

    @Override
    public String name() {
        return this.name;
    }
}
