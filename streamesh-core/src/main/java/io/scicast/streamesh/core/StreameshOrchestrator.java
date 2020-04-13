package io.scicast.streamesh.core;

import io.scicast.streamesh.core.flow.FlowInstance;
import io.scicast.streamesh.core.flow.execution.FlowExecutionEvent;

import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public interface StreameshOrchestrator {

    String applyDefinition(Definition definition);

    Definition getDefinition(String id);

    Definition getDefinitionByName(String name);

    void removeDefinition(String id);

    Set<Definition> getDefinitions();

    Set<TaskDescriptor> getAllTasks();

    Set<TaskDescriptor> getTasksByDefinition(String definitionId);

    Set<TaskDescriptor> getTasksByFlowInstanceId(String flowInstanceId);

    TaskDescriptor scheduleTask(String definitionId, Map<?, ?> input);

    TaskDescriptor scheduleTask(String definitionId, Map<?, ?> input, Consumer<TaskExecutionEvent<?>> eventHandler);

    TaskDescriptor scheduleTask(String definitionId, String taskId, Map<?, ?> input, Consumer<TaskExecutionEvent<?>> eventHandler);

    FlowInstance scheduleFlow(String definitionId, Map<?, ?> input);

    FlowInstance scheduleFlow(String definitionId, Map<?, ?> input, Consumer<FlowExecutionEvent<?>> eventHandler);

    FlowInstance scheduleFlow(String definitionId, String instanceId, Map<?, ?> input, Consumer<FlowExecutionEvent<?>> eventHandler);

    TaskDescriptor scheduleSecureTask(String definitionId, Map<?, ?> input, String publicKey);

    TaskDescriptor getTask(String taskId);

    InputStream getTaskOutput(String taskDescriptorId, String outputName);

    Set<FlowInstance> getAllFlowInstances();
}
