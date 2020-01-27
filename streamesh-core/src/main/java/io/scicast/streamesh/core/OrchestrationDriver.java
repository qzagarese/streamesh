package io.scicast.streamesh.core;

import java.io.InputStream;
import java.util.function.Consumer;

public interface OrchestrationDriver {

    String retrieveContainerImage(String image);

    JobDescriptor scheduleJob(String image, String command, OutputMapping outputMapping, Consumer<JobDescriptor> onStatusUpdate);

    InputStream getJobOutput(String jobId);

    void setStreameshServerAddress(String ipAddress);
}
