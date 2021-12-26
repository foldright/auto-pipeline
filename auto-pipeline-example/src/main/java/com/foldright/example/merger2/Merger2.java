package com.foldright.example.merger2;

import com.foldright.auto.pipeline.AutoPipeline;

import java.util.List;

@AutoPipeline
public interface Merger2<T extends Interval> {

    T merge(List<T> intervals);
}
