package com.foldright.auto.pipeline;

// inspired by netty
public interface Pipeline<
        T,
        HANDLER extends Handler<T, HANDLER, CTX, PL>,
        CTX extends HandlerContext<T, HANDLER, CTX, PL>,
        PL extends Pipeline<T, HANDLER, CTX, PL>
        > {

    T service();

    /**
     * Inserts a {@link Handler} at the first position of this pipeline.
     *
     * @param name    the name of the handler to insert first
     * @param handler the handler to insert first
     * @throws IllegalArgumentException if there's an entry with the same name already in the pipeline
     * @throws NullPointerException     if the specified handler is {@code null}
     */
    PL addFirst(String name, HANDLER handler);

    /**
     * Appends a {@link Handler} at the last position of this pipeline.
     *
     * @param name    the name of the handler to append
     * @param handler the handler to append
     * @throws IllegalArgumentException if there's an entry with the same name already in the pipeline
     * @throws NullPointerException     if the specified handler is {@code null}
     */
    PL addLast(String name, HANDLER handler);
}
