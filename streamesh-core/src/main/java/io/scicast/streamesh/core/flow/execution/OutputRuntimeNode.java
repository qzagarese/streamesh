package io.scicast.streamesh.core.flow.execution;

import java.util.Set;
import java.util.stream.Collectors;

public abstract class OutputRuntimeNode extends RuntimeNode {

    protected String outputName;

    @Override
    public void notify(RuntimeNode node) {
        Set<RuntimeDataValue.RuntimeDataValuePart> parts = node.getValue().getParts().stream()
                .filter(p -> p.getRefName() != null && p.getRefName().equals(outputName))
                .collect(Collectors.toSet());
        if (!parts.isEmpty()) {
            value = RuntimeDataValue.builder()
                    .parts(parts)
                    .build();
            notifyObservers();;
        }

    }
}
