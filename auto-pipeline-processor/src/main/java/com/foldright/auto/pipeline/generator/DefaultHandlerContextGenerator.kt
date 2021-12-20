package com.foldright.auto.pipeline.generator

import com.foldright.auto.pipeline.AutoPipelineClassDescriptor
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import javax.annotation.processing.Filer
import javax.lang.model.element.Modifier

class DefaultHandlerContextGenerator(private val desc: AutoPipelineClassDescriptor, private val filer: Filer) :
    AbstractGenerator(desc, filer) {

    fun gen() {
        val defaultContextClassBuilder = TypeSpec.classBuilder(desc.defaultHandlerContextRawClassName)
            .addModifiers(Modifier.PUBLIC)
            .addTypeVariables(desc.entityDeclaredTypeVariables)
            .superclass(desc.abstractHandlerContextTypeName)
            .addField(desc.handlerRawClassName, "handler", Modifier.PRIVATE, Modifier.FINAL)

        val constructor = MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .addParameter(desc.pipelineTypeName, "pipeline")
            .addParameter(desc.handlerRawClassName, "handler")
            .addStatement("super(pipeline)")
            .addStatement("this.handler = handler")
            .build()
        defaultContextClassBuilder.addMethod(constructor)

        val handlerMethod = MethodSpec.methodBuilder("handler")
            .addAnnotation(Override::class.java)
            .addModifiers(Modifier.PROTECTED)
            .returns(desc.handlerRawClassName)
            .addStatement("return handler")
            .build()
        defaultContextClassBuilder.addMethod(handlerMethod)

        JavaFile.builder(desc.defaultHandlerContextRawClassName.packageName(), defaultContextClassBuilder.build())
            .skipJavaLangImports(true)
            .build()
            .writeTo(filer)
    }

}