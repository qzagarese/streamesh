package io.scicast.streamesh.core.flow.execution;

import io.scicast.streamesh.core.TaskOutput;
import io.scicast.streamesh.core.flow.FlowGraph;

public class TaskOutputRuntimeNode extends OutputRuntimeNode {

    public TaskOutputRuntimeNode(FlowGraph.FlowNode flowNode) {
        this.name = flowNode.getName();
        this.outputName = ((TaskOutput) flowNode.getValue()).getName();
        this.staticGraphNode = flowNode;
    }

}
