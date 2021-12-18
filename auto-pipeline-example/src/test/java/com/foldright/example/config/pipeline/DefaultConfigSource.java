package com.foldright.example.config.pipeline;

import com.foldright.example.config.ConfigSource;

public class DefaultConfigSource implements ConfigSource {

    private final ConfigSourcePipeline pipeline;

    public DefaultConfigSource(ConfigSourcePipeline pipeline) {
        this.pipeline = pipeline;
    }

    @Override
    public String get(String key) {
        return pipeline.get(key);
    }
}
