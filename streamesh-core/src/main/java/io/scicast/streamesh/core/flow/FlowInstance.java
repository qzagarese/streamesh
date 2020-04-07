package io.scicast.streamesh.core.flow;

import io.scicast.streamesh.core.flow.execution.ExecutionGraph;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class FlowInstance {

    private String id;
    private String definitionId;
    private String flowName;
    private ExecutionGraph executionGraph;

}
