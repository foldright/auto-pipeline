package com.foldright.auto.pipeline.processor.generator

import com.foldright.auto.pipeline.processor.AutoPipelineClassDescriptor
import com.foldright.auto.pipeline.processor.AutoPipelineOperatorsDescriptor
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeVariableName
import javax.annotation.processing.Filer
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.type.TypeVariable

abstract class AbstractGenerator(private val desc: AutoPipelineClassDescriptor, private val filer: Filer) {

    protected fun genPipelineOverrideMethods(statement: (AutoPipelineOperatorsDescriptor) -> String): List<MethodSpec> =
        desc.entityMethods.map {
            MethodSpec.overriding(it.executableElement)
                .addCode(statement.invoke(it))
                .build()
        }

    protected fun createMethodSpecBuilder(method: ExecutableElement): MethodSpec.Builder {
        val modifiers = method.modifiers
        require(
            !(modifiers.contains(Modifier.PRIVATE)
                    || modifiers.contains(Modifier.FINAL)
                    || modifiers.contains(Modifier.STATIC))
        ) { "cannot override method with modifiers: $modifiers" }


        val methodName = method.simpleName.toString()
        val methodBuilder = MethodSpec.methodBuilder(methodName)

        methodBuilder.addModifiers(modifiers)
        for (typeParameterElement in method.typeParameters) {
            val typeVariable = typeParameterElement.asType() as TypeVariable
            methodBuilder.addTypeVariable(TypeVariableName.get(typeVariable))
        }
        methodBuilder.returns(TypeName.get(method.returnType))

        for (parameter in method.parameters) {
            methodBuilder.addParameter(ParameterSpec.get(parameter))
        }

        methodBuilder.varargs(method.isVarArgs)

        for (thrownType in method.thrownTypes) {
            methodBuilder.addException(TypeName.get(thrownType))
        }

        return methodBuilder
    }
}
