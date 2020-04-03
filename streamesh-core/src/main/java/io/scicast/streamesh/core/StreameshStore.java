package io.scicast.streamesh.core;

import java.util.Set;

public interface StreameshStore {

    void storeDefinition(Definition definition);

    void storeFlowInstance(FlowInstance instance);

    Set<FlowInstance> getFlowInstancesByDefinition(String flowDefinitionId);

    Set<TaskDescriptor> getTasksByFlowInstance(String flowInstanceId);

    Definition getDefinitionById(String id);

    Definition getDefinitionByName(String name);

    void removeDefinition(String id);

    Set<Definition> getAllDefinitions();

    Set<TaskDescriptor> getAllTasks();

    Set<TaskDescriptor> getTasksByDefinition(String definitionId);

    TaskDescriptor getTaskById(String jobId);

    void updateTask(String definitionId, TaskDescriptor descriptor);
}
