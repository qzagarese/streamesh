package io.scicast.streamesh.core.flow.execution;

import io.scicast.streamesh.core.MicroPipe;
import io.scicast.streamesh.core.flow.FlowGraph;
import io.scicast.streamesh.core.flow.FlowParameterRef;
import io.scicast.streamesh.core.flow.FlowReference;
import lombok.Getter;
import lombok.Setter;

public class FlowReferenceRuntimeNode extends ExecutablePipeRuntimeNode {

    @Getter
    @Setter
    private String instanceId;

    public FlowReferenceRuntimeNode(FlowGraph.FlowNode flowNode) {
        super(flowNode);
        definitionId = ((FlowReference) flowNode.getValue()).getDefinition().getId();
        flowNode.getIncomingLinks().stream().map(edge -> edge.getSource()).forEach(node -> {
            upstreamNodeToParameterSpec.put(node.getName(), ((FlowParameterRef) node.getValue()).getName());
        });
        value = RuntimeDataValue.builder().build();
    }

}
