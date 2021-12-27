package com.foldright.example.merge;

import com.foldright.auto.pipeline.AutoPipeline;

import java.util.List;

@AutoPipeline
public interface Merger<T> {

    T merge(List<T> elements);
}
