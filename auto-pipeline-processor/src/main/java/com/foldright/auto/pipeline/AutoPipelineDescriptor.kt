package com.foldright.auto.pipeline

import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier.ABSTRACT
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

class AutoPipelineClassDescriptor(
    private val elements: Elements,
    private val types: Types,
    private val element: TypeElement
) {
    val typeMirror = element.asType()
    val packageName = elements.getPackageOf(element).toString()
    val simpleName = element.simpleName.toString()
    val operations = elements.getAllMembers(element)
        .filterNotNull()
        .filterIsInstance(ExecutableElement::class.java)
        .filter {
            it.modifiers.contains(PUBLIC) && it.modifiers.contains(ABSTRACT)
        }
        .map { AutoPipelineMethodDescriptor(it) }
}

class AutoPipelineMethodDescriptor(private val executableElement: ExecutableElement) {
    val methodName = executableElement.simpleName.toString()
    val returnType = executableElement.returnType
    val params = executableElement.parameters
}