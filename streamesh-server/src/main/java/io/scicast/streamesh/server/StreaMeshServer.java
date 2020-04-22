package io.scicast.streamesh.server;

import io.scicast.streamesh.core.StreameshOrchestrator;
import io.scicast.streamesh.core.StreameshOrchestratorFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.net.NetworkInterface;

@SpringBootApplication
public class StreaMeshServer {

    private static String serverAddress;

    public static void main(String[] args) throws IOException {
        serverAddress = System.getProperty(StartupUtils.STREAMESH_SERVER_ADDRESS_PROPERTY);
        if (serverAddress == null) {
            serverAddress = StartupUtils.selectAddress();
        }
        SpringApplication.run(io.scicast.streamesh.server.StreaMeshServer.class, args);
    }

    @Bean
    public StreameshOrchestrator provideOrchestrator() {
        return new StreameshOrchestratorFactory().createOrchestrator(serverAddress);
    }
}
