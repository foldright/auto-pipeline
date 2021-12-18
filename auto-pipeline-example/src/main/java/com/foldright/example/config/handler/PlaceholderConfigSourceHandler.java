package com.foldright.example.config.handler;

import com.foldright.example.config.pipeline.ConfigSourceHandler;
import com.foldright.example.config.pipeline.ConfigSourceHandlerContext;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.removeEnd;
import static org.apache.commons.lang3.StringUtils.removeStart;

/**
 * 支持 value 为 ${value} 形式的 placeholder，暂时不支持如 ${${value}} 的嵌套
 */
public class PlaceholderConfigSourceHandler implements ConfigSourceHandler {

    private static final Pattern PATTERN = Pattern.compile("(\\$\\{.+?})");

    @Override
    public String get(String key, ConfigSourceHandlerContext context) {
        String value = context.get(key);
        if (StringUtils.isBlank(value)) {
            return null;
        }

        Matcher matcher = PATTERN.matcher(value);

        if (!matcher.find()) {
            return value;
        }

        int groupCount = matcher.groupCount();
        for (int i = 0; i < groupCount; i++) {

            // like ${xxx}
            String matched = matcher.group(i + 1);
            // like xxx
            String placeholderKey = removeEnd(removeStart(matched, "${"), "}");
            // get xxx's value and replace ${xxx} with placeholderValue
            String placeholderValue = context.pipeline().get(placeholderKey);
            value = StringUtils.replace(value, matched, placeholderValue);
        }

        if (StringUtils.isBlank(value)) {
            return null;
        }

        return value;
    }
}
