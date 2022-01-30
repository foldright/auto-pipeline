package com.foldright.auto.pipeline.processor.generator

import com.foldright.auto.pipeline.processor.AutoPipelineClassDescriptor
import com.foldright.auto.pipeline.processor.AutoPipelineOperatorsDescriptor
import com.foldright.auto.pipeline.processor.AutoPipelineOperatorsDescriptor.Companion.expand
import com.squareup.javapoet.*
import java.util.concurrent.ConcurrentMap
import javax.annotation.processing.Filer
import javax.lang.model.element.Modifier

class PipelineGenerator(private val desc: AutoPipelineClassDescriptor, private val filer: Filer) :
    AbstractGenerator(desc) {
    private val headHandlerContextFieldName = "head"
    private val tailHandlerContextFieldName = "tail"
    private val prevHandlerContextFieldName = "prev"
    private val nextHandlerContextFieldName = "next"
    private val pipelineStateFieldName = "state"

    fun gen() {
        val pipelineClassBuilder = TypeSpec.classBuilder(desc.pipelineRawClassName)
            .addModifiers(Modifier.PUBLIC)
            .addTypeVariables(desc.entityDeclaredTypeVariables)
            .addSuperinterface(desc.entityType)
            .addField(
                desc.abstractHandlerContextTypeName,
                headHandlerContextFieldName,
                Modifier.PRIVATE,
                Modifier.FINAL
            )
            .addField(
                desc.abstractHandlerContextTypeName,
                tailHandlerContextFieldName,
                Modifier.PRIVATE,
                Modifier.FINAL
            ).addField(
                desc.pipelineStateTypeName,
                pipelineStateFieldName,
                Modifier.PRIVATE,
                Modifier.FINAL
            )

        val pipelineConstructor = MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .addCode(
                """
                $pipelineStateFieldName = new PipelineState();

                $headHandlerContextFieldName = new HeadContext(this);
                $tailHandlerContextFieldName = new TailContext(this);

                $headHandlerContextFieldName.$prevHandlerContextFieldName = null;
                $headHandlerContextFieldName.$nextHandlerContextFieldName = $tailHandlerContextFieldName;
                $tailHandlerContextFieldName.$prevHandlerContextFieldName = $headHandlerContextFieldName;
                $tailHandlerContextFieldName.$nextHandlerContextFieldName = null;
            """.trimIndent()
            ).build()
        pipelineClassBuilder.addMethod(pipelineConstructor)
        pipelineClassBuilder.addMethods(genPipelineOverrideMethodsViaDelegate())

        val markLastHandlerContextMethod = MethodSpec.methodBuilder("markLastHandlerContext")
            .addModifiers(Modifier.PROTECTED)
            .addParameter(desc.handlerContextTypeName, "handlerContext")
            .returns(TypeName.VOID)
            .addCode(
                """
                state.put("_lastHandler", handlerContext);
            """.trimIndent()
            ).build()
        pipelineClassBuilder.addMethod(markLastHandlerContextMethod)

        val lastHandlerContextMethod = MethodSpec.methodBuilder("lastHandlerContext")
            .addModifiers(Modifier.PROTECTED)
            .returns(desc.handlerContextTypeName)
            .addCode(
                """
                return (${'$'}T) state.get("_lastHandler");
            """.trimIndent(),
                desc.handlerContextTypeName
            ).build()
        pipelineClassBuilder.addMethod(lastHandlerContextMethod)

        val addFirstMethod = MethodSpec.methodBuilder("addFirst")
            .addModifiers(Modifier.PUBLIC, Modifier.SYNCHRONIZED)
            .addParameter(desc.handlerTypeName, "handler")
            .returns(desc.pipelineTypeName)
            .addCode(
                """
                ${'$'}T newCtx = new ${'$'}T(this, handler);
                ${'$'}T nextCtx = $headHandlerContextFieldName.$nextHandlerContextFieldName;
                $headHandlerContextFieldName.$nextHandlerContextFieldName = newCtx;
                newCtx.$prevHandlerContextFieldName = $headHandlerContextFieldName;
                newCtx.$nextHandlerContextFieldName = nextCtx;
                nextCtx.$prevHandlerContextFieldName = newCtx;

                return this;
            """.trimIndent(),
                desc.abstractHandlerContextTypeName,
                desc.defaultHandlerContextTypeName,
                desc.abstractHandlerContextTypeName
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
                ${'$'}T newCtx = new ${'$'}T(this, handler);
                ${'$'}T prevCtx = $tailHandlerContextFieldName.$prevHandlerContextFieldName;

                newCtx.$prevHandlerContextFieldName = prevCtx;
                newCtx.$nextHandlerContextFieldName = $tailHandlerContextFieldName;
                prevCtx.$nextHandlerContextFieldName = newCtx;
                $tailHandlerContextFieldName.$prevHandlerContextFieldName = newCtx;

                return this;
            """.trimIndent(),
                desc.abstractHandlerContextTypeName,
                desc.defaultHandlerContextTypeName,
                desc.abstractHandlerContextTypeName
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
            .addMethods(genEndPointContextOverrideMethods(true))
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
            .addMethods(genEndPointContextOverrideMethods(false))
            .build()
        pipelineClassBuilder.addType(tailContextClass)

        val attributeField =
            FieldSpec.builder(
                ParameterizedTypeName.get(
                    ConcurrentMap::class.java,
                    String::class.java,
                    Object::class.java
                ), "attributes", Modifier.PRIVATE, Modifier.FINAL
            )
                .initializer("new java.util.concurrent.ConcurrentHashMap<>()")
                .build()
        val pipelineStateClass = TypeSpec.classBuilder(desc.pipelineStateTypeNameRawClassName.simpleName())
            .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
            .addField(attributeField)
            .addMethod(
                MethodSpec.methodBuilder("put")
                    .addModifiers(Modifier.PROTECTED)
                    .addModifiers(Modifier.SYNCHRONIZED)
                    .addParameter(String::class.java, "key", Modifier.FINAL)
                    .addParameter(Object::class.java, "value", Modifier.FINAL)
                    .returns(TypeName.OBJECT)
                    .addStatement("return attributes.put(key, value)")
                    .build()
            ).addMethod(
                MethodSpec.methodBuilder("remove")
                    .addModifiers(Modifier.PROTECTED)
                    .addModifiers(Modifier.SYNCHRONIZED)
                    .addParameter(String::class.java, "key", Modifier.FINAL)
                    .returns(TypeName.OBJECT)
                    .addStatement("return attributes.remove(key)")
                    .build()
            ).addMethod(
                MethodSpec.methodBuilder("get")
                    .addModifiers(Modifier.PROTECTED)
                    .addParameter(String::class.java, "key", Modifier.FINAL)
                    .returns(TypeName.OBJECT)
                    .addStatement("return attributes.get(key)")
                    .build()
            ).build()
        pipelineClassBuilder.addType(pipelineStateClass)


        javaFileBuilder(desc.pipelineRawClassName.packageName(), pipelineClassBuilder.build())
            .build()
            .writeTo(filer)
    }

    /**
     * generate Pipeline Override Methods for Head Node or Tail Node .
     */
    private fun genEndPointContextOverrideMethods(headNode: Boolean): List<MethodSpec> =
        genPipelineOverrideMethods {
            val reversePipe = it.reversePipe()
            if (reversePipe) {
                if (headNode) {
                    genMarkLastHandlerContextStatement() + genContextOverrideMethodsStatementViaReturnType(it)
                } else {
                    genMarkLastHandlerContextStatement() + genContextOverrideMethodsStatementViaDelegate(it, prevHandlerContextFieldName)
                }
            } else {
                if (headNode) {
                    genMarkLastHandlerContextStatement() + genContextOverrideMethodsStatementViaDelegate(it, nextHandlerContextFieldName)
                } else {
                    genMarkLastHandlerContextStatement() + genContextOverrideMethodsStatementViaReturnType(it)
                }
            }
        }

    private fun genContextOverrideMethodsStatementViaReturnType(operatorDesc: AutoPipelineOperatorsDescriptor): String {
        val typeName = TypeName.get(operatorDesc.returnType)
        return when {
            TypeName.VOID.equals(typeName)-> "//noop"
            !typeName.isPrimitive -> "return null;"
            else -> when (typeName) {
                // TODO: Generating the default value is not good,
                //       since there is a assumption that user want the "tail" behavior.
                TypeName.BOOLEAN -> "return false;"
                TypeName.BYTE, TypeName.SHORT, TypeName.INT, TypeName.LONG, TypeName.FLOAT, TypeName.DOUBLE -> "return 0;"
                TypeName.CHAR -> "return '0';"
                else -> "//no-op"
            }
        }
    }

    private fun genContextOverrideMethodsStatementViaDelegate(
        operatorDesc: AutoPipelineOperatorsDescriptor, delegate: String
    ): String {
        return when (TypeName.get(operatorDesc.returnType)) {
            TypeName.VOID -> "${delegate}.${operatorDesc.methodName}(${operatorDesc.params.expand()});"
            else -> "return ${delegate}.${operatorDesc.methodName}(${operatorDesc.params.expand()});"
        }
    }

    private fun genPipelineOverrideMethodsViaDelegate(): List<MethodSpec> {
        return genPipelineOverrideMethods {
            val delegateStatement = if (it.reversePipe())
                """
                    final ${desc.handlerContextTypeName} lastHandlerContext = lastHandlerContext();
                    final ${desc.handlerContextTypeName} delegate = lastHandlerContext == null ? $tailHandlerContextFieldName : lastHandlerContext;
                """.trimIndent()
            else
                """
                   final ${desc.handlerContextTypeName} delegate = $headHandlerContextFieldName;
                """.trimIndent()

            val returnStatement = when (TypeName.get(it.returnType)) {
                TypeName.VOID -> "delegate.${it.methodName}(${it.params.expand()});"
                else -> "return delegate.${it.methodName}(${it.params.expand()});"
            }

            delegateStatement + "\n" + returnStatement
        }
    }

}
