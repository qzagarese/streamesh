package io.scicast.streamesh.core.flow.execution;

import io.scicast.streamesh.core.flow.FlowGraph;

public class TaskOutputRuntimeNode extends RuntimeNode {
    public TaskOutputRuntimeNode(FlowGraph.FlowNode flowNode) {

    }

    @Override
    public boolean canExecute() {
        return false;
    }

    @Override
    public void notify(RuntimeNode node) {

    }
}
