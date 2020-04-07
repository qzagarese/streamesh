package io.scicast.streamesh.core.flow.execution;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ExecutionGraph {


    Set<RuntimeNode> nodes = new HashSet<>();


    public void addNode(RuntimeNode node) {
        nodes.add(node);
    }

    public Set<RuntimeNode> getExecutableNodes() {
        return nodes.stream()
                .filter(n -> n.canExecute())
                .collect(Collectors.toSet());
    }

}
