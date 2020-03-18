package io.scicast.streamesh.core.internal.reflect;

import lombok.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

@Builder
@Getter
@With
public class Scope {

    private Object value;

    private Map<String, Scope> structure = new HashMap<>();
    private Map<String, String> dependencies = new HashMap<>();


    public Scope attach(Scope childScope, List<String> path) {
        if (path == null || path.isEmpty()) {
            return childScope;
        }
        Scope parent = this;
        for(int i = 0; i < path.size(); i++) {
            Scope target = structure.get(path.get(i));
            if (target == null) {
                target = Scope.builder().build();
                parent.getStructure().put(path.get(i), target);
            }
            if (i == (path.size() - 1)) {
                Scope currentValue = parent.getStructure().get(path.get(i));
                if (currentValue != null && (!currentValue.isEmpty())) {
                    throw new IllegalArgumentException(
                            String.format("Cannot define value of variable %s more than once.", stringify(path)));
                }
                parent.getStructure().put(path.get(i), childScope);
            }
            parent = target;
        }
        return this;
    }

    private String stringify(List<String> path) {
        return path.stream().collect(Collectors.joining("/"));
    }

    private boolean isEmpty() {
        return structure.isEmpty();
    }

}
