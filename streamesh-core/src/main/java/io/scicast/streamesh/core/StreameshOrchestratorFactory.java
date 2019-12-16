package io.scicast.streamesh.core;

import io.scicast.streamesh.core.internal.InMemoryStreameshOrchestrator;

public class StreameshOrchestratorFactory {


    public StreameshOrchestrator createOrchestrator() {

        return new InMemoryStreameshOrchestrator();

    }
}
