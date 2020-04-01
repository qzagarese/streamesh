package io.scicast.streamesh.core.internal.reflect.handler;

import io.scicast.streamesh.core.MicroPipe;
import io.scicast.streamesh.core.flow.FlowGraph;
import io.scicast.streamesh.core.flow.FlowReference;
import io.scicast.streamesh.core.internal.reflect.GraphContext;
import io.scicast.streamesh.core.internal.reflect.GraphNode;

import java.util.List;
import java.util.stream.Collectors;

public class FlowRefGraphNodeHandler implements GraphNodeHandler {
    @Override
    public FlowGraph handle(GraphContext context) {
        FlowGraph graph = context.getGraph();
        FlowReference target = (FlowReference) context.getTarget();
        String flowRefNodeName = context.getPath().stream().collect(Collectors.joining("."));
        graph.createNode(flowRefNodeName, context.getTarget(), GraphNode.NodeType.INTERNAL);

        target.getOutput().forEach(output -> {
            List<String> outputPath = context.getScope().getPathByValue(output, context.getPath());
            String flowOutputRefNodeName = outputPath.stream().collect(Collectors.joining("."));
            graph.createNode(flowOutputRefNodeName, output, GraphNode.NodeType.INTERNAL);
            graph.connect(flowRefNodeName, flowOutputRefNodeName);
        });

        target.getInput().forEach(input -> {
            List<String> inputPath = context.getScope().getPathByValue(input, context.getPath());
            String flowParameterNodeName = inputPath.stream().collect(Collectors.joining("."));
            graph.createNode(flowParameterNodeName, input, GraphNode.NodeType.INTERNAL);
            graph.connect(flowParameterNodeName, flowRefNodeName);
        });

        return graph;

    }
}
