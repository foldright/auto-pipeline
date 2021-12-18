package com.foldright.example.config;

public interface ConfigSourceHandler {

    String get(String key, ConfigSourceHandlerContext context);

}
