package com.foldright.auto.pipeline;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * specify the direction of the pipeline, which can be used on methods or interfaces(which must be annotated with {@link AutoPipeline}).
 * the default direction is {@link Direction#FORWARD}, here's the rules:
 * <ul>
 * <li>
 *     rule1: in the <code>@AutoPipeline</code> interface's method, we can:
 *     <ul>
 *      <li>add {@link Direction#FORWARD} to apply the interface's method, the the annotated method will be called from <strong>head to tail</strong></li>
 *      <li>add {@link Direction#REVERSE} to apply the interface's method, the the annotated method will be called from <strong>tail to head</strong></li>
 *      <li>if there's no {@link Direction} annotation to the method, the direction of pipeline will follow the {@link Direction} in the in the <code>@AutoPipeline</code> interface, see rule2</li>
 *      <li>{@link Direction#FORWARD} and {@link Direction#REVERSE} are mutually exclusive, they cannot coexist</li>
 *     </ul>
 * </li>
 * <li>
 *     rule2: in the <code>@AutoPipeline</code> interface, we can:
 *     <ul>
 *         <li>add {@link Direction#FORWARD} to the <code>@AutoPipeline</code> interface, the methods declared in this interface will be called from <strong>head to tail</strong> unless the method is annotated with {@link Direction} in the method level</li>
 *         <li>add {@link Direction#REVERSE} to the <code>@AutoPipeline</code> interface, the methods declared in this interface will be called from <strong>tail to head</strong> unless the method is annotated with {@link Direction} in the method level</li>
 *         <li>if no {@link Direction} annotation to the <code>@AutoPipeline</code> interface, it equals to add {@link Direction#FORWARD} to the <code>@AutoPipeline</code> interface</li>
 *         <li>{@link Direction#FORWARD} and {@link Direction#REVERSE} are mutually exclusive, they cannot coexist</li>
 *     </ul>
 *
 * </li>
 * </ul>
 *
 * @see AutoPipeline
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.CLASS)
public @interface PipelineDirection {

    /**
     * the direction of the pipeline, check the {@link PipelineDirection} to find the rules
     *
     * @see PipelineDirection
     */
    Direction value();

    enum Direction {

        /**
         * from head to tail
         */
        FORWARD,

        /**
         * from tail to head
         */
        REVERSE,
    }

}
