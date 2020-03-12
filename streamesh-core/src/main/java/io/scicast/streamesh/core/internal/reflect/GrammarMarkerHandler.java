package io.scicast.streamesh.core.internal.reflect;

import io.scicast.streamesh.core.StreameshContext;

public interface GrammarMarkerHandler<T> {

    Scope handle(Scope scope, StreameshContext context, Object instance, T annotation);

}
