package io.scicast.streamesh.core.flow.execution;

import io.scicast.streamesh.core.MicroPipe;
import io.scicast.streamesh.core.flow.FlowGraph;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class ExecutablePipeRuntimeNode extends UpdatableRuntimeNode {

    @Getter
    protected Map<String, Object> pipeInput = new HashMap<>();

    @Getter
    protected String definitionId;

    protected Set<String> expectedNotificationsSubjects;
    protected Map<String, String> upstreamNodeToParameterSpec = new HashMap<>();


    public ExecutablePipeRuntimeNode(FlowGraph.FlowNode flowNode) {
        this.name = flowNode.getName();
        this.staticGraphNode = flowNode;
        definitionId = ((MicroPipe) flowNode.getValue()).getId();
        expectedNotificationsSubjects = flowNode.getIncomingLinks().stream()
                .map(flowEdge -> flowEdge.getSource())
                .map(node -> node.getName())
                .collect(Collectors.toSet());
    }

    public boolean canExecute() {
        return expectedNotificationsSubjects.isEmpty();
    }

    @Override
    public void notify(RuntimeNode node) {
        String parameterName = upstreamNodeToParameterSpec.get(node.getName());
        List<String> values = node.getValue().getParts().stream()
                .map(part -> part.getValue())
                .collect(Collectors.toList());
        pipeInput.put(parameterName, values.size() > 1 ? values : values.get(0));
        expectedNotificationsSubjects.remove(node.getName());
    }

    @Override
    public void update(RuntimeDataValue value) {
        Set<RuntimeDataValue.RuntimeDataValuePart> toBeUpdated = value.getParts().stream()
                .filter(v -> this.value.getParts().contains(v))
                .collect(Collectors.toSet());
        this.value.getParts().removeAll(toBeUpdated);
        this.value.getParts().addAll(value.getParts());
        notifyObservers();
    }
}
