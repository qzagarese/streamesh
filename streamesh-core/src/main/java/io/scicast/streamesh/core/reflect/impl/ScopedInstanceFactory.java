package io.scicast.streamesh.core.reflect.impl;

import io.scicast.streamesh.core.StreameshContext;

public interface ScopedInstanceFactory<T> {

    T create(StreameshContext context);

}
