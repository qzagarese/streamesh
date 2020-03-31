package io.scicast.streamesh.core.flow;

import io.scicast.streamesh.core.TaskOutput;
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

    @Resolvable(scope = "root", expectsAnyOf = { FlowParameter.class, PipeOutput.class },
        dataFlow = Resolvable.DataFlowDirection.INCOMING)
    private String target;

}
