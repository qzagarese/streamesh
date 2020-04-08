package io.scicast.streamesh.core.flow.execution;

import io.scicast.streamesh.core.flow.FlowGraph;
import io.scicast.streamesh.core.flow.FlowOutputRef;

public class FlowOutputRuntimeNode extends OutputRuntimeNode {

    public FlowOutputRuntimeNode(FlowGraph.FlowNode flowNode) {
        this.name = flowNode.getName();
        this.outputName = ((FlowOutputRef) flowNode.getValue()).getName();
    }
}
