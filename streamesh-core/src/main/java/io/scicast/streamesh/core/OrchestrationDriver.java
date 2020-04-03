package io.scicast.streamesh.core;

import java.io.InputStream;
import java.util.List;
import java.util.function.Consumer;

public interface OrchestrationDriver {

    String retrieveContainerImage(String image);

    TaskDescriptor scheduleTask(TaskExecutionIntent intent, Consumer<TaskExecutionEvent<?>> onUpdate, StreameshContext context);

    InputStream getTaskOutput(String taskId, String outputName);

}
