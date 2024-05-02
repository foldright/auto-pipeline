package com.foldright.auto.pipeline.processor

import com.foldright.auto.pipeline.AutoPipeline
import com.foldright.auto.pipeline.PipelineDirection
import com.squareup.javapoet.*
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier.ABSTRACT
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

class AutoPipelineClassDescriptor(
    elements: Elements,
    private val types: Types,
    private val entityElement: TypeElement
) {
    // entity description
    private val entityPackage = elements.getPackageOf(entityElement).toString()
    private val entitySimpleName = entityElement.simpleName.toString()
    val entityType: TypeName = TypeName.get(entityElement.asType())

    val entityDeclaredTypeVariables: List<TypeVariableName> by lazy {
        if (entityType is ParameterizedTypeName) {
            entityElement.typeParameters.map { TypeVariableName.get(it) }
        } else {
            listOf()
        }
    }

    private val entityAnnotations: List<AnnotationSpec> by lazy {
        entityElement.annotationMirrors
            .map { AnnotationSpec.get(it) }
            .filterNot {
                val tp = it.type
                if (tp is ClassName) {
                    tp.canonicalName() == AutoPipeline::class.java.canonicalName
                } else {
                    false
                }
            }
    }

    // new package for all pipeline source code
    private val newPackageName = "${entityPackage}.pipeline"

    val listTypeName: ClassName = ClassName.get("java.util", "List")


    // pipeline description
    private val pipelineSimpleName = "${entitySimpleName}Pipeline"
    val pipelineRawClassName: ClassName = ClassName.get(newPackageName, pipelineSimpleName)
    val pipelineTypeName: TypeName by lazy {
        if (entityDeclaredTypeVariables.isEmpty()) {
            pipelineRawClassName
        } else {
            ParameterizedTypeName.get(pipelineRawClassName, *entityDeclaredTypeVariables.toTypedArray())
        }
    }


    // handlerContext description
    private val handlerContextName = "${entitySimpleName}HandlerContext"
    val handlerContextRawClassName: ClassName = ClassName.get(newPackageName, handlerContextName)
    val handlerContextTypeName: TypeName by lazy {
        if (entityDeclaredTypeVariables.isEmpty()) {
            handlerContextRawClassName
        } else {
            ParameterizedTypeName.get(handlerContextRawClassName, *entityDeclaredTypeVariables.toTypedArray())
        }
    }


    // abstractHandlerContext description
    private val abstractHandlerContextName = "Abstract${handlerContextName}"
    val abstractHandlerContextRawClassName: ClassName = ClassName.get(newPackageName, abstractHandlerContextName)
    val abstractHandlerContextTypeName: TypeName by lazy {
        if (entityDeclaredTypeVariables.isEmpty()) {
            abstractHandlerContextRawClassName
        } else {
            ParameterizedTypeName.get(abstractHandlerContextRawClassName, *entityDeclaredTypeVariables.toTypedArray())
        }
    }


    // defaultHandlerContext
    private val defaultHandlerContextName = "Default${handlerContextName}"
    val defaultHandlerContextRawClassName: ClassName = ClassName.get(newPackageName, defaultHandlerContextName)
    val defaultHandlerContextTypeName: TypeName by lazy {
        if (entityDeclaredTypeVariables.isEmpty()) {
            defaultHandlerContextRawClassName
        } else {
            ParameterizedTypeName.get(defaultHandlerContextRawClassName, *entityDeclaredTypeVariables.toTypedArray())
        }
    }


    // handler
    private val handlerName = "${entitySimpleName}Handler"
    val handlerRawClassName: ClassName = ClassName.get(newPackageName, handlerName)
    val handlerTypeName: TypeName by lazy {
        if (entityDeclaredTypeVariables.isEmpty()) {
            handlerRawClassName
        } else {
            ParameterizedTypeName.get(handlerRawClassName, *entityDeclaredTypeVariables.toTypedArray())
        }
    }


    val entityMethods = elements.getAllMembers(entityElement)
        .filterNotNull()
        .filterIsInstance<ExecutableElement>()
        .filter {
            it.kind == ElementKind.METHOD && it.modifiers.contains(PUBLIC) && it.modifiers.contains(ABSTRACT)
        }
        .map { AutoPipelineOperatorsDescriptor(it, entityElement) }
}

class AutoPipelineOperatorsDescriptor(
    val executableElement: ExecutableElement,
    private val entityElement: TypeElement
) {
    val methodName = executableElement.simpleName.toString()
    val returnType: TypeMirror = executableElement.returnType
    val params: List<VariableElement> = executableElement.parameters

    private val defaultDirection: PipelineDirection.Direction by lazy {
        entityElement.getAnnotation(PipelineDirection::class.java)?.value ?: PipelineDirection.Direction.FORWARD
    }

    val direction: PipelineDirection.Direction by lazy {
        executableElement.getAnnotation(PipelineDirection::class.java)?.value ?: defaultDirection
    }

    companion object {
        fun List<VariableElement>.expand(): CharSequence = this.joinToString(",") {
            it.simpleName
        }

        fun List<VariableElement>.expandAndAdd(additional: CharSequence): CharSequence = when {
            this.isEmpty() -> additional
            else -> "${this.expand()} , $additional"
        }

    }
}
