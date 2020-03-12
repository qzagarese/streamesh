package io.scicast.streamesh.core.internal.reflect.handler;

import io.scicast.streamesh.core.StreameshContext;
import io.scicast.streamesh.core.internal.reflect.GrammarMarkerHandler;
import io.scicast.streamesh.core.internal.reflect.InScope;
import io.scicast.streamesh.core.internal.reflect.Scope;

public class InScopeHandler implements GrammarMarkerHandler<InScope> {

    @Override
    public Scope handle(Scope scope, StreameshContext context, Object instance, InScope annotation) {
        return null;
    }
}
