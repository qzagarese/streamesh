package io.scicast.streamesh.core.flow.execution;

import io.scicast.streamesh.core.MicroPipe;
import io.scicast.streamesh.core.TaskOutput;
import io.scicast.streamesh.core.TaskParameter;
import io.scicast.streamesh.core.flow.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class RuntimeNodeFactory {

    private Map<Class<?>, Function<FlowGraph.FlowNode, RuntimeNode>> internalFactories = new HashMap<>();

    public RuntimeNodeFactory() {
        internalFactories.put(FlowParameter.class, FlowParameterRuntimeNode::new);
        internalFactories.put(FlowOutput.class, FlowOutputRuntimeNode::new);
        internalFactories.put(PipeInput.class, PipeInputRuntimeNode::new);
        internalFactories.put(PipeOutput.class, PipeOutputRuntimeNode::new);
        internalFactories.put(MicroPipe.class, MicroPipeRuntimeNode::new);
        internalFactories.put(TaskParameter.class, TaskParameterRuntimeNode::new);
        internalFactories.put(TaskOutput.class, TaskOutputRuntimeNode::new);
        internalFactories.put(FlowReference.class, FlowReferenceRuntimeNode::new);
        internalFactories.put(FlowParameterRef.class, FlowParameterRefRuntimeNode::new);
        internalFactories.put(FlowOutputRef.class, FlowOutputRefRuntimeNode::new);
    }


    public RuntimeNode create(FlowGraph.FlowNode node) {
        return internalFactories.entrySet().stream()
            .filter(entry -> entry.getKey().equals(node.getValue().getClass()))
            .map(entry -> entry.getValue())
            .findFirst()
            .map(f -> f.apply(node))
            .orElseThrow(() -> new UnsupportedOperationException("Cannot build runtime node for flow nodes of type "
                    + node.getValue().getClass().getName()));
    }



}
