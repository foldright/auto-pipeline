package com.foldright.auto.pipeline

import com.google.common.collect.Sets
import com.squareup.javapoet.*
import org.apache.commons.lang3.StringUtils
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.*
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic

class AutoPipelineProcessor : AbstractProcessor() {
    private lateinit var types: Types
    private lateinit var elements: Elements
    private lateinit var filer: Filer
    private lateinit var messager: Messager

    @Synchronized
    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        types = processingEnv.typeUtils
        elements = processingEnv.elementUtils
        filer = processingEnv.filer
        messager = processingEnv.messager
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val elements = roundEnv.getElementsAnnotatedWith(AutoPipeline::class.java)
        if (elements.isEmpty()) {
            return false;
        }

        for (element in elements) {
            if (element.kind != ElementKind.INTERFACE) {
                error(element, "Only interface can annotated with @${AutoPipeline::class.java.simpleName}")
                return false
            }

            if (!element.modifiers.contains(Modifier.PUBLIC)) {
                error(element, "Only interface can annotated with @${AutoPipeline::class.java.simpleName}")
                return false
            }


            if (element is TypeElement) {
                doProcess(element)
            }
        }

        // TODO
        return false
    }

    private fun doProcess(element: TypeElement) {
        val packageName = elements.getPackageOf(element).qualifiedName.toString()
        val simpleName = element.simpleName.toString()
        val allMembers = elements.getAllMembers(element)
        val typeMirror = element.asType()

        val newPackageName = "${packageName}.pipeline"

        val method = MethodSpec.methodBuilder(StringUtils.uncapitalize(simpleName))
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .returns(TypeName.get(typeMirror))
            .build()

        val pipelineType = TypeSpec.interfaceBuilder("${simpleName}Pipeline")
            .addSuperinterface(element.asType())
            .addMethod(method)
            .build()

        JavaFile.builder(newPackageName, pipelineType)
            .skipJavaLangImports(true)
            .build()
            .writeTo(filer)


        val contextInterfaceName = "${simpleName}Context"
        val contextType = TypeSpec.interfaceBuilder(contextInterfaceName)
            .addSuperinterface(element.asType())
            .addMethod(method)
            .build()

        JavaFile.builder(newPackageName, contextType)
            .skipJavaLangImports(true)
            .build()
            .writeTo(filer)


        val handlerType = TypeSpec.interfaceBuilder("${simpleName}Handler")
            .addMethod(method)

        allMembers
            .filterNotNull()
            .filterIsInstance(ExecutableElement::class.java)
            .filter { ele ->
                ele.modifiers.contains(Modifier.PUBLIC) && ele.modifiers.contains(Modifier.ABSTRACT)
            }.forEach { ele ->
                val methodName = ele.simpleName.toString()
                val returnType = ele.returnType
                val params = ele.parameters
                val contextParam = ParameterSpec.builder(
                    ClassName.get(newPackageName, contextInterfaceName),
                    StringUtils.uncapitalize(contextInterfaceName)
                ).build()

                val operationMethod = MethodSpec.methodBuilder(methodName)
                    .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
                    .addParameters(params.map { ParameterSpec.get(it) })
                    .addParameter(contextParam)
                    .returns(TypeName.get(returnType))
                    .build()

                handlerType.addMethod(operationMethod)
            }


        JavaFile.builder(newPackageName, handlerType.build())
            .skipJavaLangImports(true)
            .build()
            .writeTo(filer)
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        return Sets.newHashSet(AutoPipeline::class.java.canonicalName)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }


    private fun error(element: Element, message: String) {
        messager.printMessage(Diagnostic.Kind.ERROR, message, element)
    }
}