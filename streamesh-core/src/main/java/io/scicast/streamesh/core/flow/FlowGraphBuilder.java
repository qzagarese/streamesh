package io.scicast.streamesh.core.flow;

import io.scicast.streamesh.core.internal.reflect.GraphNode;
import io.scicast.streamesh.core.internal.reflect.Scope;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FlowGraphBuilder {

    public FlowGraph build(Scope scope) {
        FlowGraph graph = new FlowGraph();
        buildNodes(scope, new ArrayList<>(), graph);



        return graph;
    }

    private void buildNodes(Scope scope, List<String> path, FlowGraph graph) {
        Scope subScope = scope.subScope(path);
        Object value = subScope != null ? subScope.getValue() : null;
        GraphNode annotation = value != null ? value.getClass().getAnnotation(GraphNode.class) : null;
        if (annotation != null) {
            graph.createNode(path.stream().collect(Collectors.joining(".")), value, annotation.value());
        }
        if (subScope != null) {
            subScope.getStructure().keySet().forEach(entry -> {
                buildNodes(scope, Stream.concat(path.stream(), Stream.of(entry)).collect(Collectors.toList()), graph);
            });
        }
    }

}
