package io.scicast.streamesh.core.internal.reflect.handler;

import io.scicast.streamesh.core.MicroPipe;
import io.scicast.streamesh.core.flow.FlowGraph;
import io.scicast.streamesh.core.internal.reflect.GraphContext;
import io.scicast.streamesh.core.internal.reflect.GraphNode;

import java.util.List;
import java.util.stream.Collectors;

public class MicroPipeGraphNodeHandler implements GraphNodeHandler {
    @Override
    public FlowGraph handle(GraphContext context) {
        FlowGraph graph = context.getGraph();
        MicroPipe target = (MicroPipe) context.getTarget();
        String microPipeNodeName = context.getPath().stream().collect(Collectors.joining("."));
        graph.createNode(microPipeNodeName, context.getTarget(), GraphNode.NodeType.INTERNAL);

        target.getOutputMapping().forEach(output -> {
            List<String> outputPath = context.getScope().getPathByValue(output, context.getPath());
            String taskOutputNodeName = outputPath.stream().collect(Collectors.joining("."));
            graph.createNode(taskOutputNodeName, output, GraphNode.NodeType.INTERNAL);
            graph.connect(microPipeNodeName, taskOutputNodeName);
        });

        target.getInputMapping().getParameters().forEach(input -> {
            List<String> inputPath = context.getScope().getPathByValue(input, context.getPath());
            String taskParameterNodeName = inputPath.stream().collect(Collectors.joining("."));
            graph.createNode(taskParameterNodeName, input, GraphNode.NodeType.INTERNAL);
            graph.connect(taskParameterNodeName, microPipeNodeName);
        });

        return graph;
    }
}
