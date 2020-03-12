package io.scicast.streamesh.core.internal.reflect;

import io.scicast.streamesh.core.StreameshContext;
import io.scicast.streamesh.core.flow.FlowDefinition;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ScopeBuilder {

    private Map<Annotation, Class<? extends GrammarMarkerHandler>> handlers = new HashMap<>();
    private StreameshContext context;


    public Scope build(FlowDefinition definition) {
        AtomicReference<Scope> scope = new AtomicReference<>(Scope.builder().build());

        Set<Annotation> annotations = Stream.of(definition.getClass().getDeclaredAnnotations())
                .filter(annotation -> annotation.getClass().isAnnotationPresent(FlowGrammarMarker.class))
                .collect(Collectors.toSet());

        annotations.forEach(annotation -> {
            GrammarMarkerHandler handler = getHandler(annotation);
            scope.set(handler.handle(scope.get(), context, definition, annotation));
        });

        return scope.get();
    }

    private GrammarMarkerHandler getHandler(Annotation annotation) {
        return null;
    }

}
