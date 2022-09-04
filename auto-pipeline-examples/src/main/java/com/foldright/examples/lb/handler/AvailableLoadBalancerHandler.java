package com.foldright.examples.lb.handler;

import com.foldright.examples.lb.Node;
import com.foldright.examples.lb.pipeline.LoadBalancerHandler;
import com.foldright.examples.lb.pipeline.LoadBalancerHandlerContext;

import java.util.List;
import java.util.stream.Collectors;

public class AvailableLoadBalancerHandler implements LoadBalancerHandler {

    @Override
    public <T extends Node> T choose(List<T> nodes, LoadBalancerHandlerContext loadBalancerHandlerContext) {
        List<T> availableNodes = nodes.stream().filter(Node::available).collect(Collectors.toList());
        return loadBalancerHandlerContext.choose(availableNodes);
    }
}
