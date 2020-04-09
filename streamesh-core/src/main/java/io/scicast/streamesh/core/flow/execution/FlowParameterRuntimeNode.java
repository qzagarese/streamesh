package io.scicast.streamesh.core.flow.execution;

import io.scicast.streamesh.core.flow.FlowGraph;
import io.scicast.streamesh.core.flow.FlowParameter;

public class FlowParameterRuntimeNode extends UpdatableRuntimeNode {

    private FlowParameter staticNodeValue;

    public FlowParameterRuntimeNode(FlowGraph.FlowNode flowNode) {
        this.name = flowNode.getName();
        this.staticGraphNode = flowNode;
        this.staticNodeValue = (FlowParameter) flowNode.getValue();
    }

    @Override
    public void notify(RuntimeNode node) {
        update(node.getValue());
    }

    @Override
    public void update(RuntimeDataValue value) {
        if (!value.equals(this.value)) {
            this.value = value;
            notifyObservers();
        }
    }
}
