package io.scicast.streamesh.core.internal.reflect.handler;

import io.scicast.streamesh.core.flow.FlowGraph;
import io.scicast.streamesh.core.internal.reflect.GraphContext;

public class MicropipeGraphNodeHandler implements GraphNodeHandler {
    @Override
    public FlowGraph handle(GraphContext context) {
        return context.getGraph();
    }
}
