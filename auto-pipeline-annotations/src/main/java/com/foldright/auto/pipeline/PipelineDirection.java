package com.foldright.auto.pipeline;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.CLASS)
public @interface PipelineDirection {

    Direction value();

    enum Direction {
        FORWARD,
        REVERSE,
    }

}
