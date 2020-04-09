package io.scicast.streamesh.core.flow.execution;

import io.scicast.streamesh.core.flow.FlowGraph;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ExecutionGraph {


    private Set<RuntimeNode> nodes = new HashSet<>();
    private RuntimeNodeFactory factory = new RuntimeNodeFactory();

    public ExecutionGraph(FlowGraph staticGraph) {
        staticGraph.getNodes().forEach(flowNode -> {
            nodes.add(factory.create(flowNode));
        });

        staticGraph.getNodes().forEach(node -> {
            RuntimeNode runtimeNode = getNode(node.getName());
            node.getOutgoingLinks().forEach(edge -> {
                runtimeNode.addObserver(getNode(edge.getDestination().getName()));
            });
        });

    }

    public RuntimeNode getNode(String name) {
        return nodes.stream()
                .filter(node -> node.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    public void addNode(RuntimeNode node) {
        nodes.add(node);
    }

    public Set<ExecutablePipeRuntimeNode> getExecutableNodes() {
        return nodes.stream()
                .filter(node -> node instanceof ExecutablePipeRuntimeNode)
                .map(node -> (ExecutablePipeRuntimeNode) node)
                .filter(node -> node.canExecute())
                .collect(Collectors.toSet());
    }

    public Set<FlowParameterRuntimeNode> getInputNodes() {
        return nodes.stream()
                .filter(node -> node instanceof FlowParameterRuntimeNode)
                .map(node -> (FlowParameterRuntimeNode) node)
                .collect(Collectors.toSet());
    }

    public Set<FlowOutputRuntimeNode> getOutputNodes() {
        return nodes.stream()
                .filter(node -> node instanceof FlowOutputRuntimeNode)
                .map(node -> (FlowOutputRuntimeNode) node)
                .collect(Collectors.toSet());
    }

    public Set<PipeInputRuntimeNode> getPipeInputNodes() {
        return nodes.stream()
                .filter(node -> node instanceof PipeInputRuntimeNode)
                .map(node -> (PipeInputRuntimeNode) node)
                .collect(Collectors.toSet());
    }

}
