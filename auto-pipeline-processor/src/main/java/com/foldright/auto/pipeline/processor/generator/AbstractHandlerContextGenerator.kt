package com.foldright.auto.pipeline.processor.generator

import com.foldright.auto.pipeline.processor.AutoPipelineClassDescriptor
import com.foldright.auto.pipeline.processor.AutoPipelineOperatorsDescriptor.Companion.expand
import com.squareup.javapoet.*
import javax.annotation.processing.Filer
import javax.lang.model.element.Modifier

class AbstractHandlerContextGenerator(private val desc: AutoPipelineClassDescriptor, private val filer: Filer) :
    AbstractGenerator(desc) {

    fun gen() {
        val abstractContextClassBuilder = TypeSpec.classBuilder(desc.abstractHandlerContextRawClassName)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addTypeVariables(desc.entityDeclaredTypeVariables)
            .addSuperinterface(desc.handlerContextTypeName)

        val pipelineField =
            FieldSpec.builder(desc.pipelineTypeName, "pipeline", Modifier.PRIVATE, Modifier.FINAL).build()
        abstractContextClassBuilder.addField(pipelineField)

        val prevContextField =
            FieldSpec.builder(desc.abstractHandlerContextTypeName, "prev", Modifier.VOLATILE)
                .build()
        abstractContextClassBuilder.addField(prevContextField)

        val nextContextField =
            FieldSpec.builder(desc.abstractHandlerContextTypeName, "next", Modifier.VOLATILE)
                .build()
        abstractContextClassBuilder.addField(nextContextField)

        val constructor = MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .addParameter(ParameterSpec.builder(desc.pipelineTypeName, "pipeline").build())
            .addCode(
                """
                this.pipeline = pipeline;
                """.trimMargin()
            )
            .build()
        abstractContextClassBuilder.addMethod(constructor)

        val operationMethods = genPipelineOverrideMethods {
            when (TypeName.get(it.returnType)) {
                TypeName.VOID -> """handler().${it.methodName}(${it.params.expand()}, next);"""
                else -> """return handler().${it.methodName}(${it.params.expand()}, next);"""
            }
        }
        abstractContextClassBuilder.addMethods(operationMethods)

        val handlerMethod = MethodSpec.methodBuilder("handler")
            .addModifiers(Modifier.PROTECTED, Modifier.ABSTRACT)
            .returns(desc.handlerTypeName)
            .build()
        abstractContextClassBuilder.addMethod(handlerMethod)

        val pipelineMethod = MethodSpec.methodBuilder("pipeline")
            .addAnnotation(Override::class.java)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .returns(desc.pipelineTypeName)
            .addCode("return pipeline;")
            .build()
        abstractContextClassBuilder.addMethod(pipelineMethod)

        javaFileBuilder(desc.abstractHandlerContextRawClassName.packageName(), abstractContextClassBuilder.build())
            .build()
            .writeTo(filer)
    }
}
