package io.scicast.streamesh.core.internal.reflect.handler;

import io.scicast.streamesh.core.StreameshContext;
import io.scicast.streamesh.core.internal.reflect.*;

import java.util.Arrays;
import java.util.UUID;

public class InScopeHandler implements GrammarMarkerHandler<InScope> {

    @Override
    public HandlerResult handle(ScopeContext scopeContext, StreameshContext context) {

        Scope result = scopeContext.getScope();
        if (((InScope)scopeContext.getAnnotation()).includeAnonymousReference()) {
            result = result.attach(Scope.builder()
                    .value(scopeContext.getInstance())
                    .build(), Arrays.asList(UUID.randomUUID().toString()), false);
        }
        return HandlerResult.builder()
                .resultScope(result)
                .targetMountPoint(scopeContext.getParentPath())
                .targetValue(scopeContext.getInstance())
                .build();
    }
}
