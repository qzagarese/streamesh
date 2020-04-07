package io.scicast.streamesh.core.flow.execution;

import io.scicast.streamesh.core.flow.FlowInstance;
import io.scicast.streamesh.core.flow.FlowDefinition;

import java.util.Map;
import java.util.function.Consumer;

public interface FlowExecutor {

    FlowInstance execute(FlowDefinition flow, Map<?, ?> input, Consumer<FlowExecutionEvent<?>> eventHandler);

}
