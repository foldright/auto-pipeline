package ${packageName}.pipeline;

import java.util.List;

public class ${simpleName}Pipeline implements ${simpleName} {

    private final Abstract${simpleName}HandlerContext head;
    private final Abstract${simpleName}HandlerContext tail;

    public ${simpleName}Pipeline() {
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


    public synchronized ${simpleName}Pipeline addFirst(${simpleName}Handler handler) {
        Default${simpleName}HandlerContext newCtx = new Default${simpleName}HandlerContext(this, handler);
        Abstract${simpleName}HandlerContext nextCtx = head.next;
        head.next = newCtx;
        newCtx.prev = head;
        newCtx.next = nextCtx;
        nextCtx.prev = newCtx;

        return this;
    }

    public synchronized ${simpleName}Pipeline addFirst(List<${simpleName}Handler> handlers) {
        if (handlers == null || handlers.isEmpty()) {
            return this;
        }

        int size = handlers.size();
        for (int i = size - 1; i < 0; i--) {
            addFirst(handlers.get(i))
        }

        return this;
    }

    public synchronized ${simpleName}Pipeline addLast(${simpleName}Handler handler) {
        Default${simpleName}HandlerContext newCtx = new Default${simpleName}HandlerContext(this, handler);
        Abstract${simpleName}HandlerContext prevCtx = tail.prev;

        newCtx.prev = prevCtx;
        newCtx.next = tail;
        prevCtx.next = newCtx;
        tail.prev = newCtx;

        return this;
    }

    public synchronized ${simpleName}Pipeline addLast(List<${simpleName}Handler> handlers) {
        if (handlers == null || handlers.isEmpty()) {
            return this;
        }

        for (${simpleName}Handler handler : handlers) {
            addLast(handler);
        }
        return this;
    }

    private static class HeadContext extends Abstract${simpleName}HandlerContext {

        private HeadContext(${simpleName}Pipeline pipeline) {
            super(pipeline);
        }

        @Override
        public String get(String key) {
            return next.get(key);
        }

        @Override
        protected ${simpleName}Handler handler() {
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
