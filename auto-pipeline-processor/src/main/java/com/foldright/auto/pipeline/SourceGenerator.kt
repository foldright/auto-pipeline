package com.foldright.auto.pipeline

import com.squareup.javapoet.*
import com.squareup.javapoet.TypeName.*
import org.apache.commons.lang3.StringUtils
import javax.annotation.processing.Filer
import javax.lang.model.element.Modifier.*

class SourceGenerator(private val descriptor: AutoPipelineClassDescriptor, private val filer: Filer) {

    private val newPackageName = "${descriptor.packageName}.pipeline"

    private val pipelineName = "${descriptor.simpleName}Pipeline"
    private val handlerContextName = "${descriptor.simpleName}HandlerContext"
    private val abstractHandlerContextName = "Abstract${handlerContextName}"
    private val defaultHandlerContextName = "Default${handlerContextName}"
    private val handlerName = "${descriptor.simpleName}Handler"

    private val pipelineTypeName = ClassName.get(newPackageName, pipelineName)
    private val handlerContextTypeName = ClassName.get(newPackageName, handlerContextName)
    private val abstractHandlerContextTypeName = ClassName.get(newPackageName, abstractHandlerContextName)
    private val defaultHandlerContextTypeName = ClassName.get(newPackageName, defaultHandlerContextName)
    private val handlerTypeName = ClassName.get(newPackageName, handlerName)

    private val listTypeName = ClassName.get("java.util", "List")

