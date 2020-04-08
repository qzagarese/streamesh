package io.scicast.streamesh.core.flow.execution;

import io.scicast.streamesh.core.flow.FlowGraph;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TaskParameterRuntimeNode extends RuntimeNode {

    protected Set<String> expectedNotificationsSubjects;

    public TaskParameterRuntimeNode(FlowGraph.FlowNode flowNode) {
        this.name = flowNode.getName();
        expectedNotificationsSubjects = flowNode.getIncomingLinks().stream()
                .map(edge -> edge.getSource().getName())
                .collect(Collectors.toSet());
        value = RuntimeDataValue.builder().build();
    }

    @Override
    public boolean canExecute() {
        return false;
    }

    @Override
    public void notify(RuntimeNode node) {
        value = RuntimeDataValue.builder()
                .parts(Stream.concat(
                        node.getValue().getParts().stream(),
                        value.getParts().stream())
                    .collect(Collectors.toSet()))
                .build();
        expectedNotificationsSubjects.remove(node.getName());
        if (expectedNotificationsSubjects.isEmpty()) {
            notifyObservers();
        }
    }
}
