package com.foldright.example.config;

public class DefaultConfigSourceHandlerContext extends AbstractConfigSourceHandlerContext {

    private final ConfigSourceHandler handler;

    public DefaultConfigSourceHandlerContext(ConfigSourcePipeline pipeline, ConfigSourceHandler handler) {
        super(pipeline);
        this.handler = handler;
    }


    @Override
    protected ConfigSourceHandler handler() {
        return handler;
    }
}
