package io.scicast.streamesh.core.flow.execution;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OutputAvailabilityDescriptor {

    private String executableId;
    private String nodeName;
    private RuntimeDataValue runtimeDataValue;

}
