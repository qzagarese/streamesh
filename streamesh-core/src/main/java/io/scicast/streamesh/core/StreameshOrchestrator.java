package io.scicast.streamesh.core;

import java.io.InputStream;
import java.util.Map;
import java.util.Set;

public interface StreameshOrchestrator {

    String applyDefinition(CallableDefinition definition);

    CallableDefinition getDefinition(String id);

    CallableDefinition getDefinitionByName(String name);

    void removeDefinition(String id);

    Set<CallableDefinition> getDefinitions();

    Set<JobDescriptor> getAllJobs();

    Set<JobDescriptor> getJobsByDefinition(String definitionId);

    JobDescriptor scheduleJob(String definitionId, Map<?, ?> input);

    JobDescriptor getJob(String jobId);

    InputStream getJobOutput(String jobDescriptorId);
}
