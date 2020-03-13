package io.scicast.streamesh.core.internal.reflect;

import lombok.Builder;
import lombok.Getter;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.List;

@Getter
@Builder
public class ScopeContext<T extends Annotation> {

    T annotation;
    Object instance;
    AnnotatedElement target;
    List<String> parentPath;

}
