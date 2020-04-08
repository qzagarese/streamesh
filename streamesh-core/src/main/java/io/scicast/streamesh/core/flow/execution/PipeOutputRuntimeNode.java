package io.scicast.streamesh.core.flow.execution;

import io.scicast.streamesh.core.flow.FlowGraph;

public class PipeOutputRuntimeNode extends RuntimeNode {
    public PipeOutputRuntimeNode(FlowGraph.FlowNode flowNode) {
        this.name = flowNode.getName();
    }

    @Override
    public boolean canExecute() {
        return false;
    }

    @Override
    public void notify(RuntimeNode node) {
        value = node.getValue();
        notifyObservers();
    }
}
