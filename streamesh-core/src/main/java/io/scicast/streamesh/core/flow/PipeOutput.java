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
@LocallyScoped(using = "as")
@GraphNode(GraphNode.NodeType.INTERNAL)
public class PipeOutput {

    @Resolvable(scope = "parent.parent.type.output",
            expectsAnyOf = { TaskOutput.class, FlowOutputRef.class },
            dataFlow = Resolvable.DataFlowDirection.INCOMING)
    private String target;

    private String as;

}
