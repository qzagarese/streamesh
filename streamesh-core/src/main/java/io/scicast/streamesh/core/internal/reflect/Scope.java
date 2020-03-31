package io.scicast.streamesh.core.internal.reflect;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import lombok.With;

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
    @Getter
    private Map<String, Scope> structure = new HashMap<>();

    @Builder.Default
    private List<ValueDependency> dependencies = new ArrayList<>();

    @Builder.Default
    private Map<Integer, String> hashCodeToPathSegment = new HashMap<>();

    public Scope attach(Scope childScope, List<String> path, boolean overwrite) {
        if (path == null || path.isEmpty()) {
            if (childScope == null) {
                return this;
            } else {
                AtomicReference<Scope> newScope = new AtomicReference<>(this);
                childScope.getStructure().forEach((key, value) -> {
                    newScope.set(newScope.get().attach(value, Arrays.asList(key), overwrite));
                });
                return newScope.get();
            }
        }
        Scope parent = this;
        for(int i = 0; i < path.size(); i++) {
            parent.hashCodeToPathSegment.put(childScope.getValue().hashCode(), path.get(i));
            Scope target = parent.getStructure().get(path.get(i));
            if (target == null) {
                target = Scope.builder().build();
                parent.getStructure().put(path.get(i), target);
            }
            if (i == (path.size() - 1)) {
                if (!target.isEmpty() && !overwrite) {
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
        return path.stream().collect(Collectors.joining("."));
    }

    private boolean isEmpty() {
        return structure.isEmpty();
    }

    public Object getValue(List<String> path) {
        Scope scope = this.subScope(path);
        return scope != null ? scope.getValue() : null;
    }

    public Scope subScope(List<String> path) {
        if (path == null || path.isEmpty()) {
            return this;
        }

        Scope scope = this;
        for (String element: path) {
            scope = scope.getStructure().get(element);
            if (scope == null) {
                return null;
            }
        }
        return scope;
    }

    public List<String> getPathByValue(Object value) {
        List<String> path = new ArrayList<>();
        Scope target = this;
        String pathSegment = target.hashCodeToPathSegment.get(value.hashCode());
        while (pathSegment != null) {
            path.add(pathSegment);
            target = target.getStructure().get(pathSegment);
            if (target.getValue() != null && value.hashCode() == target.getValue().hashCode()) {
                return path;
            }
            pathSegment = target.hashCodeToPathSegment.get(value.hashCode());
        }
        return null;
    }

    public boolean pathExists(List<String> path) {
        if (path == null) {
            return false;
        }
        if (path.isEmpty()) {
            return true;
        }
        Scope parent = this;
        for(int i = 0; i < path.size(); i++) {
            Scope target = parent.getStructure().get(path.get(i));
            if (target == null) {
                return false;
            }
            parent = target;
        }
        return true;
    }
}
