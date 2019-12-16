package io.scicast.streamesh.core;

import java.util.Set;

public interface StreameshOrchestrator {

    String applyDefinition(CallableDefinition definition);

    CallableDefinition getDefinition(String id);

    void removeDefinition(String id);

    Set<CallableDefinition> getDefinitions();

    Set<JobDescriptor> getAllJobs();

    Set<JobDescriptor> getJobsByDefinition(String definitionId);

    JobDescriptor scheduleJob(String definitionId, JobInput input);

    OutputHandle getOutput(String jobDescriptorId);
}
