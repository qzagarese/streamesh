package io.scicast.streamesh.core;

import io.scicast.streamesh.core.flow.FlowInstance;

import java.util.Set;

public interface StreameshStore {

    void storeDefinition(Definition definition);

    void storeFlowInstance(FlowInstance instance);

    FlowInstance getFlowInstance(String instanceId);

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

    Set<FlowInstance> getAllFlowInstances();
}
