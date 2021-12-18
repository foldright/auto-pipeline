package com.foldright.auto.pipeline

import com.google.common.collect.Sets
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic

class AutoPipelineProcessor : AbstractProcessor() {
    private lateinit var types: Types
    private lateinit var elements: Elements
    private lateinit var filer: Filer
    private lateinit var messager: Messager

    @Synchronized
    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        types = processingEnv.typeUtils
        elements = processingEnv.elementUtils
        filer = processingEnv.filer
        messager = processingEnv.messager
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val elements = roundEnv.getElementsAnnotatedWith(AutoPipeline::class.java)
        if (elements.isEmpty()) {
            return false;
        }

        for (element in elements) {
            if (element.kind != ElementKind.INTERFACE) {
                error(element, "Only interface can annotated with @${AutoPipeline::class.java.simpleName}")
                return false
            }

            if (!element.modifiers.contains(Modifier.PUBLIC)) {
                error(element, "Only interface can annotated with @${AutoPipeline::class.java.simpleName}")
                return false
            }


            if (element is TypeElement) {
                doProcess(element)
            }
        }

        return false
    }

    private fun doProcess(element: TypeElement) {
        val classDescriptor = AutoPipelineClassDescriptor(elements, types, element)
        val generator = SourceGenerator(classDescriptor, filer)

        generator.genPipeline()
        generator.genHandlerContextInterface()
        generator.genHandlerInterface()
        generator.genAbstractHandlerContextClass()
        generator.genDefaultHandlerContextClass()
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        return Sets.newHashSet(AutoPipeline::class.java.canonicalName)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }


    private fun error(element: Element, message: String) {
        messager.printMessage(Diagnostic.Kind.ERROR, message, element)
    }
}