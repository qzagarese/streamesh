package io.scicast.streamesh.core.flow;

import io.scicast.streamesh.core.StreameshStore;

import java.util.Optional;

public class FlowGraphBuilder {

    private final StreameshStore store;

    public FlowGraphBuilder(StreameshStore store) {
        this.store = store;
    }

    public FlowGraph build(FlowDefinition definition) {
        FlowGraph graph = new FlowGraph();
        definition.getOutput().forEach(output -> {
            graph.createNode(output.getName(), false);
        });
        definition.getPipes().forEach(pipe -> {
            Optional.of(store.getDefinitionByName(pipe.getType()))
                    .orElseThrow(() -> new IllegalArgumentException("Cannot find any component of type " + pipe.getType()));

            graph.createNode(pipe.getAs(), true);
        });
        definition.getInput().forEach(input -> {
            graph.createNode(input.getName(), false);
        });

        return graph;
    }

}
