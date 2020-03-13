package io.scicast.streamesh.core.internal.reflect.handler;

import io.scicast.streamesh.core.StreameshContext;
import io.scicast.streamesh.core.internal.reflect.GrammarMarkerHandler;
import io.scicast.streamesh.core.internal.reflect.Resolvable;
import io.scicast.streamesh.core.internal.reflect.Scope;

import java.lang.reflect.AnnotatedElement;

public class ResolvableHandler implements GrammarMarkerHandler<Resolvable> {

    @Override
    public Scope handle(Scope scope, StreameshContext context, Object instance, Resolvable annotation, AnnotatedElement target) {
        return scope;
    }
}
