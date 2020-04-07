package io.scicast.streamesh.core.flow.execution;

import io.scicast.streamesh.core.MicroPipe;
import io.scicast.streamesh.core.flow.FlowGraph;
import io.scicast.streamesh.core.flow.FlowReference;

public class ExecutionGraphBuilder {


    public ExecutionGraph build(FlowGraph staticGraph) {
        ExecutionGraph graph = new ExecutionGraph();
        RuntimeNodeFactory nodeFactory = new RuntimeNodeFactory();

        staticGraph.getNodes().stream()
                .filter(node -> node.getValue() instanceof MicroPipe || node.getValue() instanceof FlowReference)
                .forEach(node -> {
                    RuntimeNode runtimeNode = nodeFactory.create(node);
                });

        return graph;
    }

}
