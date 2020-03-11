package io.scicast.streamesh.core;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class FlowParticipant {

    private String name;
    private String type;

}
