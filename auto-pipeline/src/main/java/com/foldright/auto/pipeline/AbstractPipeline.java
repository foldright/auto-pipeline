package com.foldright.auto.pipeline;

public abstract class AbstractPipeline<
        T,
        HANDLER extends Handler<T, HANDLER, CTX, PL>,
        CTX extends AbstractHandlerContext<T, HANDLER, CTX, PL>,
        PL extends AbstractPipeline<T, HANDLER, CTX, PL>
        > implements Pipeline<T, HANDLER, CTX, PL> {

    private final T service;
    protected CTX head;
    protected CTX tail;

    protected AbstractPipeline(T service) {
        this.service = service;
        head = newContext("head-context", null);
        tail = newContext("tail-handler", null);

        head.prev = null;
        head.next = tail;

        tail.prev = head;
        tail.next = null;
    }

    protected CTX headNext() {
        return head.next;
    }

    @Override
    public T service() {
        return service;
    }

    @Override
    public PL addFirst(String name, HANDLER handler) {
        CTX newCtx = newContext(name, handler);
        addFirst0(newCtx);
        return (PL) this;
    }

    protected abstract CTX newContext(String name, HANDLER handler);

    private synchronized void addFirst0(CTX newCtx) {
        CTX nextCtx = head.next;
        newCtx.prev = head;
        newCtx.next = nextCtx;
        head.next = newCtx;
        nextCtx.prev = newCtx;
    }

    @Override
    public PL addLast(String name, HANDLER handler) {
        CTX newCtx = newContext(name, handler);
        addLast0(newCtx);
        return (PL) this;
    }

    private synchronized void addLast0(CTX newCtx) {
        CTX prev = tail.prev;
        newCtx.prev = prev;
        newCtx.next = tail;
        prev.next = newCtx;
        tail.prev = newCtx;
    }
}
