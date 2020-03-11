package io.scicast.streamesh.core;

import java.io.InputStream;
import java.util.Map;
import java.util.Set;

public interface StreameshOrchestrator {

    String applyDefinition(Definition definition);

    Definition getDefinition(String id);

    Definition getDefinitionByName(String name);

    void removeDefinition(String id);

    Set<Definition> getDefinitions();

    Set<TaskDescriptor> getAllTasks();

    Set<TaskDescriptor> getTasksByDefinition(String definitionId);

    TaskDescriptor scheduleTask(String definitionId, Map<?, ?> input);

    TaskDescriptor scheduleSecureTask(String definitionId, Map<?, ?> input, String publicKey);

    TaskDescriptor getTask(String taskId);

    InputStream getTaskOutput(String taskDescriptorId, String outputName);
}
