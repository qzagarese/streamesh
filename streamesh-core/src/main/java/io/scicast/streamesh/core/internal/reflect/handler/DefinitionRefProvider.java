package io.scicast.streamesh.core.internal.reflect.handler;

import io.scicast.streamesh.core.Definition;
import io.scicast.streamesh.core.StreameshContext;
import io.scicast.streamesh.core.internal.reflect.LocallyScoped;

public class DefinitionRefProvider implements ScopedInstanceFactory<Definition> {

    @Override
    public Definition create(StreameshContext context, LocallyScoped annotation, String instance) {
        return null;
    }
}
