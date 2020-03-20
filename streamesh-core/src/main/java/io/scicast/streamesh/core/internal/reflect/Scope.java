package io.scicast.streamesh.core.internal.reflect;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Builder
@Getter
@With
public class Scope {

    @JsonIgnore
    private Object value;

    @Builder.Default
    private Map<String, Scope> structure = new HashMap<>();

    @Builder.Default
    private Map<String, String> dependencies = new HashMap<>();


    public Scope attach(Scope childScope, List<String> path) {
        if (path == null || path.isEmpty()) {
            if (childScope == null) {
                return this;
            } else {
                AtomicReference<Scope> newScope = new AtomicReference<>(this);
                childScope.getStructure().forEach((key, value) -> {
                    newScope.set(newScope.get().attach(value, Arrays.asList(key)));
                });
                return newScope.get();
            }
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
