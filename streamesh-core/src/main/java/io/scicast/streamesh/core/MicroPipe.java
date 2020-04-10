package io.scicast.streamesh.core;

import io.scicast.streamesh.core.internal.reflect.GraphNode;
import io.scicast.streamesh.core.internal.reflect.InScope;
import io.scicast.streamesh.core.internal.reflect.LocallyScoped;
import io.scicast.streamesh.core.internal.reflect.handler.MicroPipeGraphNodeHandler;
import lombok.*;

import java.util.List;

@Builder
@With
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")

@InScope
@GraphNode(value = GraphNode.NodeType.INTERNAL, handler = MicroPipeGraphNodeHandler.class)
public class MicroPipe implements Definition {

    private String id;
    private String name;
    private String type;
    private String image;
    private String imageId;
    private String description;

    @LocallyScoped(as = "output")
    private List<TaskOutput> outputMapping;

    @LocallyScoped(as = "input")
    private TaskInput inputMapping;

}
