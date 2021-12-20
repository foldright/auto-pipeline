package com.foldright.auto.pipeline.generator

import com.foldright.auto.pipeline.AutoPipelineClassDescriptor
import com.squareup.javapoet.*
import org.apache.commons.lang3.StringUtils
import javax.annotation.processing.Filer
import javax.lang.model.element.Modifier

class HandlerGenerator (private val desc: AutoPipelineClassDescriptor, private val filer: Filer) {

    fun gen() {
        val handlerTypeBuilder = TypeSpec.interfaceBuilder(desc.handlerRawClassName)
            .addTypeVariables(desc.entityDeclaredTypeVariables)
            .addModifiers(Modifier.PUBLIC)

        val contextParam = ParameterSpec.builder(
            desc.handlerContextTypeName, StringUtils.uncapitalize(desc.handlerContextRawClassName.simpleName())
        ).build()

        desc.entityMethods.forEach {
            val operationMethod = MethodSpec.methodBuilder(it.methodName)
                .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
                .addParameters(it.params.map { param -> ParameterSpec.get(param) })
                .addParameter(contextParam)
                .returns(TypeName.get(it.returnType))
                .build()

            handlerTypeBuilder.addMethod(operationMethod)
        }

        JavaFile.builder(desc.handlerRawClassName.packageName(), handlerTypeBuilder.build())
            .skipJavaLangImports(true)
            .build()
            .writeTo(filer)
    }
}