package io.scicast.streamesh.core.internal.reflect;

import io.scicast.streamesh.core.flow.FlowGraph;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class GraphContext {

    private FlowGraph graph;
    private GraphNode annotation;
    private Object target;
    private Scope scope;
    private List<String> path;
}
