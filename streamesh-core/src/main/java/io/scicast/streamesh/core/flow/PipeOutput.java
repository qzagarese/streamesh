package io.scicast.streamesh.core.flow;

import io.scicast.streamesh.core.TaskOutput;
import io.scicast.streamesh.core.internal.reflect.LocallyScoped;
import io.scicast.streamesh.core.internal.reflect.Resolvable;
import lombok.*;

@Getter
@Builder
@With
@NoArgsConstructor
@AllArgsConstructor
@LocallyScoped(using = "as")
public class PipeOutput {

    @Resolvable(scope = "parent.parent.type.output", expectsAnyOf = { TaskOutput.class, FlowOutputRef.class })
    private String target;

    private String as;

}
