package io.scicast.streamesh.docker.driver.internal;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ReadResult {
    private int readBytes;
    private byte[] buffer;
}