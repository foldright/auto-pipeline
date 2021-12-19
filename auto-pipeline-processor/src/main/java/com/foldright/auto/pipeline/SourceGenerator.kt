package com.foldright.auto.pipeline

import com.squareup.javapoet.*
import com.squareup.javapoet.TypeName.*
import org.apache.commons.lang3.StringUtils
import javax.annotation.processing.Filer
import javax.lang.model.element.Modifier.*

class SourceGenerator(private val desc: AutoPipelineClassDescriptor, private val filer: Filer) {

    fun genPipeline() {
        PipelineGenerator(desc, filer).gen()
    }

    private fun genPipelineOverrideMethods(statement: (AutoPipelineOperatorsDescriptor) -> String): List<MethodSpec> =
        desc.entityOperations.map {
            MethodSpec.methodBuilder(it.methodName)
                .addModifiers(PUBLIC)
                .addAnnotation(Override::class.java)
                .addParameters(it.params.map { param -> ParameterSpec.get(param) })
                .returns(get(it.returnType))
                .addCode(statement.invoke(it))
                .build()
        }

    fun genHandlerContextInterface() {
        val contextInterface = TypeSpec.interfaceBuilder(desc.handlerContextTypeName)
            .addModifiers(PUBLIC)
            .addSuperinterface(desc.entityType)
            .addMethod(
                MethodSpec.methodBuilder("pipeline")
                    .addModifiers(PUBLIC, ABSTRACT)
                    .returns(desc.pipelineTypeName)
                    .build()
            )
            .build()

        JavaFile.builder(desc.handlerContextTypeName.packageName(), contextInterface)
            .skipJavaLangImports(true)
            .build()
            .writeTo(filer)
    }

    fun genAbstractHandlerContextClass() {
        val abstractContextClassBuilder = TypeSpec.classBuilder(desc.abstractHandlerContextTypeName)
            .addModifiers(PUBLIC, ABSTRACT)
            .addSuperinterface(desc.handlerContextTypeName)

        val pipelineField =
            FieldSpec.builder(desc.pipelineTypeName, "pipeline", PRIVATE, FINAL).build()
        abstractContextClassBuilder.addField(pipelineField)

        val prevContextField =
            FieldSpec.builder(desc.abstractHandlerContextTypeName, "prev", VOLATILE)
                .build()
        abstractContextClassBuilder.addField(prevContextField)

        val nextContextField =
            FieldSpec.builder(desc.abstractHandlerContextTypeName, "next", VOLATILE)
                .build()
        abstractContextClassBuilder.addField(nextContextField)

        val constructor = MethodSpec.constructorBuilder()
            .addModifiers(PUBLIC)
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
                VOID -> """handler().${it.methodName}(${it.params.joinToString(", ") { it.simpleName }}, next);"""
                else -> """return handler().${it.methodName}(${it.params.joinToString(", ") { it.simpleName }}, next);"""
            }
        }
        abstractContextClassBuilder.addMethods(operationMethods)

        val handlerMethod = MethodSpec.methodBuilder("handler")
            .addModifiers(PROTECTED, ABSTRACT)
            .returns(desc.handlerTypeName)
            .build()
        abstractContextClassBuilder.addMethod(handlerMethod)

        val pipelineMethod = MethodSpec.methodBuilder("pipeline")
            .addAnnotation(Override::class.java)
            .addModifiers(PUBLIC)
            .returns(desc.pipelineTypeName)
            .addCode("return pipeline;")
            .build()
        abstractContextClassBuilder.addMethod(pipelineMethod)

        JavaFile.builder(desc.abstractHandlerContextTypeName.packageName(), abstractContextClassBuilder.build())
            .skipJavaLangImports(true)
            .build()
            .writeTo(filer)
    }

    fun genDefaultHandlerContextClass() {
        val defaultContextClassBuilder = TypeSpec.classBuilder(desc.defaultHandlerContextTypeName)
            .addModifiers(PUBLIC)
            .superclass(desc.abstractHandlerContextTypeName)
            .addField(desc.handlerTypeName, "handler", PRIVATE, FINAL)

        val constructor = MethodSpec.constructorBuilder()
            .addModifiers(PUBLIC)
            .addParameter(desc.pipelineTypeName, "pipeline")
            .addParameter(desc.handlerTypeName, "handler")
            .addStatement("super(pipeline)")
            .addStatement("this.handler = handler")
            .build()
        defaultContextClassBuilder.addMethod(constructor)

        val handlerMethod = MethodSpec.methodBuilder("handler")
            .addAnnotation(Override::class.java)
            .addModifiers(PROTECTED)
            .returns(desc.handlerTypeName)
            .addStatement("return handler")
            .build()
        defaultContextClassBuilder.addMethod(handlerMethod)

        JavaFile.builder(desc.defaultHandlerContextTypeName.packageName(), defaultContextClassBuilder.build())
            .skipJavaLangImports(true)
            .build()
            .writeTo(filer)
    }


    fun genHandlerInterface() {
        val handlerTypeBuilder = TypeSpec.interfaceBuilder(desc.handlerTypeName).addModifiers(PUBLIC)

        val contextParam = ParameterSpec.builder(
            desc.handlerContextTypeName, StringUtils.uncapitalize(desc.handlerContextTypeName.simpleName())
        ).build()

        desc.entityOperations.forEach {
            val operationMethod = MethodSpec.methodBuilder(it.methodName)
                .addModifiers(ABSTRACT, PUBLIC)
                .addParameters(it.params.map { param -> ParameterSpec.get(param) })
                .addParameter(contextParam)
                .returns(get(it.returnType))
                .build()

            handlerTypeBuilder.addMethod(operationMethod)
        }

        JavaFile.builder(desc.handlerTypeName.packageName(), handlerTypeBuilder.build())
            .skipJavaLangImports(true)
            .build()
            .writeTo(filer)
    }
}