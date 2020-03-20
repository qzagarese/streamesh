package io.scicast.streamesh.core.internal.reflect;

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

    public static List<Annotation> getMarkerAnnotations(AnnotatedElement element) {
        return Stream.of(element.getDeclaredAnnotations())
                .filter(annotation -> annotation.annotationType().isAnnotationPresent(FlowGrammarMarker.class))
                .collect(Collectors.toList());
    }

}
