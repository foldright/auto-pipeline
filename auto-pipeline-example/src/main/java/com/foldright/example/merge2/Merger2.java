package com.foldright.example.merge2;

import com.foldright.auto.pipeline.AutoPipeline;

import java.util.List;

@AutoPipeline
public interface Merger2<T extends Interval> {

    T merge(List<T> intervals);
}
