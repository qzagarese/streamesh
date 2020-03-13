package io.scicast.streamesh.core.internal.reflect;

import io.scicast.streamesh.core.StreameshContext;
import io.scicast.streamesh.core.flow.FlowDefinition;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Builder
public class ScopeFactory {


    private final Map<Annotation, GrammarMarkerHandler> handlers = new HashMap<>();
    private StreameshContext context;


    public Scope create(FlowDefinition definition) {
        AtomicReference<Scope> scope = new AtomicReference<>(Scope.builder().build());

        Set<Annotation> annotations = Stream.of(definition.getClass().getDeclaredAnnotations())
                .filter(annotation -> annotation.annotationType().isAnnotationPresent(FlowGrammarMarker.class))
                .collect(Collectors.toSet());

        // 1. build Scope and put definition in scanList
        // 2. for each annotated field, process field and check if the type corresponding is annotated
        // 3. if so, add it to the scanList
        // 4. exit when the scanList is empty

        annotations.forEach(annotation -> {
            GrammarMarkerHandler handler = getHandler(annotation);
            ScopeContext<Annotation> scopeContext = ScopeContext.builder()
                    .annotation(annotation)
                    .instance(definition)
                    .target(definition.getClass())
                    .build();
            scope.set(handler.handle(scope.get(), scopeContext, context));
        });

        return scope.get();
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
