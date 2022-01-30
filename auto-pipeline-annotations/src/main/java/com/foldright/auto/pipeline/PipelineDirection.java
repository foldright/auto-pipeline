package com.foldright.auto.pipeline;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotation to be applied to an abstract method for indicating the pipeline's direction
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.CLASS)
public @interface PipelineDirection {

    Direction direction() default Direction.FORWARD;

    enum Direction {

        /**
         * indicate the pipeline should be invoked from head to tail
         */
        FORWARD,

        /**
         * indicate the pipeline should be invoked from tail to head
         */
        REVERSE
    }

}
