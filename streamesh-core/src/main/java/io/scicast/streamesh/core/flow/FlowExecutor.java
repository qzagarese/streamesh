package io.scicast.streamesh.core.flow;

import io.scicast.streamesh.core.FlowInstance;
import io.scicast.streamesh.core.StreameshContext;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class FlowExecutor {

    private final FlowDefinition flow;
    private final StreameshContext context;

    public FlowInstance execute() {
        FlowGraph graph = flow.getGraph().clone();
        
        return null;
    }

}
