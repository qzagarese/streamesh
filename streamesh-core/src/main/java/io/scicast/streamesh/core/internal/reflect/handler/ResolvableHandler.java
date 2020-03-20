package io.scicast.streamesh.core.internal.reflect.handler;

import io.scicast.streamesh.core.StreameshContext;
import io.scicast.streamesh.core.internal.reflect.*;

import java.lang.reflect.AnnotatedElement;

public class ResolvableHandler implements GrammarMarkerHandler<Resolvable> {

    @Override
    public HandlerResult handle(ScopeContext scopeContext, StreameshContext context) {
        return HandlerResult.builder()
                .resultScope(scopeContext.getScope())
                .targetMountPoint(scopeContext.getParentPath())
                .targetValue(scopeContext.getInstance())
                .build();
    }
}
