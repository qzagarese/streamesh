package io.scicast.streamesh.core.flow;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.scicast.streamesh.core.TaskDescriptor;
import io.scicast.streamesh.core.flow.execution.ExecutionGraph;
import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.time.LocalDateTime;

@Builder
@Getter
@With
public class FlowInstance {

    private String id;
    private String definitionId;
    private String flowName;
    private LocalDateTime started;
    private LocalDateTime completed;
    private FlowInstanceStatus status;


    @JsonIgnore
    private ExecutionGraph executionGraph;

    public enum FlowInstanceStatus {
        LAUNCHING, RUNNING, COMPLETE
    }

}
