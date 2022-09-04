package com.foldright.examples.lb.pipeline

import com.foldright.examples.lb.LoadBalancer
import com.foldright.examples.lb.Node
import com.foldright.examples.lb.handler.AvailableLoadBalancerHandler
import com.foldright.examples.lb.handler.RandomLoadBalancerHandler
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe

class LoadBalancerTest : AnnotationSpec() {

    class DefaultNode(val id: String, val weight: Int, val available: Boolean) : Node {
        override fun id(): String = id
        override fun weight(): Int = weight
        override fun available(): Boolean = available
    }

    lateinit var pipeline: LoadBalancer

    @Test
    fun testDefaultHandler() {
        pipeline = LoadBalancerPipeline()
            .addLast(AvailableLoadBalancerHandler())
            .addLast(RandomLoadBalancerHandler())

        val nodes = listOf(
            DefaultNode("1", 1, false),
            DefaultNode("2", 2, true),
            DefaultNode("3", 3, false),
        )


        val node = pipeline.choose(nodes)

        node.id shouldBe "2"
        node.weight shouldBe 2
        node.available shouldBe true
    }
}
