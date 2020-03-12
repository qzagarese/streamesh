package io.scicast.streamesh.core.internal.reflect.handler;

import io.scicast.streamesh.core.StreameshContext;
import io.scicast.streamesh.core.internal.reflect.FlowScoped;
import io.scicast.streamesh.core.internal.reflect.GrammarMarkerHandler;
import io.scicast.streamesh.core.internal.reflect.Scope;

public class FlowScopedHandler implements GrammarMarkerHandler<FlowScoped> {

    @Override
    public Scope handle(Scope scope, StreameshContext context, Object instance, FlowScoped annotation) {
        return null;
    }
}
