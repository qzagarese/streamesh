package io.scicast.streamesh.core.internal.reflect;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.util.Map;
import java.util.Queue;

@Builder
@Getter
@With
public class Scope {

    private Map<String, Object> structure;
    private Map<String, String> dependencies;

    private Queue scanList;

    public void addTarget(Object target) {
        scanList.add(target);
    }

    public Object nextTarget() {
        return scanList.poll();
    }

}
