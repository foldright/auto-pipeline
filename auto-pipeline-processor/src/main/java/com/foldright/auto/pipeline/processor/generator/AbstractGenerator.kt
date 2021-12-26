package com.foldright.auto.pipeline.processor.generator

import com.foldright.auto.pipeline.processor.AutoPipelineClassDescriptor
import com.foldright.auto.pipeline.processor.AutoPipelineOperatorsDescriptor
import com.squareup.javapoet.*
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.type.TypeVariable

abstract class AbstractGenerator(private val desc: AutoPipelineClassDescriptor) {

    companion object {
        private const val indent: String = "    "
    }

    protected fun javaFileBuilder(packageName: String, typeSpec: TypeSpec): JavaFile.Builder =
        JavaFile.builder(packageName, typeSpec)
            .skipJavaLangImports(true)
            .indent(indent)


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
