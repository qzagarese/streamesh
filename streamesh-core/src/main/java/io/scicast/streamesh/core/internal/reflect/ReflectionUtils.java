package io.scicast.streamesh.core.internal.reflect;

import io.scicast.streamesh.core.internal.reflect.handler.ScopedInstanceFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReflectionUtils {

    public static Object getFieldValue(Object annotatedInstance, Field field) {
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

    public static GrammarMarkerHandler instantiateHandler(Annotation annotation) {
        GrammarMarkerHandler grammarMarkerHandler;
        FlowGrammarMarker marker = annotation.annotationType().getAnnotation(FlowGrammarMarker.class);
        grammarMarkerHandler = (GrammarMarkerHandler) doInstantiation(marker.handler(), "Handlers");
        return grammarMarkerHandler;
    }

    public static ScopedInstanceFactory instantiateFactory(Class<? extends ScopedInstanceFactory> concreteType) {
        return (ScopedInstanceFactory) doInstantiation(concreteType, "Factories");
    }

    private static Object doInstantiation(Class<?> type, String instanceType) {
        Object instance;
        Constructor c = Stream.of(type.getConstructors())
                .findFirst()
                .orElseThrow(() -> {
                    return new IllegalStateException(
                            String.format("Cannot instantiate %s. %s must declare only one zero-arguments constructor.",
                                    type.getName(), instanceType));
                });
        try {
            instance = c.newInstance(new Object[0]);
        } catch (Exception e) {
            throw new RuntimeException("Cannot instantiate " + type.getName(), e);
        }
        return instance;
    }

    public static List<Annotation> getMarkerAnnotations(AnnotatedElement element) {
        return Stream.of(element.getDeclaredAnnotations())
                .filter(annotation -> annotation.annotationType().isAnnotationPresent(FlowGrammarMarker.class))
                .collect(Collectors.toList());
    }

}
