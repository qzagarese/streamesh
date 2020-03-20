package io.scicast.streamesh.core.internal.reflect;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@With
public class ScopeContext {

    private Annotation annotation;
    private Object typeLevelInstance;
    private Object instance;
    private AnnotatedElement target;
    private List<String> parentPath;
    private Scope scope;

    @Builder.Default
    private List<ScannableItem> scanList = new ArrayList<>();

    public void addTarget(ScannableItem target) {
        scanList.add(target);
    }

}
