package io.scicast.streamesh.core.flow.execution;

import io.scicast.streamesh.core.flow.FlowGraph;
import io.scicast.streamesh.core.flow.PipeInput;
import io.scicast.streamesh.core.internal.reflect.ExpressionParser;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PipeInputRuntimeNode extends RuntimeNode {

    private PipeInput staticNodeValue;

    public PipeInputRuntimeNode(FlowGraph.FlowNode flowNode) {
        this.name = flowNode.getName();
        this.staticNodeValue = (PipeInput) flowNode.getValue();
        if (!ExpressionParser.isExpression(staticNodeValue.getValue())) {
            value = RuntimeDataValue.builder()
                    .parts(Stream.of(RuntimeDataValue.RuntimeDataValuePart.builder()
                            .value(staticNodeValue.getValue())
                            .state(RuntimeDataValue.DataState.COMPLETE)
                            .build())
                        .collect(Collectors.toSet()))
                    .build();
        }
    }

    @Override
    public boolean canExecute() {
        return false;
    }

    @Override
    public void notify(RuntimeNode node) {
        if (canPropagate(node)) {
            update(node.getValue());
        }
    }

    private boolean canPropagate(RuntimeNode node) {
        if (staticNodeValue.getUsable().equals(PipeInput.UsabilityState.WHILE_BEING_PRODUCED)) {
            return true;
        } else {
            return node.getValue().getParts().stream().allMatch(p -> p.getState().equals(RuntimeDataValue.DataState.COMPLETE));
        }
    }
}
