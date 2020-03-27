package io.scicast.streamesh.core.internal.reflect.handler;

import io.scicast.streamesh.core.StreameshContext;
import io.scicast.streamesh.core.internal.reflect.*;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class LocallyScopedHandler implements GrammarMarkerHandler<LocallyScoped> {


    private Map<Class<? extends ScopedInstanceFactory>, ScopedInstanceFactory> factories = new HashMap<>();
    private StreameshContext context;

    @Override
    public HandlerResult handle(ScopeContext scopeContext, StreameshContext context) {
        this.context = context;
        ReflectionUtils.logState(scopeContext);
        LocallyScoped annotation = (LocallyScoped) scopeContext.getAnnotation();

        List<String> path = scopeContext.getParentPath().stream().collect(Collectors.toList());

        if (annotation.randomName()) {
            path.add(UUID.randomUUID().toString());
        } else if (!annotation.as().isBlank()) {
            path.add(annotation.as());
        } else if (!annotation.using().isBlank()) {
            if (!(scopeContext.getTarget() instanceof Class)) {
                throw new IllegalArgumentException("'using' can only be used on type targets, not fields. Use 'as' instead");
            }
            path.add(extractValueFromUsingField(scopeContext, annotation));
        } else {
            throw new IllegalArgumentException(
                    "Either a value for the 'as' property or for the 'using' one must be provided on instances of "
                            + annotation.annotationType().getName());
        }
        Scope scope = Scope.builder()
                .value(buildTargetInstance(scopeContext))
                .build();
        return buildResult(scopeContext, scope, path);

    }

    private String extractValueFromUsingField(ScopeContext scopeContext, LocallyScoped annotation) {
        Class<?> clazz = (Class<?>) scopeContext.getTarget();
        Field f;
        try {
            f = clazz.getDeclaredField(annotation.using());
            if (!f.getType().equals(String.class)) {
                throw new IllegalArgumentException(String.format("Field %s must be a string.", f.getName()));
            }
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException(String.format("Cannot find field %s on type %s",
                    annotation.using(),
                    clazz.getName()));
        }
        return (String) ReflectionUtils.getFieldValue(scopeContext.getInstance(), f);
    }

    private HandlerResult buildResult(ScopeContext scopeContext, Scope childScope, List<String> path) {
        return HandlerResult.builder()
                .targetMountPoint(path)
                .targetValue(childScope.getValue())
                .resultScope(scopeContext.getScope().attach(childScope, path, false))
                .build();
    }

    private Object buildTargetInstance(ScopeContext scopeContext) {
        AnnotatedElement target = scopeContext.getTarget();
        if (target instanceof Class
            || ((target instanceof  Field) && !((Field) target).getType().equals(String.class))) {
            return scopeContext.getInstance();
        } else {
            LocallyScoped annotation = (LocallyScoped) scopeContext.getAnnotation();
            ScopedInstanceFactory factory = factories.get(annotation.factory());
            if (factory == null) {
                factory = ReflectionUtils.instantiateFactory(annotation.factory());
                factories.put(annotation.factory(), factory.getClass().cast(factory));
            }
            return factory.create(context, annotation, (String) scopeContext.getInstance());
        }
    }
}
