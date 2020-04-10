package io.scicast.streamesh.core;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class TaskExecutionIntent {

    private String taskId;
    private String image;
    private String command;
    private List<TaskOutput> taskOutputs;

}
