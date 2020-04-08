package io.scicast.streamesh.core.flow.execution;

import io.scicast.streamesh.core.flow.FlowGraph;
import io.scicast.streamesh.core.flow.FlowParameterRef;

public class FlowReferenceRuntimeNode extends ExecutablePipeRuntimeNode {
    public FlowReferenceRuntimeNode(FlowGraph.FlowNode flowNode) {
        super(flowNode);
        flowNode.getIncomingLinks().stream().map(edge -> edge.getSource()).forEach(node -> {
            upstreamNodeToParameterSpec.put(node.getName(), ((FlowParameterRef) node.getValue()).getName());
        });
        value = RuntimeDataValue.builder().build();
    }
}
