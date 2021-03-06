package com.foldright.auto.pipeline.processor.generator

import com.foldright.auto.pipeline.processor.AutoPipelineClassDescriptor
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import javax.annotation.processing.Filer
import javax.lang.model.element.Modifier

class DefaultHandlerContextGenerator(private val desc: AutoPipelineClassDescriptor, private val filer: Filer) :
    AbstractGenerator(desc) {

    fun gen() {
        val defaultContextClassBuilder = TypeSpec.classBuilder(desc.defaultHandlerContextRawClassName)
            .addModifiers(Modifier.PUBLIC)
            .addTypeVariables(desc.entityDeclaredTypeVariables)
            .superclass(desc.abstractHandlerContextTypeName)
            .addField(desc.handlerTypeName, "handler", Modifier.PRIVATE, Modifier.FINAL)

        val constructor = MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .addParameter(desc.pipelineTypeName, "pipeline")
            .addParameter(desc.handlerTypeName, "handler")
            .addStatement("super(pipeline)")
            .addStatement("this.handler = handler")
            .build()
        defaultContextClassBuilder.addMethod(constructor)

        val handlerMethod = MethodSpec.methodBuilder("handler")
            .addAnnotation(Override::class.java)
            .addModifiers(Modifier.PROTECTED)
            .returns(desc.handlerTypeName)
            .addStatement("return handler")
            .build()
        defaultContextClassBuilder.addMethod(handlerMethod)


        javaFileBuilder(desc.defaultHandlerContextRawClassName.packageName(), defaultContextClassBuilder.build())
            .build()
            .writeTo(filer)
    }

}
