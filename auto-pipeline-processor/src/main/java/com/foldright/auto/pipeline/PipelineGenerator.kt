package com.foldright.auto.pipeline

import com.squareup.javapoet.*
import javax.annotation.processing.Filer
import javax.lang.model.element.Modifier

class PipelineGenerator(private val desc: AutoPipelineClassDescriptor, private val filer: Filer) {

    fun gen() {
        val pipelineClassBuilder = TypeSpec.classBuilder(desc.pipelineRawClassName)
            .addModifiers(Modifier.PUBLIC)
            .addTypeVariables(desc.entityDeclaredTypeVariables)
            .addSuperinterface(desc.entityType)
            .addField(desc.abstractHandlerContextTypeName, "head", Modifier.PRIVATE, Modifier.FINAL)
            .addField(desc.abstractHandlerContextTypeName, "tail", Modifier.PRIVATE, Modifier.FINAL)

        val pipelineConstructor = MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
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
                TypeName.VOID -> """head.${it.methodName}(${it.params.joinToString(", ") { it.simpleName }});"""
                else -> """return head.${it.methodName}(${it.params.joinToString(", ") { it.simpleName }});"""
            }
        })

        val addFirstMethod = MethodSpec.methodBuilder("addFirst")
            .addModifiers(Modifier.PUBLIC, Modifier.SYNCHRONIZED)
            .addParameter(desc.handlerTypeName, "handler")
            .returns(desc.pipelineTypeName)
            .addCode(
                """
                ${'$'}T newCtx = new ${desc.defaultHandlerContextRawClassName.simpleName()}(this, handler);
                ${'$'}T nextCtx = head.next;
                head.next = newCtx;
                newCtx.prev = head;
                newCtx.next = nextCtx;
                nextCtx.prev = newCtx;
                
                return this;
            """.trimIndent(), desc.abstractHandlerContextTypeName, desc.abstractHandlerContextTypeName
            ).build()
        pipelineClassBuilder.addMethod(addFirstMethod)


        val addFirstListMethod = MethodSpec.methodBuilder("addFirst")
            .addModifiers(Modifier.PUBLIC, Modifier.SYNCHRONIZED)
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
            .addModifiers(Modifier.PUBLIC, Modifier.SYNCHRONIZED)
            .addParameter(desc.handlerTypeName, "handler")
            .returns(desc.pipelineTypeName)
            .addCode(
                """
                ${'$'}T newCtx = new ${desc.defaultHandlerContextRawClassName.simpleName()}(this, handler);
                ${'$'}T prevCtx = tail.prev;
                
                newCtx.prev = prevCtx;
                newCtx.next = tail;
                prevCtx.next = newCtx;
                tail.prev = newCtx;
                
                return this;
            """.trimIndent(), desc.abstractHandlerContextTypeName, desc.abstractHandlerContextTypeName
            ).build()
        pipelineClassBuilder.addMethod(addLastMethod)

        val addLastListMethod = MethodSpec.methodBuilder("addLast")
            .addModifiers(Modifier.PUBLIC, Modifier.SYNCHRONIZED)
            .addParameter(ParameterizedTypeName.get(desc.listTypeName, desc.handlerTypeName), "handlers")
            .returns(desc.pipelineTypeName)
            .addCode(
                """
                if (handlers == null || handlers.isEmpty()) {
                    return this;
                }
                
                for (${'$'}T handler : handlers) {
                    addLast(handler);
                }
                
                return this;
            """.trimIndent(), desc.handlerTypeName
            ).build()
        pipelineClassBuilder.addMethod(addLastListMethod)


        val headContextClass = TypeSpec.classBuilder("HeadContext")
            .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
            .addTypeVariables(desc.entityDeclaredTypeVariables)
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
                    .addModifiers(Modifier.PROTECTED)
                    .returns(desc.handlerTypeName)
                    .addStatement("return null")
                    .build()
            )
            .addMethods(genPipelineOverrideMethods {
                when (TypeName.get(it.returnType)) {
                    TypeName.VOID -> """next.${it.methodName}(${it.params.joinToString(", ") { it.simpleName }});"""
                    else -> """return next.${it.methodName}(${it.params.joinToString(", ") { it.simpleName }});"""
                }
            })
            .build()
        pipelineClassBuilder.addType(headContextClass)


        val tailContextClass = TypeSpec.classBuilder("TailContext")
            .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
            .addTypeVariables(desc.entityDeclaredTypeVariables)
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
                    .addModifiers(Modifier.PROTECTED)
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
                        TypeName.VOID -> ""
                        TypeName.BOOLEAN -> "return false;"
                        TypeName.BYTE, TypeName.SHORT, TypeName.INT, TypeName.LONG, TypeName.FLOAT, TypeName.DOUBLE -> "return 0;"
                        TypeName.CHAR -> "return '0';"
                        else -> ""
                    }
                }
            })
            .build()
        pipelineClassBuilder.addType(tailContextClass)


        JavaFile.builder(desc.pipelineRawClassName.packageName(), pipelineClassBuilder.build())
            .skipJavaLangImports(true)
            .build()
            .writeTo(filer)
    }

    private fun genPipelineOverrideMethods(statement: (AutoPipelineOperatorsDescriptor) -> String): List<MethodSpec> =
        desc.entityOperations.map {
            MethodSpec.methodBuilder(it.methodName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override::class.java)
                .addParameters(it.params.map { param -> ParameterSpec.get(param) })
                .returns(TypeName.get(it.returnType))
                .addCode(statement.invoke(it))
                .build()
        }
}