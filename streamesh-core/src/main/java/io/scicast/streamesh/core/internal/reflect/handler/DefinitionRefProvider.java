package io.scicast.streamesh.core.internal.reflect.handler;

import io.scicast.streamesh.core.Definition;
import io.scicast.streamesh.core.Micropipe;
import io.scicast.streamesh.core.StreameshContext;
import io.scicast.streamesh.core.flow.FlowDefinition;
import io.scicast.streamesh.core.flow.FlowOutputRef;
import io.scicast.streamesh.core.flow.FlowParameterRef;
import io.scicast.streamesh.core.flow.FlowReference;
import io.scicast.streamesh.core.internal.reflect.LocallyScoped;

import java.util.stream.Collectors;

public class DefinitionRefProvider implements ScopedInstanceFactory<Object> {

    @Override
    public Object create(StreameshContext context, LocallyScoped annotation, String instance) {
        Definition definition = context.getStore().getDefinitionByName(instance);
        if (definition instanceof Micropipe) {
            return definition;
        } else {
            return buildReference((FlowDefinition) definition);
        }
    }

    private Object buildReference(FlowDefinition definition) {
        return FlowReference.builder()
                .input(definition.getInput().stream()
                    .map(input -> FlowParameterRef.builder()
                        .name(input.getName())
                        .optional(input.isOptional())
                        .repeatable(input.isRepeatable())
                        .build())
                    .collect(Collectors.toList()))
                .output(definition.getOutput().stream()
                    .map(output -> FlowOutputRef.builder()
                        .name(output.getName())
                        .build())
                    .collect(Collectors.toList()))
                .definition(definition)
                .build();
    }
}
