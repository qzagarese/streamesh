package io.scicast.streamesh.core.internal.reflect.handler;

import io.scicast.streamesh.core.StreameshContext;
import io.scicast.streamesh.core.internal.reflect.*;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class FlowScopedHandler implements GrammarMarkerHandler<FlowScoped> {



    @Override
    public HandlerResult handle(ScopeContext scopeContext, StreameshContext streameshContext) {
        ReflectionUtils.logState(scopeContext);

        FlowScoped annotation = (FlowScoped) scopeContext.getAnnotation();

        if (!annotation.as().isBlank()) {
            Scope scope = Scope.builder()
                    .value(scopeContext.getInstance())
                    .build();
            List<String> path = Arrays.asList(annotation.as());
            return buildResult(scopeContext, scope, path);
        } else if (!annotation.using().isBlank()) {
            if (!(scopeContext.getTarget() instanceof Class)) {
                throw new IllegalArgumentException("'using' can only be used on type targets, not fields. Use 'as' instead");
            }
            String using = extractUsingValue(scopeContext, annotation);
            Scope scope = Scope.builder()
                    .value(scopeContext.getInstance())
                    .build();
            return buildResult(scopeContext, scope, Arrays.asList(using));
        } else {
            throw new IllegalArgumentException(
                    "Either a value for the 'as' property or for the 'using' one must be provided on instances of "
                            + annotation.annotationType().getName());
        }

    }

    private String extractUsingValue(ScopeContext scopeContext, FlowScoped annotation) {
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

    private HandlerResult buildResult(ScopeContext scopeContext, Scope localScope, List<String> path) {
        return HandlerResult.builder()
                .targetValue(scopeContext.getInstance())
                .resultScope(scopeContext.getScope().attach(localScope, path, false))
                .targetMountPoint(path)
                .build();
    }


}
