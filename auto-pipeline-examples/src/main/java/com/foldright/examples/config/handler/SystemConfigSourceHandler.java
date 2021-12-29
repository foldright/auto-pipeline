package com.foldright.examples.config.handler;

import com.foldright.examples.config.pipeline.ConfigSourceHandler;
import com.foldright.examples.config.pipeline.ConfigSourceHandlerContext;
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
