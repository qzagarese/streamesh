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


        Stream.of(annotatedInstance.getClass().getDeclaredFields()).forEach(field -> {
            Object fieldValue = getFieldValue(annotatedInstance, field);
            List<Annotation> markers = getMarkerAnnotations(field);

            AtomicReference<ScopeContext> fieldAggregatedContext = new AtomicReference<>(cumulativeContext.get());
            if (!markers.isEmpty() && fieldValue != null) {
                markers.forEach(annotation -> {

                    ScopeContext fieldLevelContext = fieldAggregatedContext.get().withAnnotation(annotation)
                            .withTarget(field)
                            .withTypeLevelInstance(annotatedInstance)
                            .withInstance(fieldValue)
                            .withParentPath(fieldAggregatedContext.get().getParentPath());

                    ScopeContext newContext = getHandler(annotation).handle(fieldLevelContext, streameshContext);
                    fieldAggregatedContext.set(
                            fieldAggregatedContext.get()
                                    .withScope(newContext.getScope())
                                    .withScanList(newContext.getScanList()));
                });
                if(!getMarkerAnnotations(fieldValue.getClass()).isEmpty()) {
                    List<String> path = fieldAggregatedContext.get().getParentPath();
                    cumulativeContext.get().getScanList().add(ScannableItem.builder()
                            .value(fieldValue)
                            .mountedAs(path.size() > 0 ? path.get(path.size() - 1) : null)
                            .build());
                }
            }
        });

        cumulativeContext.get().getScanList().forEach(child -> {
            List<String> newPath = context.getParentPath();
            newPath.add(child.getMountedAs());

            ScopeContext childContext = cumulativeContext.get()
                    .withScanList(new ArrayList())
                    .withTypeLevelInstance(child.getValue())
                    .withInstance(child.getValue())
                    .withTarget(child.getValue().getClass())
                    .withParentPath(newPath);

            Scope childScope = scan(childContext);
            cumulativeContext.set(cumulativeContext.get()
                    .withScope(cumulativeContext.get().getScope().attach(childScope, childContext.getParentPath())));
        });

        return cumulativeContext.get().getScope();
    }

    private Object getFieldValue(Object annotatedInstance, Field field) {
        boolean accessible = field.canAccess(annotatedInstance);
        field.setAccessible(true);
        Object fieldValue;
        try {
            fieldValue = field.get(annotatedInstance);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(
                    String.format("Cannot access value of  field %s.%s.",
                            field.getDeclaringClass().getName(),
                            field.getName()), e);
        }
        field.setAccessible(accessible);
        return fieldValue;
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
