package io.scicast.streamesh.core;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class StreameshContext {

    private StreameshStore store;
    private StreameshOrchestrator orchestrator;
    private OrchestrationDriver orchestrationDriver;
    private String streameshServerAddress;

}