    fun genPipeline() {
        val pipelineClassBuilder = TypeSpec.classBuilder(pipelineName)
            .addModifiers(PUBLIC)
            .addSuperinterface(descriptor.typeMirror)
            .addField(abstractHandlerContextTypeName, "head", PRIVATE, FINAL)
            .addField(abstractHandlerContextTypeName, "tail", PRIVATE, FINAL)

        val pipelineConstructor = MethodSpec.constructorBuilder()
            .addModifiers(PUBLIC)
            .addCode(
                """
                head = new HeadContext(this);
                tail = new TailContext(this);
                
                head.prev = null;
                head.next = tail;
                tail.prev = head;
                tail.next = null;
            """.trimIndent()
            ).build()
        pipelineClassBuilder.addMethod(pipelineConstructor)
        pipelineClassBuilder.addMethods(genPipelineOverrideMethods {
            when (TypeName.get(it.returnType)) {
                VOID -> """head.${it.methodName}(${it.params.joinToString(", ") { it.simpleName }});"""
                else -> """return head.${it.methodName}(${it.params.joinToString(", ") { it.simpleName }});"""
            }
        })

        val addFirstMethod = MethodSpec.methodBuilder("addFirst")
            .addModifiers(PUBLIC, SYNCHRONIZED)
            .addParameter(handlerTypeName, "handler")
            .returns(pipelineTypeName)
            .addCode(
                """
                $abstractHandlerContextName newCtx = new ${defaultHandlerContextName}(this, handler);
                $abstractHandlerContextName nextCtx = head.next;
                head.next = newCtx;
                newCtx.prev = head;
                newCtx.next = nextCtx;
                nextCtx.prev = newCtx;
                
                return this;
            """.trimIndent()
            ).build()
        pipelineClassBuilder.addMethod(addFirstMethod)


        val addFirstListMethod = MethodSpec.methodBuilder("addFirst")
            .addModifiers(PUBLIC, SYNCHRONIZED)
            .addParameter(ParameterizedTypeName.get(listTypeName, handlerTypeName), "handlers")
            .returns(pipelineTypeName)
            .addCode(
                """
                if (handlers == null || handlers.isEmpty()) {
                    return this;
                }

                int size = handlers.size();
                for (int i = 0; i < size; i++) {
                    addFirst(handlers.get(size - i - 1));
                }
        
                return this;
            """.trimIndent()
            ).build()
        pipelineClassBuilder.addMethod(addFirstListMethod)

        val addLastMethod = MethodSpec.methodBuilder("addLast")
            .addModifiers(PUBLIC, SYNCHRONIZED)
            .addParameter(handlerTypeName, "handler")
            .returns(pipelineTypeName)
            .addCode(
                """
                $abstractHandlerContextName newCtx = new ${defaultHandlerContextName}(this, handler);
                $abstractHandlerContextName prevCtx = tail.prev;
                
                newCtx.prev = prevCtx;
                newCtx.next = tail;
                prevCtx.next = newCtx;
                tail.prev = newCtx;
                
                return this;
            """.trimIndent()
            ).build()
        pipelineClassBuilder.addMethod(addLastMethod)

        val addLastListMethod = MethodSpec.methodBuilder("addLast")
            .addModifiers(PUBLIC, SYNCHRONIZED)
            .addParameter(ParameterizedTypeName.get(listTypeName, handlerTypeName), "handlers")
            .returns(pipelineTypeName)
            .addCode(
                """
                if (handlers == null || handlers.isEmpty()) {
                    return this;
                }
                
                for (ConfigSourceHandler handler : handlers) {
                    addLast(handler);
                }
                
                return this;
            """.trimIndent()
            ).build()
        pipelineClassBuilder.addMethod(addLastListMethod)


        val headContextClass = TypeSpec.classBuilder("HeadContext")
            .addModifiers(PRIVATE, STATIC)
            .superclass(abstractHandlerContextTypeName)
            .addMethod(
                MethodSpec.constructorBuilder()
                    .addParameter(pipelineTypeName, "pipeline")
                    .addStatement("super(pipeline)")
                    .build()
            )
            .addMethod(
                MethodSpec.methodBuilder("handler")
                    .addAnnotation(Override::class.java)
                    .addModifiers(PROTECTED)
                    .returns(handlerTypeName)
                    .addStatement("return null")
                    .build()
            )
            .addMethods(genPipelineOverrideMethods {
                when (TypeName.get(it.returnType)) {
                    VOID -> """next.${it.methodName}(${it.params.joinToString(", ") { it.simpleName }});"""
                    else -> """return next.${it.methodName}(${it.params.joinToString(", ") { it.simpleName }});"""
                }
            })
            .build()
        pipelineClassBuilder.addType(headContextClass)


        val tailContextClass = TypeSpec.classBuilder("TailContext")
            .addModifiers(PRIVATE, STATIC)
            .superclass(abstractHandlerContextTypeName)
            .addMethod(
                MethodSpec.constructorBuilder()
                    .addParameter(pipelineTypeName, "pipeline")
                    .addStatement("super(pipeline)")
                    .build()
            )
            .addMethod(
                MethodSpec.methodBuilder("handler")
                    .addAnnotation(Override::class.java)
                    .addModifiers(PROTECTED)
                    .returns(handlerTypeName)
                    .addStatement("return null")
                    .build()
            )
            .addMethods(genPipelineOverrideMethods {
                val typeName = TypeName.get(it.returnType)
                when {
                    !typeName.isPrimitive -> "return null;"
                    else -> when (typeName) {
                        // TODO： 生成默认值可能不是好的做法，因为这假设了用户期望 tail 的行为
                        VOID -> ""
                        BOOLEAN -> "return false;"
                        BYTE, SHORT, INT, LONG, FLOAT, DOUBLE -> "return 0;"
                        CHAR -> "return '0';"
                        else -> ""
                    }
                }
            })
            .build()
        pipelineClassBuilder.addType(tailContextClass)


        JavaFile.builder(newPackageName, pipelineClassBuilder.build())
            .skipJavaLangImports(true)
            .build()
            .writeTo(filer)
    }

    private fun genPipelineOverrideMethods(statement: (AutoPipelineOperatorsDescriptor) -> String): List<MethodSpec> =
        descriptor.operations.map {
            MethodSpec.methodBuilder(it.methodName)
                .addModifiers(PUBLIC)
                .addAnnotation(Override::class.java)
                .addParameters(it.params.map { param -> ParameterSpec.get(param) })
                .returns(get(it.returnType))
                .addCode(statement.invoke(it))
                .build()
        }

