package io.scicast.streamesh.core;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class FlowInstance {

    private String id;
    private String definitionId;
    private ExecutionGraph executionGraph;

}
