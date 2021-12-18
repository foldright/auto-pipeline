package com.foldright.example.config;

public abstract class AbstractConfigSourceHandlerContext implements ConfigSourceHandlerContext {
    private final ConfigSourcePipeline pipeline;

    volatile AbstractConfigSourceHandlerContext prev;
    volatile AbstractConfigSourceHandlerContext next;

    public AbstractConfigSourceHandlerContext(ConfigSourcePipeline pipeline) {
        this.pipeline = pipeline;
    }

    @Override
    public String get(String key) {
        return handler().get(key, next);
    }

    protected abstract ConfigSourceHandler handler();

    @Override
    public ConfigSourcePipeline pipeline() {
        return pipeline;
    }
}
