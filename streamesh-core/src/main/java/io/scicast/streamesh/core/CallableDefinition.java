package io.scicast.streamesh.core;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CallableDefinition {

    private String image;
    private int maxConcurrentJobs = 5;
    private InputMapping inputMapping;
    private OutputMapping outputMapping;

}
