package io.scicast.streamesh.core.internal.reflect;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.List;
import java.util.Queue;

@Getter
@Builder
@With
public class ScopeContext {

    Annotation annotation;
    Object typeLevelInstance;
    Object instance;
    AnnotatedElement target;
    List<String> parentPath;
    Scope scope;

    private Queue scanQueue;

    public void addTarget(Object target) {
        scanQueue.add(target);
    }

    public Object nextTarget() {
        return scanQueue.poll();
    }

}
