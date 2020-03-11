package io.scicast.streamesh.core;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class FlowPipe {

    private String as;
    private String type;

    private List<PipeInput> input;
    private List<PipeOutput> output;

}
