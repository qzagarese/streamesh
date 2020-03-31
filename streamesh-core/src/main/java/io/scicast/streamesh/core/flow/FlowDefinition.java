package io.scicast.streamesh.core.flow;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.scicast.streamesh.core.Definition;
import io.scicast.streamesh.core.internal.reflect.FlowScoped;
import io.scicast.streamesh.core.internal.reflect.InScope;
import io.scicast.streamesh.core.internal.reflect.LocallyScoped;
import lombok.*;

import java.util.ArrayList;
import java.util.List;




@Getter
@Builder
@With
@NoArgsConstructor
@AllArgsConstructor
@FlowScoped(as = "flow")
public class FlowDefinition implements Definition {

    private final String type = "flow";

    @LocallyScoped(as = "name")
    private String name;

    @LocallyScoped(as = "id")
    private String id;

    @LocallyScoped(as = "output")
    @Builder.Default
    private List<FlowOutput> output = new ArrayList<>();

    @LocallyScoped(as = "input")
    @Builder.Default
    private List<FlowParameter> input = new ArrayList<>();

    @InScope
    @Builder.Default
    private List<FlowPipe> pipes = new ArrayList<>();

    @JsonIgnore
    private FlowGraph graph;

}
