package com.foldright.auto.pipeline

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeVariableName
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
    val entityElement: TypeElement
) {
    // entity description
    private val entityPackage = elements.getPackageOf(entityElement).toString()
    private val entitySimpleName = entityElement.simpleName.toString()
    val entityType: TypeName = TypeName.get(entityElement.asType())
    val entityTypeVariables = entityElement.typeParameters.map { TypeVariableName.get(it) }
    val entityTypeWithTypeParameters: ParameterizedTypeName =
        ParameterizedTypeName.get(ClassName.get(entityElement), *entityTypeVariables.toTypedArray())


    // new package for all pipeline source code
    private val newPackageName = "${entityPackage}.pipeline"

    val listTypeName: ClassName = ClassName.get("java.util", "List")


    // pipeline description
    private val pipelineSimpleName = "${entitySimpleName}Pipeline"
    val pipelineTypeName: ClassName = ClassName.get(newPackageName, pipelineSimpleName)


    // handlerContext description
    private val handlerContextName = "${entitySimpleName}HandlerContext"
    val handlerContextTypeName: ClassName = ClassName.get(newPackageName, handlerContextName)


    // abstractHandlerContext description
    private val abstractHandlerContextName = "Abstract${handlerContextName}"
    val abstractHandlerContextTypeName: ClassName = ClassName.get(newPackageName, abstractHandlerContextName)


    // defaultHandlerContext
    private val defaultHandlerContextName = "Default${handlerContextName}"
    val defaultHandlerContextTypeName: ClassName = ClassName.get(newPackageName, defaultHandlerContextName)


    // handler
    private val handlerName = "${entitySimpleName}Handler"
    val handlerTypeName: ClassName = ClassName.get(newPackageName, handlerName)


    val entityOperations = elements.getAllMembers(entityElement)
        .filterNotNull()
        .filterIsInstance(ExecutableElement::class.java)
        .filter {
            it.modifiers.contains(PUBLIC) && it.modifiers.contains(ABSTRACT)
        }
        .map { AutoPipelineOperatorsDescriptor(it) }
}

class AutoPipelineOperatorsDescriptor(executableElement: ExecutableElement) {
    val methodName = executableElement.simpleName.toString()
    val returnType: TypeMirror = executableElement.returnType
    val params: MutableList<out VariableElement> = executableElement.parameters
}