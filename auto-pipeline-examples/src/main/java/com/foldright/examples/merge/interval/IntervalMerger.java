package com.foldright.examples.merge.interval;

import com.foldright.auto.pipeline.AutoPipeline;
import com.foldright.examples.merge.Merger;

import java.util.List;

@AutoPipeline
public interface IntervalMerger<T extends Interval> extends Merger<T> {

    @Override
    T merge(List<T> intervals);
}
