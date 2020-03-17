package io.scicast.streamesh.core.internal.reflect.handler;

import io.scicast.streamesh.core.StreameshContext;
import io.scicast.streamesh.core.internal.reflect.GrammarMarkerHandler;
import io.scicast.streamesh.core.internal.reflect.LocallyScoped;
import io.scicast.streamesh.core.internal.reflect.Scope;
import io.scicast.streamesh.core.internal.reflect.ScopeContext;

import java.lang.reflect.AnnotatedElement;

public class LocallyScopedHandler implements GrammarMarkerHandler<LocallyScoped> {

    @Override
    public ScopeContext handle(ScopeContext scopeContext, StreameshContext context) {
        return scopeContext;
    }
}
