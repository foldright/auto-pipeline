package com.foldright.example.config;

import com.foldright.auto.pipeline.AutoPipeline;

@AutoPipeline
public interface ConfigSource {
    String get(String key);
}
