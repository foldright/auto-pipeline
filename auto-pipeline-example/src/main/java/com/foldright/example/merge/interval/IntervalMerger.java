package com.foldright.example.merge.interval;

import com.foldright.auto.pipeline.AutoPipeline;
import com.foldright.example.merge.Merger;

import java.util.List;

@AutoPipeline
public interface IntervalMerger<T extends Interval> extends Merger<T> {

    @Override
    T merge(List<T> intervals);
}
