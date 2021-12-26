package com.foldright.auto.pipeline.processor.generator

import com.foldright.auto.pipeline.processor.AutoPipelineClassDescriptor
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeSpec
import org.apache.commons.lang3.StringUtils
import javax.annotation.processing.Filer
import javax.lang.model.element.Modifier

class HandlerGenerator(private val desc: AutoPipelineClassDescriptor, private val filer: Filer) :
    AbstractGenerator(desc) {

    fun gen() {
        val handlerTypeBuilder = TypeSpec.interfaceBuilder(desc.handlerRawClassName)
            .addTypeVariables(desc.entityDeclaredTypeVariables)
            .addModifiers(Modifier.PUBLIC)

        val contextParam = ParameterSpec.builder(
            desc.handlerContextTypeName, StringUtils.uncapitalize(desc.handlerContextRawClassName.simpleName())
        ).build()

        desc.entityMethods.forEach {
            val operationMethod = createMethodSpecBuilder(it.executableElement)
                .addParameter(contextParam)
                .build()

//            val operationMethod = MethodSpec.methodBuilder(it.methodName)
//                .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
//                .addParameters(it.params.map { param -> ParameterSpec.get(param) })
//                .addParameter(contextParam)
//                .returns(TypeName.get(it.returnType))
//                .build()

            handlerTypeBuilder.addMethod(operationMethod)
        }

        javaFileBuilder(desc.handlerRawClassName.packageName(), handlerTypeBuilder.build())
            .build()
            .writeTo(filer)
    }
}
