package io.scicast.streamesh.core.internal.reflect;

import io.scicast.streamesh.core.MicroPipe;
import io.scicast.streamesh.core.StreameshContext;
import io.scicast.streamesh.core.flow.FlowDefinition;
import io.scicast.streamesh.core.flow.FlowPipe;
import io.scicast.streamesh.core.flow.FlowReference;
import io.scicast.streamesh.core.flow.PipeInput;
import lombok.Builder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
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
        validatePipesInputBindings(fullScope);
        return fullScope;
    }

    private void validatePipesInputBindings(Scope scope) {
        List<Scope> pipeScopes = scope.getStructure().values().stream()
                .filter(s -> s.getValue() instanceof FlowPipe)
                .collect(Collectors.toList());

        validateMicroPipesBindings(pipeScopes);
        validateFlowReferencesBindings(pipeScopes);

    }

    private void validateFlowReferencesBindings(List<Scope> pipeScopes) {
        pipeScopes.forEach(pipeScope -> {
            Scope type = pipeScope.getStructure().values().stream()
                    .filter(s -> s.getValue() instanceof FlowReference)
                    .findFirst()
                    .orElse(null);
            List<Scope> inputScopes = getPipeInputScopes(pipeScope);
            if (type != null && type.getValue() != null) {
                FlowReference fr = (FlowReference) type.getValue();
                fr.getInput().forEach(parameter -> {
                    ValueDependency dependency = inputScopes.stream()
                            .flatMap(s -> s.getDependencies().stream())
                            .filter(d -> d.getResolvedTargetValue().equals(parameter))
                            .findFirst()
                            .orElse(null);
                    if (dependency == null && !parameter.isOptional()) {
                        throw new IllegalArgumentException(
                                String.format("Pipe stage %s is referencing flow %s, " +
                                                "but is not providing the mandatory parameter %s.",
                                        ((FlowPipe) pipeScope.getValue()).getAs(),
                                        fr.getDefinition().getName(),
                                        parameter.getName()));
                    }
                });
            }
        });
    }

    private void validateMicroPipesBindings(List<Scope> pipeScopes) {
        pipeScopes.forEach(pipeScope -> {
            Scope type = pipeScope.getStructure().values().stream()
                    .filter(s -> s.getValue() instanceof MicroPipe)
                    .findFirst()
                    .orElse(null);
            List<Scope> inputScopes = getPipeInputScopes(pipeScope);
            if (type != null && type.getValue() != null) {
                MicroPipe mp = (MicroPipe) type.getValue();
                mp.getInputMapping().getParameters().forEach(parameter -> {
                    ValueDependency dependency = inputScopes.stream()
                            .flatMap(s -> s.getDependencies().stream())
                            .filter(d -> d.getResolvedTargetValue().equals(parameter))
                            .findFirst()
                            .orElse(null);
                    if (dependency == null && !parameter.isOptional()) {
                        throw new IllegalArgumentException(
                                String.format("Pipe stage %s is referencing micro pipe %s, " +
                                                "but is not providing the mandatory parameter %s.",
                                        ((FlowPipe) pipeScope.getValue()).getAs(),
                                        mp.getName(),
                                        parameter.getName()));
                    }
                });
            }
        });
    }

    private List<Scope> getPipeInputScopes(Scope pipeScope) {
        Scope pipeInputScope = pipeScope.subScope(Arrays.asList(FlowPipe.INPUT_SCOPE_PATH));
        return pipeInputScope == null
                ? new ArrayList<>()
                : pipeInputScope.getStructure().values().stream()
                    .filter(s -> s.getValue() instanceof PipeInput)
                    .collect(Collectors.toList());
    }

    private void verifyDependencies(Scope fullScope) {
        verifyDependencyLayer(fullScope, fullScope);
    }

    private void verifyDependencyLayer(Scope fullScope, Scope current) {
        current.getDependencies().forEach(dependency -> {
            List<String> path = dependency.getPath();
            if (!fullScope.pathExists(path)) {
                throw new IllegalArgumentException("Cannot find symbol " + dependency.getStringifiedPath());
            } else {
                Object targetValue = fullScope.getValue(path);
                Class<?> targetType = targetValue != null ? targetValue.getClass() : null;
                if (targetType == null) {
                    throw new IllegalStateException("Cannot satisfy dependency " + dependency.getStringifiedPath());
                }

                if (!dependency.getExpectedTargetTypes().isEmpty()) {
                dependency.getExpectedTargetTypes().stream()
                        .filter(type -> type.equals(targetType))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Cannot satisfy dependency "
                                + dependency.getStringifiedPath()
                                + buildDetailedMessage(targetType, dependency.getExpectedTargetTypes())));
                }
                dependency.setResolvedTargetValue(targetValue);
            }
        });
        current.getStructure().values().forEach(scope -> verifyDependencyLayer(fullScope, scope));
    }

    private String buildDetailedMessage(Class<?> targetType, List<Class<?>> expectedTargetTypes) {
        StringBuffer buffer = new StringBuffer("\nExpecting any of: \n");
        expectedTargetTypes.forEach(t -> buffer.append("\t- " + t.getName() + "\n"));
        buffer.append("Found: " + targetType.getName());
        return buffer.toString();
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
        Stream.of(annotatedInstance.getClass().getDeclaredFields())
                .filter(f -> !Modifier.isStatic(f.getModifiers()))
                .forEach(field -> {
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
