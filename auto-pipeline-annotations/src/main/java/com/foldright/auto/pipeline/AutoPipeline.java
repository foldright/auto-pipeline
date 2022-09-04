package com.foldright.auto.pipeline;

import com.foldright.auto.pipeline.PipelineDirection.Direction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotation to be applied to an interface for which the pipeline pattern should be automatically generated.
 * the default of {@link PipelineDirection} is {@link Direction#FORWARD},
 *
 * @see PipelineDirection
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface AutoPipeline {
}
