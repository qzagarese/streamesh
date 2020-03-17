package io.scicast.streamesh.core.internal.reflect;

import io.scicast.streamesh.core.StreameshContext;
import io.scicast.streamesh.core.flow.FlowDefinition;
import lombok.Builder;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Builder
public class ScopeFactory {


    private final Map<Annotation, GrammarMarkerHandler> handlers = new HashMap<>();
    private StreameshContext streameshContext;


    public Scope create(FlowDefinition definition) {
        Scope scope = Scope.builder()
                .path(new ArrayList<>())
                .value(definition)
                .build();
        ScopeContext context = ScopeContext.builder()
                .instance(definition)
                .typeLevelInstance(definition)
                .target(definition.getClass())
                .parentPath(new ArrayList<>())
                .scope(scope)
                .build();
        context.addTarget(definition);
        return scan(context);

        // 1. build Scope and put definition in scanList
        // 2. for each annotated field, process field and check if the type corresponding is annotated
        // 3. if so, add it to the scanList
        // 4. exit when the scanList is empty

    }

    private Scope scan(ScopeContext context) {
        Object annotatedInstance = context.nextTarget();
        if (annotatedInstance == null) {
            return context.getScope();
        }

        List<Annotation> annotations = getMarkerAnnotations(annotatedInstance.getClass());
        if (annotations.isEmpty()) {
            return context.getScope();
        }

        AtomicReference<ScopeContext> cumulativeContext = new AtomicReference<>(context);

        annotations.forEach(annotation -> {
            ScopeContext newContext = getHandler(annotation).handle(context.withAnnotation(annotation), streameshContext);
            cumulativeContext.set(cumulativeContext.get()
                    .withScope(newContext.getScope())
                    .withScanQueue(newContext.getScanQueue()));

        });

        return null;
    }

    private List<Annotation> getMarkerAnnotations(AnnotatedElement element) {
        return Stream.of(element.getDeclaredAnnotations())
                    .filter(annotation -> annotation.annotationType().isAnnotationPresent(FlowGrammarMarker.class))
                    .collect(Collectors.toList());
    }

    private GrammarMarkerHandler getHandler(Annotation annotation) {
        GrammarMarkerHandler grammarMarkerHandler = handlers.get(annotation);
        if (grammarMarkerHandler == null) {
            grammarMarkerHandler = instantiateHandler(annotation);
            handlers.put(annotation, grammarMarkerHandler);
        }
        return grammarMarkerHandler;
    }

    private GrammarMarkerHandler instantiateHandler(Annotation annotation) {
        GrammarMarkerHandler grammarMarkerHandler;
        FlowGrammarMarker marker = annotation.annotationType().getAnnotation(FlowGrammarMarker.class);
        Constructor c = Stream.of(marker.handler().getConstructors())
                .findFirst()
                .orElseThrow(() -> {
                    return new IllegalStateException(
                            String.format("Cannot instantiate %s. Handlers must declare only one zero-arguments constructor.",
                                    marker.handler().getName()));
                });
        try {
            grammarMarkerHandler = (GrammarMarkerHandler) c.newInstance(new Object[0]);
        } catch (Exception e) {
            throw new RuntimeException("Cannot instantiate " + marker.handler().getName(), e);
        }
        return grammarMarkerHandler;
    }

}
