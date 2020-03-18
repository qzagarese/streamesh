package io.scicast.streamesh.core.internal.reflect.handler;

import io.scicast.streamesh.core.StreameshContext;
import io.scicast.streamesh.core.internal.reflect.FlowScoped;
import io.scicast.streamesh.core.internal.reflect.GrammarMarkerHandler;
import io.scicast.streamesh.core.internal.reflect.Scope;
import io.scicast.streamesh.core.internal.reflect.ScopeContext;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class FlowScopedHandler implements GrammarMarkerHandler<FlowScoped> {

    @Override
    public ScopeContext handle(ScopeContext scopeContext, StreameshContext streameshContext) {
        FlowScoped annotation = (FlowScoped) scopeContext.getAnnotation();

        if (!annotation.as().isBlank()) {
            Scope scope = Scope.builder()
                    .value(scopeContext.getInstance())
                    .build();
            List<String> path = Arrays.asList(annotation.as());
            return scopeContext.withScope(scopeContext.getScope().attach(scope, path));
        } else if (!annotation.using().isBlank()) {
            if (!(scopeContext.getTarget() instanceof Class)) {
                throw new IllegalArgumentException("'using' can only be used on type targets, not fields. Use 'as' instead");
            }
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
            String using = (String) getFieldValue(scopeContext.getInstance(), f);
            List<String> path = Arrays.asList(using);
            Scope scope = Scope.builder()
                    .value(scopeContext.getInstance())
                    .build();
            return scopeContext.withScope(scopeContext.getScope().attach(scope, path));
        } else {
            throw new IllegalArgumentException(
                    "Either a value for the 'as' property or for the 'using' one must be provided on instances of "
                            + annotation.annotationType().getName());
        }

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
}
