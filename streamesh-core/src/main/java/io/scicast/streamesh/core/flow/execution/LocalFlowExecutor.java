package io.scicast.streamesh.core.flow.execution;

import io.scicast.streamesh.core.flow.FlowInstance;
import io.scicast.streamesh.core.StreameshContext;
import io.scicast.streamesh.core.flow.FlowDefinition;
import io.scicast.streamesh.core.flow.FlowGraph;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.function.Consumer;

@RequiredArgsConstructor
@Getter
public class LocalFlowExecutor implements FlowExecutor {

    private final StreameshContext context;

    @Override
    public FlowInstance execute(FlowDefinition flow, Map<?, ?> input, Consumer<FlowExecutionEvent<?>> eventHandler) {
        FlowGraph graph = flow.getGraph().clone();



        return null;
    }

}
