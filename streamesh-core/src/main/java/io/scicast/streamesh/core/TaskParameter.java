package io.scicast.streamesh.core;

import io.scicast.streamesh.core.internal.reflect.GraphNode;
import io.scicast.streamesh.core.internal.reflect.LocallyScoped;
import lombok.*;

@Builder
@Getter
@With
@NoArgsConstructor
@AllArgsConstructor
@LocallyScoped(using = "name")
public class TaskParameter {

    private String name;
    private String internalName;
    private boolean optional = true;
    private boolean repeatable;

}
