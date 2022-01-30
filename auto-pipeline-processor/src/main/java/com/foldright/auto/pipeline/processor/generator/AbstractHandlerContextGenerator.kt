package com.foldright.auto.pipeline.processor.generator

import com.foldright.auto.pipeline.processor.AutoPipelineClassDescriptor
import com.foldright.auto.pipeline.processor.AutoPipelineOperatorsDescriptor.Companion.expand
import com.squareup.javapoet.*
import javax.annotation.processing.Filer
import javax.lang.model.element.Modifier

class AbstractHandlerContextGenerator(private val desc: AutoPipelineClassDescriptor, private val filer: Filer) :
    AbstractGenerator(desc) {
    private val prevHandlerContextFieldName = "prev"
    private val nextHandlerContextFieldName = "next"
    private val pipelineFieldName = "pipeline"
    private val handlerMethodName = "handler"

    fun gen() {
        val abstractContextClassBuilder = TypeSpec.classBuilder(desc.abstractHandlerContextRawClassName)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addTypeVariables(desc.entityDeclaredTypeVariables)
            .addSuperinterface(desc.handlerContextTypeName)

        val pipelineField =
            FieldSpec.builder(desc.pipelineTypeName, pipelineFieldName, Modifier.PRIVATE, Modifier.FINAL).build()
        abstractContextClassBuilder.addField(pipelineField)

        val prevContextField =
            FieldSpec.builder(desc.abstractHandlerContextTypeName, prevHandlerContextFieldName, Modifier.VOLATILE)
                .build()
        abstractContextClassBuilder.addField(prevContextField)

        val nextContextField =
            FieldSpec.builder(desc.abstractHandlerContextTypeName, nextHandlerContextFieldName, Modifier.VOLATILE)
                .build()
        abstractContextClassBuilder.addField(nextContextField)

        val constructor = MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .addParameter(ParameterSpec.builder(desc.pipelineTypeName, pipelineFieldName).build())
            .addCode(
                """
                this.${pipelineFieldName} = ${pipelineFieldName};
                """.trimMargin()
            )
            .build()
        abstractContextClassBuilder.addMethod(constructor)

        val operationMethods = genPipelineOverrideMethods {
            val invokeHandlerFieldName =
                if (it.reversePipe()) prevHandlerContextFieldName else nextHandlerContextFieldName

            val returnStatement = when (TypeName.get(it.returnType)) {
                TypeName.VOID -> """${handlerMethodName}().${it.methodName}(${it.params.expand()}, ${invokeHandlerFieldName});"""
                else -> """return ${handlerMethodName}().${it.methodName}(${it.params.expand()}, ${invokeHandlerFieldName});"""
            }
            genMarkLastHandlerContextStatement() + returnStatement
        }
        abstractContextClassBuilder.addMethods(operationMethods)

        val handlerMethod = MethodSpec.methodBuilder(handlerMethodName)
            .addModifiers(Modifier.PROTECTED, Modifier.ABSTRACT)
            .returns(desc.handlerTypeName)
            .build()
        abstractContextClassBuilder.addMethod(handlerMethod)

        val pipelineMethod = MethodSpec.methodBuilder(pipelineFieldName)
            .addAnnotation(Override::class.java)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .returns(desc.pipelineTypeName)
            .addCode("return ${pipelineFieldName};")
            .build()
        abstractContextClassBuilder.addMethod(pipelineMethod)

        javaFileBuilder(desc.abstractHandlerContextRawClassName.packageName(), abstractContextClassBuilder.build())
            .build()
            .writeTo(filer)
    }
}
