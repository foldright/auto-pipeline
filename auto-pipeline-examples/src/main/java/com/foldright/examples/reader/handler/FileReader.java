package com.foldright.examples.reader.handler;

import com.foldright.examples.reader.pipeline.ReaderHandler;
import com.foldright.examples.reader.pipeline.ReaderHandlerContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileReader implements ReaderHandler {

    private final Path filePath;
    private final String charset;

    public FileReader(Path fileName, String charset) {
        this.filePath = fileName;
        this.charset = charset;
    }

    @Override
    public String read(ReaderHandlerContext readerHandlerContext) {
        boolean fileExists = Files.exists(filePath);

        if (!fileExists) {
            return readerHandlerContext.read();
        }

        try {
            byte[] bytes = Files.readAllBytes(filePath);
            return new String(bytes, charset);
        } catch (IOException e) {
            return readerHandlerContext.read();
        }
    }
}
