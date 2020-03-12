package io.scicast.streamesh.core.internal.reflect.handler;

import io.scicast.streamesh.core.StreameshContext;

public interface ScopedInstanceFactory<T> {

    T create(StreameshContext context);

}
