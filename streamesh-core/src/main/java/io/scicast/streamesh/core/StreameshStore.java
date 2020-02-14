package io.scicast.streamesh.core;

import java.util.Set;

public interface StreameshStore {

    void storeDefinition(Micropipe definition);

    Micropipe getDefinitionById(String id);

    Micropipe getDefinitionByName(String name);

    void remove(String id);

    Set<Micropipe> getAllDefinitions();

    Set<JobDescriptor> getAllJobs();

    Set<JobDescriptor> getJobsByDefinition(String definitionId);

    JobDescriptor getJobById(String jobId);

    void updateJob(String definitionId, JobDescriptor descriptor);
}
