package io.scicast.streamesh.core.flow.execution;

import io.scicast.streamesh.core.TaskOutput;
import io.scicast.streamesh.core.flow.FlowGraph;

import java.util.Set;
import java.util.stream.Collectors;

public class TaskOutputRuntimeNode extends ExecutionOutputRuntimeNode {

    public TaskOutputRuntimeNode(FlowGraph.FlowNode flowNode) {
        super(flowNode);
        this.outputName = ((TaskOutput) flowNode.getValue()).getName();
    }
}
