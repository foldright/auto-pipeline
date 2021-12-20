package com.foldright.auto.pipeline.generator

import com.foldright.auto.pipeline.AutoPipelineClassDescriptor
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import javax.annotation.processing.Filer
import javax.lang.model.element.Modifier

class HandlerContextGenerator(private val desc: AutoPipelineClassDescriptor, private val filer: Filer) :
    AbstractGenerator(desc, filer) {

    fun gen() {
        val contextInterface = TypeSpec.interfaceBuilder(desc.handlerContextRawClassName)
            .addModifiers(Modifier.PUBLIC)
            .addTypeVariables(desc.entityDeclaredTypeVariables)
            .addSuperinterface(desc.entityType)
            .addMethod(
                MethodSpec.methodBuilder("pipeline")
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .returns(desc.pipelineTypeName)
                    .build()
            )
            .build()

        JavaFile.builder(desc.handlerContextRawClassName.packageName(), contextInterface)
            .skipJavaLangImports(true)
            .build()
            .writeTo(filer)
    }
}