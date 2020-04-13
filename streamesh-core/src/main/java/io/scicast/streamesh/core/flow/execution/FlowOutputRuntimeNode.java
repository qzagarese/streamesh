package io.scicast.streamesh.core.flow.execution;

import io.scicast.streamesh.core.flow.FlowGraph;
import io.scicast.streamesh.core.flow.FlowOutput;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.stream.Collectors;

public class FlowOutputRuntimeNode extends RuntimeNode {

    private final String outputName;

    @Getter
    @Setter
    private boolean outputAlreadyConsumed;

    public FlowOutputRuntimeNode(FlowGraph.FlowNode flowNode) {
        this.name = flowNode.getName();
        this.outputName = ((FlowOutput) flowNode.getValue()).getName();
        this.staticGraphNode = flowNode;
    }

    @Override
    public void notify(RuntimeNode node) {
        Set<RuntimeDataValue.RuntimeDataValuePart> parts = node.getValue().getParts().stream()
                .map(p -> RuntimeDataValue.RuntimeDataValuePart.builder()
                            .value(p.getValue())
                            .state(p.getState())
                            .refName(outputName)
                            .build())
                .collect(Collectors.toSet());
        if (!parts.isEmpty()) {
            value = RuntimeDataValue.builder()
                    .parts(parts)
                    .build();
            notifyObservers();
        }
    }
}
