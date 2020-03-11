package io.scicast.streamesh.core;

import lombok.*;

@Getter
@Builder
@With
@NoArgsConstructor
@AllArgsConstructor
public class PipeInput {
    private String target;
    private String value;
    private UsabilityState usable = UsabilityState.WHEN_COMPLETED;

    public enum UsabilityState {
        WHILE_BEING_PRODUCED,
        WHEN_COMPLETED
    }
}
