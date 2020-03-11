package io.scicast.streamesh.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.scicast.streamesh.core.flow.FlowGraph;
import lombok.*;

import java.util.List;

@Getter
@Builder
@With
@NoArgsConstructor
@AllArgsConstructor
public class FlowDefinition implements Definition{

    private String name;
    private String id;
    private final String type = "flow";

    private List<FlowParameter> input;

    private List<FlowOutput> output;

    private List<FlowPipe> pipes;

    @JsonIgnore
    private FlowGraph graph;

}
