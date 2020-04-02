package io.scicast.streamesh.core.internal.reflect;

import io.scicast.streamesh.core.internal.reflect.handler.GraphNodeHandler;
import io.scicast.streamesh.core.internal.reflect.handler.ScopedInstanceFactory;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReflectionUtils {

    static PrintStream stream;

    static {
        try {
            stream = new PrintStream(new FileOutputStream("scope-history.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


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

    public static GraphNodeHandler instantiateGraphNodeHandler(Class<? extends GraphNodeHandler> concreteType) {
        return (GraphNodeHandler) doInstantiation(concreteType, "Graph node handlers");
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

    public static boolean shouldScanType(Object fieldValue) {
        if (fieldValue == null) {
            return false;
        }
        Class<?> target;
        if (fieldValue instanceof Collection) {
            Collection<?> c = (Collection) fieldValue;
            if (c.isEmpty()) {
                return false;
            }
            target = c.stream().findFirst().get().getClass();
        } else {
            target = fieldValue.getClass();
        }
        return !getMarkerAnnotations(target).isEmpty();
    }

}
