package com.foldright.auto.pipeline.processor

import com.foldright.auto.pipeline.processor.generator.*
import javax.annotation.processing.Filer

class SourceGeneratorFacade(private val desc: AutoPipelineClassDescriptor, private val filer: Filer) {

    fun genSourceCode() {
        genPipeline()
        genHandlerContextInterface()
        genAbstractHandlerContextClass()
        genDefaultHandlerContextClass()
        genHandlerInterface()
    }

    private fun genPipeline() {
        PipelineGenerator(desc, filer).gen()
    }

    private fun genHandlerContextInterface() {
        HandlerContextGenerator(desc, filer).gen()
    }

    private fun genAbstractHandlerContextClass() {
        AbstractHandlerContextGenerator(desc, filer).gen()
    }

    private fun genDefaultHandlerContextClass() {
        DefaultHandlerContextGenerator(desc, filer).gen()
    }


    private fun genHandlerInterface() {
        HandlerGenerator(desc, filer).gen()
    }
}