    fun genHandlerContextInterface() {
        val contextInterface = TypeSpec.interfaceBuilder(handlerContextName)
            .addModifiers(PUBLIC)
            .addSuperinterface(descriptor.typeMirror)
            .addMethod(
                MethodSpec.methodBuilder("pipeline")
                    .addModifiers(PUBLIC, ABSTRACT)
                    .returns(ClassName.get(newPackageName, pipelineName))
                    .build()
            )
            .build()

        JavaFile.builder(newPackageName, contextInterface)
            .skipJavaLangImports(true)
            .build()
            .writeTo(filer)
    }

    fun genAbstractHandlerContextClass() {
        val abstractContextClassBuilder = TypeSpec.classBuilder(abstractHandlerContextName)
            .addModifiers(PUBLIC, ABSTRACT)
            .addSuperinterface(ClassName.get(newPackageName, handlerContextName))

        val pipelineField =
            FieldSpec.builder(ClassName.get(newPackageName, pipelineName), "pipeline", PRIVATE, FINAL).build()
        abstractContextClassBuilder.addField(pipelineField)

        val prevContextField =
            FieldSpec.builder(abstractHandlerContextTypeName, "prev", VOLATILE)
                .build()
        abstractContextClassBuilder.addField(prevContextField)

        val nextContextField =
            FieldSpec.builder(abstractHandlerContextTypeName, "next", VOLATILE)
                .build()
        abstractContextClassBuilder.addField(nextContextField)

        val constructor = MethodSpec.constructorBuilder()
            .addModifiers(PUBLIC)
            .addParameter(ParameterSpec.builder(ClassName.get(newPackageName, pipelineName), "pipeline").build())
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
            .returns(handlerTypeName)
            .build()
        abstractContextClassBuilder.addMethod(handlerMethod)

        val pipelineMethod = MethodSpec.methodBuilder("pipeline")
            .addAnnotation(Override::class.java)
            .addModifiers(PUBLIC)
            .returns(pipelineTypeName)
            .addCode("return pipeline;")
            .build()
        abstractContextClassBuilder.addMethod(pipelineMethod)


        JavaFile.builder(newPackageName, abstractContextClassBuilder.build())
            .skipJavaLangImports(true)
            .build()
            .writeTo(filer)
    }

    fun genDefaultHandlerContextClass() {
        val defaultContextClassBuilder = TypeSpec.classBuilder(defaultHandlerContextName)
            .addModifiers(PUBLIC)
            .superclass(abstractHandlerContextTypeName)
            .addField(handlerTypeName, "handler", PRIVATE, FINAL)

        val constructor = MethodSpec.constructorBuilder()
            .addModifiers(PUBLIC)
            .addParameter(pipelineTypeName, "pipeline")
            .addParameter(handlerTypeName, "handler")
            .addStatement("super(pipeline)")
            .addStatement("this.handler = handler")
            .build()
        defaultContextClassBuilder.addMethod(constructor)

        val handlerMethod = MethodSpec.methodBuilder("handler")
            .addAnnotation(Override::class.java)
            .addModifiers(PROTECTED)
            .returns(handlerTypeName)
            .addStatement("return handler")
            .build()
        defaultContextClassBuilder.addMethod(handlerMethod)

        JavaFile.builder(newPackageName, defaultContextClassBuilder.build())
            .skipJavaLangImports(true)
            .build()
            .writeTo(filer)
    }


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
                .returns(get(it.returnType))
                .build()

            handlerTypeBuilder.addMethod(operationMethod)
        }

        JavaFile.builder(newPackageName, handlerTypeBuilder.build())
            .skipJavaLangImports(true)
            .build()
            .writeTo(filer)
    }
}