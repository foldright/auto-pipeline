package com.foldright.example.lb;

import com.foldright.auto.pipeline.AutoPipeline;

import java.util.List;

@AutoPipeline
public interface LoadBalancer {

    <T extends Node> T choose(List<T> nodes);
}
