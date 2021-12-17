package com.foldright.auto.pipeline

import com.squareup.javapoet.*
import org.apache.commons.lang3.StringUtils
import javax.annotation.processing.Filer
import javax.lang.model.element.Modifier.ABSTRACT
import javax.lang.model.element.Modifier.PUBLIC

class SourceGenerator(private val descriptor: AutoPipelineClassDescriptor, private val filer: Filer) {

    private val newPackageName = "${descriptor.packageName}.pipeline"
    private val pipelineName = "${descriptor.simpleName}Pipeline"
    private val contextHandlerName = "${descriptor.simpleName}Context"
    private val handlerName = "${descriptor.simpleName}Handler"


    fun genPipelineInterface() {
        val pipelineType = TypeSpec.interfaceBuilder(pipelineName)
            .addModifiers(PUBLIC)
            .addSuperinterface(descriptor.typeMirror)
            .addMethod(genComponentMethod())
            .build()

        JavaFile.builder(newPackageName, pipelineType)
            .skipJavaLangImports(true)
            .build()
            .writeTo(filer)
    }

    fun genHandlerContextInterface() {
        val contextType = TypeSpec.interfaceBuilder(contextHandlerName)
            .addModifiers(PUBLIC)
            .addSuperinterface(descriptor.typeMirror)
            .addMethod(genComponentMethod())
            .build()

        JavaFile.builder(newPackageName, contextType)
            .skipJavaLangImports(true)
            .build()
            .writeTo(filer)
    }

    fun genHandlerInterface() {
        val handlerTypeBuilder = TypeSpec.interfaceBuilder(handlerName).addModifiers(PUBLIC)

        val contextParam = ParameterSpec.builder(
            ClassName.get(newPackageName, contextHandlerName),
            StringUtils.uncapitalize(contextHandlerName)
        ).build()

        descriptor.operations.forEach {
            val operationMethod = MethodSpec.methodBuilder(it.methodName)
                .addModifiers(ABSTRACT, PUBLIC)
                .addParameters(it.params.map { param -> ParameterSpec.get(param) })
                .addParameter(contextParam)
                .returns(TypeName.get(it.returnType))
                .build()

            handlerTypeBuilder.addMethod(operationMethod)
        }

        JavaFile.builder(newPackageName, handlerTypeBuilder.build())
            .skipJavaLangImports(true)
            .build()
            .writeTo(filer)
    }


    private fun genComponentMethod() =
        MethodSpec.methodBuilder(StringUtils.uncapitalize(descriptor.simpleName))
            .addModifiers(PUBLIC, ABSTRACT)
            .returns(TypeName.get(descriptor.typeMirror))
            .build()
}