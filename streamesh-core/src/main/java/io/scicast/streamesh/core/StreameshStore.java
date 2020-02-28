package io.scicast.streamesh.core;

import java.util.Set;

public interface StreameshStore {

    void storeDefinition(Micropipe definition);

    Micropipe getDefinitionById(String id);

    Micropipe getDefinitionByName(String name);

    void remove(String id);

    Set<Micropipe> getAllDefinitions();

    Set<TaskDescriptor> getAllJobs();

    Set<TaskDescriptor> getJobsByDefinition(String definitionId);

    TaskDescriptor getJobById(String jobId);

    void updateJob(String definitionId, TaskDescriptor descriptor);
}
