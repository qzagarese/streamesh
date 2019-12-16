package io.scicast.streamesh.server;

import io.scicast.streamesh.core.StreameshOrchestrator;
import io.scicast.streamesh.core.StreameshOrchestratorFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class StreaMeshServer {

    public static void main(String[] args) {
        SpringApplication.run(StreaMeshServer.class, args);
    }

    @Bean
    public StreameshOrchestrator provideOrchestrator() {
        return new StreameshOrchestratorFactory().createOrchestrator();
    }
}
