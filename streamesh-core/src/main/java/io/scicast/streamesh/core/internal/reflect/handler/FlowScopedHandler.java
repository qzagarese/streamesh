package io.scicast.streamesh.core.internal.reflect.handler;

import io.scicast.streamesh.core.StreameshContext;
import io.scicast.streamesh.core.internal.reflect.FlowScoped;
import io.scicast.streamesh.core.internal.reflect.GrammarMarkerHandler;
import io.scicast.streamesh.core.internal.reflect.Scope;
import io.scicast.streamesh.core.internal.reflect.ScopeContext;

import java.lang.reflect.AnnotatedElement;

public class FlowScopedHandler implements GrammarMarkerHandler<FlowScoped> {

    @Override
    public ScopeContext handle(ScopeContext scopeContext, StreameshContext streameshContext) {
        return scopeContext;
    }
}
