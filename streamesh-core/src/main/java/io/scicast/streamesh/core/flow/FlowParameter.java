package io.scicast.streamesh.core.flow;


import io.scicast.streamesh.core.internal.reflect.GraphNode;
import io.scicast.streamesh.core.internal.reflect.LocallyScoped;
import lombok.*;

@Getter
@Builder
@With
@NoArgsConstructor
@AllArgsConstructor
@LocallyScoped(using = "name")
@GraphNode(GraphNode.NodeType.SOURCE)
public class FlowParameter {

    private String name;
    private boolean repeatable;
    private boolean optional;

}
