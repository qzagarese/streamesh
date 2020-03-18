package io.scicast.streamesh.core.internal.reflect;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.util.List;
import java.util.Map;
import java.util.Queue;

@Builder
@Getter
@With
public class Scope {

    private List<String> path;
    private Object value;

    private Map<String, Scope> structure;
    private Map<String, String> dependencies;


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
                parent.getStructure().put(path.get(i), childScope);
            }
            parent = target;
        }
        return this;
    }
}
