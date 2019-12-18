package io.scicast.streamesh.docker.driver.internal;

import com.github.dockerjava.api.DockerClient;

public interface DockerClientProvider {

    DockerClient getClient();

}
