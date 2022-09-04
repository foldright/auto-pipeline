package com.foldright.examples.lb.handler;

import com.foldright.examples.lb.Node;
import com.foldright.examples.lb.pipeline.LoadBalancerHandler;
import com.foldright.examples.lb.pipeline.LoadBalancerHandlerContext;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomLoadBalancerHandler implements LoadBalancerHandler {

    @Override
    public <T extends Node> T choose(List<T> nodes, LoadBalancerHandlerContext loadBalancerHandlerContext) {
        int randomIndex = ThreadLocalRandom.current().nextInt(nodes.size());
        return nodes.get(randomIndex);
    }
}
