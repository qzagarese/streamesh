package io.scicast.streamesh.core.flow.execution;

import io.scicast.streamesh.core.flow.FlowGraph;
import io.scicast.streamesh.core.flow.FlowOutput;
import lombok.Getter;
import lombok.Setter;

public class FlowOutputRuntimeNode extends OutputRuntimeNode {

    @Getter
    @Setter
    private boolean outputAlreadyConsumed;

    public FlowOutputRuntimeNode(FlowGraph.FlowNode flowNode) {
        this.name = flowNode.getName();
        this.outputName = ((FlowOutput) flowNode.getValue()).getName();
        this.staticGraphNode = flowNode;
    }
}
