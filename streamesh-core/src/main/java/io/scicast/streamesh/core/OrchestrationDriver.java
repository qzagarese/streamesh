package io.scicast.streamesh.core;

import java.util.Set;

public interface OrchestrationDriver {

    String apply(SvcDescriptor descriptor);

    Set<SvcDescriptor> getClusterState();

    SvcDescriptor getDescriptor(String id);

    Set<JobDescriptor> getJobs();

    JobDescriptor scheduleJob(String svcDescriptorId, InputDescriptor input);

    OutputHandle getOutput(String jobDescriptorId);
}
