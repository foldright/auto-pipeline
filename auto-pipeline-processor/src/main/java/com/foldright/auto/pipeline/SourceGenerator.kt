package com.foldright.auto.pipeline

import com.squareup.javapoet.*
import com.squareup.javapoet.TypeName.*
import org.apache.commons.lang3.StringUtils
import javax.annotation.processing.Filer
import javax.lang.model.element.Modifier.*

class SourceGenerator(private val desc: AutoPipelineClassDescriptor, private val filer: Filer) {

    fun genPipeline() {
        val pipelineClassBuilder = TypeSpec.classBuilder(desc.pipelineTypeName)
            .addTypeVariables(desc.entityTypeVariables)
            .addModifiers(PUBLIC)
            .addSuperinterface(desc.entityType)
            .addField(desc.abstractHandlerContextTypeName, "head", PRIVATE, FINAL)
            .addField(desc.abstractHandlerContextTypeName, "tail", PRIVATE, FINAL)

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
            .addParameter(desc.handlerTypeName, "handler")
            .returns(desc.pipelineTypeName)
            .addCode(
                """
                ${desc.abstractHandlerContextTypeName.simpleName()} newCtx = new ${desc.defaultHandlerContextTypeName.simpleName()}(this, handler);
                ${desc.abstractHandlerContextTypeName.simpleName()} nextCtx = head.next;
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
            .addParameter(ParameterizedTypeName.get(desc.listTypeName, desc.handlerTypeName), "handlers")
            .returns(desc.pipelineTypeName)
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
            .addParameter(desc.handlerTypeName, "handler")
            .returns(desc.pipelineTypeName)
            .addCode(
                """
                ${desc.abstractHandlerContextTypeName.simpleName()} newCtx = new ${desc.defaultHandlerContextTypeName.simpleName()}(this, handler);
                ${desc.abstractHandlerContextTypeName.simpleName()} prevCtx = tail.prev;
                
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
            .addParameter(ParameterizedTypeName.get(desc.listTypeName, desc.handlerTypeName), "handlers")
            .returns(desc.pipelineTypeName)
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
            .superclass(desc.abstractHandlerContextTypeName)
            .addMethod(
                MethodSpec.constructorBuilder()
                    .addParameter(desc.pipelineTypeName, "pipeline")
                    .addStatement("super(pipeline)")
                    .build()
            )
            .addMethod(
                MethodSpec.methodBuilder("handler")
                    .addAnnotation(Override::class.java)
                    .addModifiers(PROTECTED)
                    .returns(desc.handlerTypeName)
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
            .superclass(desc.abstractHandlerContextTypeName)
            .addMethod(
                MethodSpec.constructorBuilder()
                    .addParameter(desc.pipelineTypeName, "pipeline")
                    .addStatement("super(pipeline)")
                    .build()
            )
            .addMethod(
                MethodSpec.methodBuilder("handler")
                    .addAnnotation(Override::class.java)
                    .addModifiers(PROTECTED)
                    .returns(desc.handlerTypeName)
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


        JavaFile.builder(desc.pipelineTypeName.packageName(), pipelineClassBuilder.build())
            .skipJavaLangImports(true)
            .build()
            .writeTo(filer)
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