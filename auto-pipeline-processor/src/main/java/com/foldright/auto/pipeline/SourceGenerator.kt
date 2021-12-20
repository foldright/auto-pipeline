package com.foldright.auto.pipeline

import javax.annotation.processing.Filer

class SourceGenerator(private val desc: AutoPipelineClassDescriptor, private val filer: Filer) {

    fun genPipeline() {
        PipelineGenerator(desc, filer).gen()
    }

    fun genHandlerContextInterface() {
        HandlerContextGenerator(desc, filer).gen()
    }

    fun genAbstractHandlerContextClass() {
        AbstractHandlerContextGenerator(desc, filer).gen()
    }

    fun genDefaultHandlerContextClass() {
        DefaultHandlerContextGenerator(desc, filer).gen()
    }


    fun genHandlerInterface() {
        HandlerGenerator(desc, filer).gen()
    }
}