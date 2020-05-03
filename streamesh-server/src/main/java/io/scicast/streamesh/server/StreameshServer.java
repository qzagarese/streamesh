package io.scicast.streamesh.server;

import io.scicast.streamesh.core.StreameshOrchestrator;
import io.scicast.streamesh.core.StreameshOrchestratorFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.util.Collections;

@SpringBootApplication
public class StreameshServer {

    private static final String STREAMESH_SERVER_NETWORK_INTERFACE = "streamesh.server.network.interface";
    private static String serverAddress;

    public static void main(String[] args) throws IOException {
        serverAddress = System.getProperty(StartupUtils.STREAMESH_SERVER_ADDRESS_PROPERTY);
        if (serverAddress == null) {
            serverAddress = Collections.list(NetworkInterface.getNetworkInterfaces()).stream()
                    .filter(ni -> ni.getDisplayName().equals(System.getProperty(STREAMESH_SERVER_NETWORK_INTERFACE)))
                    .map(ni -> Collections.list(ni.getInetAddresses()).stream()
                            .filter(inetAddress -> inetAddress instanceof Inet4Address)
                            .map(inetAddress -> inetAddress.getHostAddress())
                            .findFirst()
                            .orElse(null))
                    .findFirst()
                    .orElse(null);
        }
        if (serverAddress == null) {
            serverAddress = StartupUtils.selectAddress();
        }
        SpringApplication.run(StreameshServer.class, args);
    }

    @Bean
    public StreameshOrchestrator provideOrchestrator() {
        return new StreameshOrchestratorFactory().createOrchestrator(serverAddress);
    }
}
