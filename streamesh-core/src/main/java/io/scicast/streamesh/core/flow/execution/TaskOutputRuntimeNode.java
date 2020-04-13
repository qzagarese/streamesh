package io.scicast.streamesh.core.flow.execution;

import io.scicast.streamesh.core.TaskOutput;
import io.scicast.streamesh.core.flow.FlowGraph;

import java.util.Set;
import java.util.stream.Collectors;

public class TaskOutputRuntimeNode extends RuntimeNode {

    private String outputName;

    public TaskOutputRuntimeNode(FlowGraph.FlowNode flowNode) {
        this.name = flowNode.getName();
        this.outputName = ((TaskOutput) flowNode.getValue()).getName();
        this.staticGraphNode = flowNode;
    }

    @Override
    public void notify(RuntimeNode node) {
        Set<RuntimeDataValue.RuntimeDataValuePart> parts = node.getValue().getParts().stream()
                .filter(p -> p.getRefName() != null && p.getRefName().equals(outputName))
                .collect(Collectors.toSet());
        if (!parts.isEmpty()) {
            value = RuntimeDataValue.builder()
                    .parts(parts)
                    .build();
            notifyObservers();
        }

    }

}
