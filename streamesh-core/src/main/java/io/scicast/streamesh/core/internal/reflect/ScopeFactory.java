package io.scicast.streamesh.core.internal.reflect;

import io.scicast.streamesh.core.StreameshContext;
import io.scicast.streamesh.core.flow.FlowDefinition;
import lombok.Builder;

import java.lang.annotation.Annotation;
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
                .build();
        ScopeContext context = ScopeContext.builder()
                .instance(definition)
                .typeLevelInstance(definition)
                .target(definition.getClass())
                .parentPath(new ArrayList<>())
                .scope(scope)
                .scanList(new ArrayList())
                .build();
        Scope fullScope = scan(context);
        verifyDependencies(fullScope);

        return fullScope;
    }

    private void verifyDependencies(Scope fullScope) {
        verifyDependencyLayer(fullScope, fullScope);
    }

    private void verifyDependencyLayer(Scope fullScope, Scope current) {
        current.getDependencies().forEach(path -> {
            if (!fullScope.pathExists(path)) {
                throw new IllegalArgumentException("Cannot find symbol " + path.stream().collect(Collectors.joining(".")));
            }
        });
        current.getStructure().values().forEach(scope -> verifyDependencyLayer(fullScope, scope));
    }

    private Scope scan(ScopeContext context) {
        Object annotatedInstance = context.getTypeLevelInstance();
        if (annotatedInstance == null) {
            return context.getScope();
        }

        List<Annotation> annotations = ReflectionUtils.getMarkerAnnotations(annotatedInstance.getClass());
        if (annotations.isEmpty()) {
            return context.getScope();
        }

        //Handle type level annotations
        context = processTypeLevelAnnotations(annotations, context);


        //Handle field level annotations
        context = processFieldLevelAnnotations(annotatedInstance, context);


        AtomicReference<ScopeContext> cumulativeContext = new AtomicReference<>(context);

        context.getScanList().forEach(child -> {
            List<String> childMountPoint = child.getMountPoint();

            ScopeContext childContext;
            Scope childScope;

            if (child.getMultiplicity().equals(ScannableItem.Multiplicity.SINGLE)) {
                childContext = singleChildContext(cumulativeContext.get(), childMountPoint, child.getValue());
                childScope = scan(childContext);
            } else {
                Collection<?> children = (Collection) child.getValue();
                AtomicReference<ScopeContext> childCumulativeContext = new AtomicReference<>(cumulativeContext.get());
                children.stream().forEach(c -> {
                    ScopeContext sc = singleChildContext(childCumulativeContext.get(), childMountPoint, c);
                    childCumulativeContext.set(childCumulativeContext.get()
                        .withScope(scan(sc)));
                });
                childScope = childCumulativeContext.get().getScope();
            }

            cumulativeContext.set(cumulativeContext.get()
                    .withScope(childScope));
        });
        return cumulativeContext.get().getScope();
    }

    private ScopeContext singleChildContext(ScopeContext cumulativeContext, List<String> childMountPoint, Object value) {
        return cumulativeContext
                .withTypeLevelInstance(value)
                .withInstance(value)
                .withTarget(value.getClass())
                .withParentPath(childMountPoint)
                .withScanList(new ArrayList<>());
    }

    private ScopeContext processFieldLevelAnnotations(Object annotatedInstance, ScopeContext mainContext) {
        AtomicReference<ScopeContext> cumulativeContext = new AtomicReference<>(mainContext);
        Stream.of(annotatedInstance.getClass().getDeclaredFields()).forEach(field -> {
            Object fieldValue = ReflectionUtils.getFieldValue(annotatedInstance, field);
            List<Annotation> markers = ReflectionUtils.getMarkerAnnotations(field);

            AtomicReference<ScopeContext> fieldAggregatedContext = new AtomicReference<>(cumulativeContext.get());
            if (!markers.isEmpty() && fieldValue != null) {
                markers.forEach(annotation -> {

                    ScopeContext fieldLevelContext = ScopeContext.builder()
                            .annotation(annotation)
                            .target(field)
                            .typeLevelInstance(annotatedInstance)
                            .instance(fieldValue)
                            .parentPath(fieldAggregatedContext.get().getParentPath())
                            .scope(fieldAggregatedContext.get().getScope())
                            .build();

                    HandlerResult result = getHandler(annotation).handle(fieldLevelContext, streameshContext);
                    // for each field, decide whether the corresponding type should be processed
                    if(ReflectionUtils.shouldScanType(result.getTargetValue())) {
                        List<String> mountPoint = result.getTargetMountPoint();
                        fieldAggregatedContext.get().getScanList().add(ScannableItem.builder()
                                .value(result.getTargetValue())
                                .multiplicity((result.getTargetValue() instanceof Collection)
                                        ? ScannableItem.Multiplicity.MULTIPLE
                                        : ScannableItem.Multiplicity.SINGLE)
                                .mountPoint(mountPoint)
                                .build());
                    }
                    fieldAggregatedContext.set(fieldAggregatedContext.get().withScope(result.getResultScope()));
                });

            }
            cumulativeContext.set(cumulativeContext.get()
                    .withScope(fieldAggregatedContext.get().getScope())
                    .withScanList(fieldAggregatedContext.get().getScanList()));
        });
        return cumulativeContext.get();
    }

    private ScopeContext processTypeLevelAnnotations(List<Annotation> annotations, ScopeContext mainContext) {
        AtomicReference<ScopeContext> cumulativeContext = new AtomicReference<>(mainContext);
        annotations.forEach(annotation -> {
            HandlerResult result = getHandler(annotation).handle(cumulativeContext.get().withAnnotation(annotation), streameshContext);
            cumulativeContext.set(cumulativeContext.get()
                    .withScope(result.getResultScope())
                    .withParentPath(result.getTargetMountPoint()));
        });
        return cumulativeContext.get();
    }



    private GrammarMarkerHandler getHandler(Annotation annotation) {
        GrammarMarkerHandler grammarMarkerHandler = handlers.get(annotation);
        if (grammarMarkerHandler == null) {
            grammarMarkerHandler = ReflectionUtils.instantiateHandler(annotation);
            handlers.put(annotation, grammarMarkerHandler);
        }
        return grammarMarkerHandler;
    }



}
