package io.scicast.streamesh.core;

import io.scicast.streamesh.core.internal.DefaultStreameshOrchestrator;

public class StreameshOrchestratorFactory {


    public StreameshOrchestrator createOrchestrator(String serverIpAddress) {

        return new DefaultStreameshOrchestrator(serverIpAddress);

    }
}
