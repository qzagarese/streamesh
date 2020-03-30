package io.scicast.streamesh.core.flow;

import io.scicast.streamesh.core.TaskParameter;
import io.scicast.streamesh.core.internal.reflect.LocallyScoped;
import io.scicast.streamesh.core.internal.reflect.Resolvable;
import lombok.*;

@Getter
@Builder
@With
@NoArgsConstructor
@AllArgsConstructor
@LocallyScoped(randomName = true)
public class PipeInput {

    @Resolvable(scope = "parent.type.input", expectsAnyOf = { TaskParameter.class, FlowParameterRef.class })
    private String target;

    @Resolvable(scope = "root", expectsAnyOf = { FlowParameter.class, PipeOutput.class })
    private String value;
    private UsabilityState usable = UsabilityState.WHEN_COMPLETED;

    public enum UsabilityState {
        WHILE_BEING_PRODUCED,
        WHEN_COMPLETED
    }
}
