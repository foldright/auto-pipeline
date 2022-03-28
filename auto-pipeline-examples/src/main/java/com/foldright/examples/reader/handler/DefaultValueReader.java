package com.foldright.examples.reader.handler;

import com.foldright.examples.reader.pipeline.ReaderHandler;
import com.foldright.examples.reader.pipeline.ReaderHandlerContext;

public class DefaultValueReader implements ReaderHandler {

    private final String defaultValue;

    public DefaultValueReader(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public String read(ReaderHandlerContext readerHandlerContext) {
        return defaultValue;
    }
}
