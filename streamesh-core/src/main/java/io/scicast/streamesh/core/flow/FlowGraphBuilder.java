package io.scicast.streamesh.core.flow;

import com.fasterxml.jackson.databind.deser.impl.PropertyBasedCreator;
import io.scicast.streamesh.core.internal.reflect.*;
import io.scicast.streamesh.core.internal.reflect.handler.GraphNodeHandler;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FlowGraphBuilder {

    private Map<Class<? extends GraphNodeHandler>, GraphNodeHandler> handlers = new HashMap<>();

    public FlowGraph build(Scope scope) {
        FlowGraph graph = new FlowGraph();
        graph = handleNodeMarkers(scope, new ArrayList<>(), graph);



        return graph;
    }

    private FlowGraph handleNodeMarkers(Scope scope, List<String> path, FlowGraph graph) {
        Scope subScope = scope.subScope(path);
        Object value = subScope != null ? subScope.getValue() : null;
        GraphNode annotation = value != null ? value.getClass().getAnnotation(GraphNode.class) : null;
        AtomicReference<FlowGraph> graphRef = new AtomicReference<>(graph);

        if (annotation != null) {
            GraphContext context = GraphContext.builder()
                    .annotation(annotation)
                    .scope(scope)
                    .target(value)
                    .graph(graphRef.get())
                    .path(path)
                    .build();
            graphRef.set(getHandler(annotation.handler()).handle(context));
        }
        if (subScope != null) {
            subScope.getStructure().keySet().forEach(entry -> {
                graphRef.set(
                        handleNodeMarkers(scope,
                                Stream.concat(path.stream(), Stream.of(entry)).collect(Collectors.toList()),
                                graphRef.get()));
            });
        }
        return graphRef.get();
    }

    private GraphNodeHandler getHandler(Class<? extends GraphNodeHandler> type) {
        GraphNodeHandler handler = handlers.get(type);
        if (handler == null) {
            handler = ReflectionUtils.instantiateGraphNodeHandler(type);
            handlers.put(type, handler);
        }
        return handler;
    }

}
