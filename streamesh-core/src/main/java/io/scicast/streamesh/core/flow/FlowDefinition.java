package io.scicast.streamesh.core.flow;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.scicast.streamesh.core.Definition;
import io.scicast.streamesh.core.internal.reflect.FlowScoped;
import io.scicast.streamesh.core.internal.reflect.InScope;
import io.scicast.streamesh.core.internal.reflect.LocallyScoped;
import lombok.*;

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

    @LocallyScoped(as = "input")
    private List<FlowParameter> input;

    @LocallyScoped(as = "output")
    private List<FlowOutput> output;

    @InScope
    private List<FlowPipe> pipes;

    @JsonIgnore
    private FlowGraph graph;

}
