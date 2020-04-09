package io.scicast.streamesh.core.flow.execution;

import io.scicast.streamesh.core.flow.FlowGraph;
import io.scicast.streamesh.core.flow.FlowParameterRef;
import lombok.Getter;
import lombok.Setter;

public class FlowReferenceRuntimeNode extends ExecutablePipeRuntimeNode {

    @Getter
    @Setter
    private String instanceId;

    public FlowReferenceRuntimeNode(FlowGraph.FlowNode flowNode) {
        super(flowNode);
        flowNode.getIncomingLinks().stream().map(edge -> edge.getSource()).forEach(node -> {
            upstreamNodeToParameterSpec.put(node.getName(), ((FlowParameterRef) node.getValue()).getName());
        });
        value = RuntimeDataValue.builder().build();
    }

    @Override
    public boolean canExecute() {
        return super.canExecute() && instanceId == null;
    }
}
