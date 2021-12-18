package com.foldright.example.config.handler;

import com.foldright.example.config.pipeline.ConfigSourceHandler;
import com.foldright.example.config.pipeline.ConfigSourceHandlerContext;
import org.apache.commons.lang3.StringUtils;

public class SystemConfigSourceHandler implements ConfigSourceHandler {

    @Override
    public String get(String key, ConfigSourceHandlerContext context) {
        String value = System.getProperty(key);
        if (StringUtils.isNotBlank(value)) {
            return value;
        }
        return context.get(key);
    }
}
