package io.scicast.streamesh.core.flow;

import io.scicast.streamesh.core.internal.reflect.GraphNode;
import io.scicast.streamesh.core.internal.reflect.LocallyScoped;
import io.scicast.streamesh.core.internal.reflect.Resolvable;
import lombok.*;

@Getter
@Builder
@With
@NoArgsConstructor
@AllArgsConstructor
@LocallyScoped(using = "name")
@GraphNode(GraphNode.NodeType.SINK)
public class FlowOutput {

    private String name;

    @Resolvable(scope = "root")
    private String target;

}
