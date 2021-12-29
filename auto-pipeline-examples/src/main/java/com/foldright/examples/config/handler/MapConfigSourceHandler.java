package com.foldright.examples.config.handler;

import com.foldright.examples.config.pipeline.ConfigSourceHandler;
import com.foldright.examples.config.pipeline.ConfigSourceHandlerContext;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class MapConfigSourceHandler implements ConfigSourceHandler {

    private final Map<String, String> map;

    public MapConfigSourceHandler(Map<String, String> map) {
        this.map = map;
    }


    @Override
    public String get(String key, ConfigSourceHandlerContext context) {
        String value = map.get(key);

        if (StringUtils.isNotBlank(value)) {
            return value;
        }

        return context.get(key);
    }
}
