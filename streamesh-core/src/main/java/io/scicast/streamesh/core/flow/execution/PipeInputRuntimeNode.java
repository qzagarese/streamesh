package io.scicast.streamesh.core.flow.execution;

import io.scicast.streamesh.core.flow.FlowGraph;
import io.scicast.streamesh.core.flow.PipeInput;
import io.scicast.streamesh.core.internal.reflect.ExpressionParser;
import lombok.Getter;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PipeInputRuntimeNode extends RuntimeNode {

    private PipeInput staticNodeValue;

    @Getter
    private boolean staticallyInitialised;

    public PipeInputRuntimeNode(FlowGraph.FlowNode flowNode) {
        this.name = flowNode.getName();
        this.staticNodeValue = (PipeInput) flowNode.getValue();
        this.staticGraphNode = flowNode;
        if (!ExpressionParser.isExpression(staticNodeValue.getValue())) {
            value = RuntimeDataValue.builder()
                    .parts(Stream.of(RuntimeDataValue.RuntimeDataValuePart.builder()
                            .value(staticNodeValue.getValue())
                            .state(RuntimeDataValue.DataState.COMPLETE)
                            .build())
                        .collect(Collectors.toSet()))
                    .build();
            staticallyInitialised = true;
        }
    }

    @Override
    public void notify(RuntimeNode node) {
        mergeValues(node);
        if (shouldPropagate()) {
            notifyObservers();
        }
    }

    private void mergeValues(RuntimeNode node) {
        if (value == null) {
            this.value = node.getValue();
        } else {
            Set<RuntimeDataValue.RuntimeDataValuePart> updatedParts = value.getParts().stream()
                    .map(part -> {
                        if (node.getValue().getParts().contains(part)) {
                            RuntimeDataValue.RuntimeDataValuePart valuePart = node.getValue().getParts().stream()
                                    .filter(p -> p.getRefName().equals(part.getRefName())
                                            && p.getValue().equals(part.getValue()))
                                    .findFirst()
                                    .orElse(null);
                            if (valuePart != null && valuePart.getState().equals(RuntimeDataValue.DataState.COMPLETE)) {
                                return valuePart;
                            }
                        }
                        return part;
                    }).collect(Collectors.toSet());
            updatedParts = Stream.concat(updatedParts.stream(),
                    node.getValue().getParts().stream()
                            .filter(p -> !value.getParts().contains(p))).collect(Collectors.toSet());
            value = RuntimeDataValue.builder()
                    .parts(updatedParts)
                    .build();
        }
    }

    private boolean shouldPropagate() {
        if (staticNodeValue.getUsable().equals(PipeInput.UsabilityState.WHILE_BEING_PRODUCED)) {
            return true;
        } else {
            return value != null && value.getParts().stream()
                    .allMatch(p -> p.getState().equals(RuntimeDataValue.DataState.COMPLETE));
        }
    }
}
