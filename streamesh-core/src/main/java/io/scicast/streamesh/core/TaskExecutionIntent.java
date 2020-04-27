package io.scicast.streamesh.core;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Builder
@Getter
public class TaskExecutionIntent {

    private String taskId;
    private String image;
    private TaskInput taskInput;
    private List<TaskOutput> taskOutputs;
    private Map<?, ?> runtimeInput;

}
