package io.scicast.streamesh.core.internal.reflect;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private Map<Integer, List<String>> hashCodeToPathSegment = new HashMap<>();

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

            indexByValueHashCode(parent, childScope.getValue().hashCode(), path.get(i));

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

    private void indexByValueHashCode(Scope parent, int valueHashCode, String pathSegment) {
        List<String> previousSegments = parent.hashCodeToPathSegment.get(valueHashCode);
//            if (previousSegment != null && !overwrite) {
//                throw new IllegalStateException(String.format("Hashcode %d (type: %s) is already mapped to a path starting by %s",
//                        childScope.getValue().hashCode(), childScope.getValue().getClass().getName(), previousSegment));
//            }
        if (previousSegments == null) {
            previousSegments = new ArrayList<>();
        }
        List<String> currentSegments = Stream.concat(previousSegments.stream(), Stream.of(pathSegment)).collect(Collectors.toList());
        parent.hashCodeToPathSegment.put(valueHashCode, currentSegments);
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
        return getPathByValue(value, new ArrayList<>());
    }

    public List<String> getPathByValue(Object value, List<String> hint) {
        List<String> path = new ArrayList<>();
        Scope target = this;
        AtomicInteger counter = new AtomicInteger(0);
        List<String> pathSegments = target.hashCodeToPathSegment.get(value.hashCode());
        while (pathSegments != null) {
            String segment = null;
            if (hint.size() > counter.get()) {
                segment = pathSegments.stream()
                        .filter(s -> s.equals(hint.get(counter.get())))
                        .findFirst()
                        .orElse(null);
            }
            segment = (segment == null) ? pathSegments.get(0) : segment;
            path.add(segment);
            target = target.getStructure().get(segment);
            if (target.getValue() != null && value.hashCode() == target.getValue().hashCode()) {
                return path;
            }
            pathSegments = target.hashCodeToPathSegment.get(value.hashCode());
            counter.incrementAndGet();
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
