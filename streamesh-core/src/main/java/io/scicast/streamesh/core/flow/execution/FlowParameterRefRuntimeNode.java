package io.scicast.streamesh.core.flow.execution;

import io.scicast.streamesh.core.flow.FlowGraph;

public class FlowParameterRefRuntimeNode extends TaskParameterRuntimeNode {

    public FlowParameterRefRuntimeNode(FlowGraph.FlowNode flowNode) {
        super(flowNode);
    }

}
