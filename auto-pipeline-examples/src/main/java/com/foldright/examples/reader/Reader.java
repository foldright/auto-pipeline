package com.foldright.examples.reader;

import com.foldright.auto.pipeline.AutoPipeline;

@AutoPipeline
public interface Reader {

    String read();
}
