package com.foldright.examples.config;

import com.foldright.auto.pipeline.AutoPipeline;

@AutoPipeline
public interface ConfigSource {
    String get(String key);
}
