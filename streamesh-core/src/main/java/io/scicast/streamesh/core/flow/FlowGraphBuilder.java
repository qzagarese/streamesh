package io.scicast.streamesh.core.flow;

import io.scicast.streamesh.core.FlowDefinition;
import io.scicast.streamesh.core.StreameshStore;

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
            graph.createNode(pipe.getAs(), true);
        });
        definition.getInput().forEach(input -> {
            graph.createNode(input.getName(), false);
        });

        return null;
    }

}
