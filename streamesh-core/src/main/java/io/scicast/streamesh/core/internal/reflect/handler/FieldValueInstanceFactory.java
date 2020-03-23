package io.scicast.streamesh.core.internal.reflect.handler;

import io.scicast.streamesh.core.StreameshContext;
import io.scicast.streamesh.core.internal.reflect.LocallyScoped;

/**
 *
 * Default implementation that simply returns the provided value.
 * This is used in most cases where the scan tree does not need to be expanded.
 *
 */
public class FieldValueInstanceFactory implements ScopedInstanceFactory<String> {

    @Override
    public String create(StreameshContext context, LocallyScoped annotation, String instance) {
        return instance;
    }
}
