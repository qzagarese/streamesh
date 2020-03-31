package io.scicast.streamesh.core.internal.reflect.handler;

import io.scicast.streamesh.core.flow.FlowGraph;
import io.scicast.streamesh.core.internal.reflect.GraphContext;

import java.util.stream.Collectors;

public class BaseGraphNodeHandler implements GraphNodeHandler {
    @Override
    public FlowGraph handle(GraphContext context) {
        FlowGraph graph = context.getGraph();
        graph.createNode(context.getPath().stream().collect(Collectors.joining(".")),
                context.getTarget(),
                context.getAnnotation().value());
        return graph;
    }
}
