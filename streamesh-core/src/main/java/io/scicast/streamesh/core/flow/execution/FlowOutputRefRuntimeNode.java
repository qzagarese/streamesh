package io.scicast.streamesh.core.flow.execution;

import io.scicast.streamesh.core.flow.FlowGraph;

public class FlowOutputRefRuntimeNode extends RuntimeNode {
    public FlowOutputRefRuntimeNode(FlowGraph.FlowNode flowNode) {
        this.name = flowNode.getName();
        this.staticGraphNode = flowNode;
    }

    @Override
    public void notify(RuntimeNode node) {

    }
}
