package io.scicast.streamesh.core;

import java.util.Set;

public interface StreameshStore {

    void storeDefinition(Definition definition);

    Definition getDefinitionById(String id);

    Definition getDefinitionByName(String name);

    void remove(String id);

    Set<Definition> getAllDefinitions();

    Set<TaskDescriptor> getAllJobs();

    Set<TaskDescriptor> getJobsByDefinition(String definitionId);

    TaskDescriptor getJobById(String jobId);

    void updateJob(String definitionId, TaskDescriptor descriptor);
}
