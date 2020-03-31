package io.scicast.streamesh.core.internal.reflect.handler;

import io.scicast.streamesh.core.flow.FlowGraph;
import io.scicast.streamesh.core.internal.reflect.GraphContext;

public interface GraphNodeHandler {

    FlowGraph handle(GraphContext context);

}
