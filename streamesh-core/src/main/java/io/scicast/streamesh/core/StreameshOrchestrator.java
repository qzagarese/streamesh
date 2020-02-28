package io.scicast.streamesh.core;

import java.io.InputStream;
import java.util.Map;
import java.util.Set;

public interface StreameshOrchestrator {

    String applyDefinition(Micropipe definition);

    Micropipe getDefinition(String id);

    Micropipe getDefinitionByName(String name);

    void removeDefinition(String id);

    Set<Micropipe> getDefinitions();

    Set<TaskDescriptor> getAllTasks();

    Set<TaskDescriptor> getTasksByDefinition(String definitionId);

    TaskDescriptor scheduleTask(String definitionId, Map<?, ?> input);

    TaskDescriptor scheduleSecureTask(String definitionId, Map<?, ?> input, String publicKey);

    TaskDescriptor getTask(String taskId);

    InputStream getTaskOutput(String taskDescriptorId, String outputName);
}
