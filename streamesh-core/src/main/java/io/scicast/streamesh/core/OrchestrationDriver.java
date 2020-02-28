package io.scicast.streamesh.core;

import java.io.InputStream;
import java.util.List;
import java.util.function.Consumer;

public interface OrchestrationDriver {

    String retrieveContainerImage(String image);

    TaskDescriptor scheduleTask(String image, String command, List<OutputMapping> outputMapping, Consumer<TaskDescriptor> onStatusUpdate);

    InputStream getTaskOutput(String taskId, String outputName);

    void setStreameshServerAddress(String ipAddress);
}
