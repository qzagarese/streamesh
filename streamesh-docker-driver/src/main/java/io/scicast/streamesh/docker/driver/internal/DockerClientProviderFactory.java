package io.scicast.streamesh.docker.driver.internal;

public class DockerClientProviderFactory {

    private static final DefaultDockerClientProvider INSTANCE = new DefaultDockerClientProvider();

    public static DockerClientProvider create() {
        return INSTANCE;
    }

}
