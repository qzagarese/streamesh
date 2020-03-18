package io.scicast.streamesh.core.internal.reflect;

import io.scicast.streamesh.core.StreameshContext;
import io.scicast.streamesh.core.flow.FlowDefinition;
import lombok.Builder;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;
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
                .scanList(new ArrayList())
                .build();
        return scan(context);
    }

    private Scope scan(ScopeContext context) {
        Object annotatedInstance = context.getTypeLevelInstance();
        if (annotatedInstance == null) {
            return context.getScope();
        }

        List<Annotation> annotations = getMarkerAnnotations(annotatedInstance.getClass());
        if (annotations.isEmpty()) {
            return context.getScope();
        }

        AtomicReference<ScopeContext> cumulativeContext = new AtomicReference<>(context);

        annotations.forEach(annotation -> {
            ScopeContext newContext = getHandler(annotation).handle(cumulativeContext.get().withAnnotation(annotation), streameshContext);
            cumulativeContext.set(cumulativeContext.get()
                    .withScope(newContext.getScope())
                    .withScanList(newContext.getScanList()));
        });

        List<Object> processableChildren = new ArrayList<>();

        Stream.of(annotatedInstance.getClass().getDeclaredFields()).forEach(field -> {
            Object fieldValue = getFieldValue(annotatedInstance, field);
            List<Annotation> markers = getMarkerAnnotations(field);
            if (!markers.isEmpty() && fieldValue != null) {
                markers.forEach(annotation -> {

                    ScopeContext fieldLevelContext = context.withAnnotation(annotation)
                            .withTypeLevelInstance(annotatedInstance)
                            .withInstance(fieldValue)
                            .withParentPath(cumulativeContext.get().getParentPath());
                    // TODO prepare path in context

                    ScopeContext newContext = getHandler(annotation).handle(fieldLevelContext, streameshContext);
                    cumulativeContext.set(cumulativeContext.get()
                            .withScope(newContext.getScope())
                            .withScanList(newContext.getScanList()));
                });
                if(!getMarkerAnnotations(fieldValue.getClass()).isEmpty()) {
                    processableChildren.add(fieldValue);
                }
            }
        });

        processableChildren.forEach(child -> {
            // TODO make recursive call to children (need to prepare context)
            ScopeContext childContext = cumulativeContext.get()
                    .withScanList(new ArrayList())
                    .withTypeLevelInstance(child)
                    .withInstance(child);
            Scope childScope = scan(childContext);
            cumulativeContext.set(cumulativeContext.get()
                    .withScope(cumulativeContext.get().getScope().attach(childScope, childContext.getParentPath())));
            // TODO aggregate scope from children results to current context scope
        });

        return cumulativeContext.get().getScope();
    }

    private Object getFieldValue(Object annotatedInstance, Field field) {
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
