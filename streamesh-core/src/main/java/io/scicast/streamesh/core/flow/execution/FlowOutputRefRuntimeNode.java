package io.scicast.streamesh.core.flow.execution;

import io.scicast.streamesh.core.flow.FlowGraph;
import io.scicast.streamesh.core.flow.FlowOutputRef;

public class FlowOutputRefRuntimeNode extends ExecutionOutputRuntimeNode {
    public FlowOutputRefRuntimeNode(FlowGraph.FlowNode flowNode) {
        super(flowNode);
        this.outputName = ((FlowOutputRef) flowNode.getValue()).getName();
    }

}
