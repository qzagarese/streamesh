package io.scicast.streamesh.core.flow.execution;

import io.scicast.streamesh.core.MicroPipe;
import io.scicast.streamesh.core.TaskParameter;
import io.scicast.streamesh.core.flow.FlowGraph;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.stream.Collectors;

public class MicroPipeRuntimeNode extends ExecutablePipeRuntimeNode {

    @Getter
    @Setter
    private String taskId;

    public MicroPipeRuntimeNode(FlowGraph.FlowNode flowNode) {
        super(flowNode);
        flowNode.getIncomingLinks().stream().map(edge -> edge.getSource()).forEach(node -> {
            upstreamNodeToParameterSpec.put(node.getName(), ((TaskParameter) node.getValue()).getName());
        });
        value = RuntimeDataValue.builder().build();
    }

}
