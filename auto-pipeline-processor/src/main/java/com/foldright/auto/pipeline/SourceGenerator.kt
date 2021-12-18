package com.foldright.auto.pipeline

import com.squareup.javapoet.*
import org.apache.commons.lang3.StringUtils
import javax.annotation.processing.Filer
import javax.lang.model.element.Modifier.*

class SourceGenerator(private val descriptor: AutoPipelineClassDescriptor, private val filer: Filer) {

    private val newPackageName = "${descriptor.packageName}.pipeline"
    private val pipelineName = "${descriptor.simpleName}Pipeline"
    private val handlerContextName = "${descriptor.simpleName}Context"
    private val abstractHandlerContextName = "Abstract${handlerContextName}"
    private val handlerName = "${descriptor.simpleName}Handler"


    fun genPipeline() {
        val pipelineType = TypeSpec.interfaceBuilder(pipelineName)
            .addModifiers(PUBLIC)
            .addSuperinterface(descriptor.typeMirror)
            .build()

        JavaFile.builder(newPackageName, pipelineType)
            .skipJavaLangImports(true)
            .build()
            .writeTo(filer)
    }

    fun genHandlerContextInterface() {
        val contextInterface = TypeSpec.interfaceBuilder(handlerContextName)
            .addModifiers(PUBLIC)
            .addSuperinterface(descriptor.typeMirror)
            .addMethod(pipelineMethodBuilder().build())
            .build()

        JavaFile.builder(newPackageName, contextInterface)
            .skipJavaLangImports(true)
            .build()
            .writeTo(filer)
    }

    fun genAbstractHandlerContextClass() {
        val pipelineField =
            FieldSpec.builder(ClassName.get(newPackageName, pipelineName), "pipeline", PRIVATE, FINAL).build()

        val prevContextField =
            FieldSpec.builder(ClassName.get(newPackageName, abstractHandlerContextName), "prev", VOLATILE)
                .build()

        val nextContextField =
            FieldSpec.builder(ClassName.get(newPackageName, abstractHandlerContextName), "next", VOLATILE)
                .build()

        val constructor = MethodSpec.constructorBuilder()
            .addModifiers(PUBLIC)
            .addParameter(ParameterSpec.builder(ClassName.get(newPackageName, pipelineName), "pipeline").build())
            .addCode(
                """
                this.pipeline = pipeline;
                """.trimMargin()
            )
            .build()


        val abstractContextClass = TypeSpec.classBuilder(abstractHandlerContextName)
            .addModifiers(PUBLIC, ABSTRACT)
            .addSuperinterface(ClassName.get(newPackageName, handlerContextName))
            .addField(pipelineField)
            .addField(prevContextField)
            .addField(nextContextField)
            .addMethod(constructor)
            .build()

        JavaFile.builder(newPackageName, abstractContextClass)
            .skipJavaLangImports(true)
            .build()
            .writeTo(filer)
    }

    private fun pipelineMethodBuilder() =
        MethodSpec.methodBuilder("pipeline")
            .addModifiers(PUBLIC, ABSTRACT)
            .returns(ClassName.get(newPackageName, pipelineName))


    fun genHandlerInterface() {
        val handlerTypeBuilder = TypeSpec.interfaceBuilder(handlerName).addModifiers(PUBLIC)

        val contextParam = ParameterSpec.builder(
            ClassName.get(newPackageName, handlerContextName),
            StringUtils.uncapitalize(handlerContextName)
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
}