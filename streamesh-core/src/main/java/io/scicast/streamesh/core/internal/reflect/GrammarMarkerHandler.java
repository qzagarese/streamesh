package io.scicast.streamesh.core.internal.reflect;

import io.scicast.streamesh.core.StreameshContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

public interface GrammarMarkerHandler<T extends Annotation> {

    Scope handle(Scope scope, ScopeContext<T> scopeContext, StreameshContext streameshContext);

}
