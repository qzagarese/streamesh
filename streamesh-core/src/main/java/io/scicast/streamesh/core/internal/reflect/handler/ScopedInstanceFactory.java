package io.scicast.streamesh.core.internal.reflect.handler;

import io.scicast.streamesh.core.StreameshContext;
import io.scicast.streamesh.core.internal.reflect.LocallyScoped;

public interface ScopedInstanceFactory<T> {

    T create(StreameshContext context, LocallyScoped annotation, String instance);

}
