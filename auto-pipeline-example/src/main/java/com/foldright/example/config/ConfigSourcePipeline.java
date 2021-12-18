package com.foldright.example.config;

import com.google.common.collect.Lists;

import java.util.List;

public class ConfigSourcePipeline implements ConfigSource {

    private final AbstractConfigSourceHandlerContext head;
    private final AbstractConfigSourceHandlerContext tail;

    public ConfigSourcePipeline() {
        head = new HeadContext(this);
        tail = new TailContext(this);

        head.prev = null;
        head.next = tail;
        tail.prev = head;
        tail.next = null;
    }

    @Override
    public String get(String key) {
        return head.get(key);
    }


    public synchronized ConfigSourcePipeline addFirst(ConfigSourceHandler handler) {
        DefaultConfigSourceHandlerContext newCtx = new DefaultConfigSourceHandlerContext(this, handler);
        AbstractConfigSourceHandlerContext nextCtx = head.next;
        head.next = newCtx;
        newCtx.prev = head;
        newCtx.next = nextCtx;
        nextCtx.prev = newCtx;

        return this;
    }

    public synchronized ConfigSourcePipeline addFirst(List<ConfigSourceHandler> handlers) {
        for (ConfigSourceHandler handler : Lists.reverse(handlers)) {
            addFirst(handler);
        }

        return this;
    }

    public synchronized ConfigSourcePipeline addLast(ConfigSourceHandler handler) {
        DefaultConfigSourceHandlerContext newCtx = new DefaultConfigSourceHandlerContext(this, handler);
        AbstractConfigSourceHandlerContext prevCtx = tail.prev;

        newCtx.prev = prevCtx;
        newCtx.next = tail;
        prevCtx.next = newCtx;
        tail.prev = newCtx;

        return this;
    }

    public synchronized ConfigSourcePipeline addLast(List<ConfigSourceHandler> handlers) {
        for (ConfigSourceHandler handler : handlers) {
            addLast(handler);
        }
        return this;
    }

    private static class HeadContext extends AbstractConfigSourceHandlerContext {

        private HeadContext(ConfigSourcePipeline pipeline) {
            super(pipeline);
        }

        @Override
        public String get(String key) {
            return next.get(key);
        }

        @Override
        protected ConfigSourceHandler handler() {
            return null;
        }
    }

    private static class TailContext extends AbstractConfigSourceHandlerContext {

        private TailContext(ConfigSourcePipeline pipeline) {
            super(pipeline);
        }

        @Override
        public String get(String key) {
            return null;
        }


        @Override
        protected ConfigSourceHandler handler() {
            return null;
        }
    }
}
