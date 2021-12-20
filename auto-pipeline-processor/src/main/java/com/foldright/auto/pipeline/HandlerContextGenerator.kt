package com.foldright.auto.pipeline

import com.squareup.javapoet.*
import javax.annotation.processing.Filer
import javax.lang.model.element.Modifier

class HandlerContextGenerator(private val desc: AutoPipelineClassDescriptor, private val filer: Filer) {

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