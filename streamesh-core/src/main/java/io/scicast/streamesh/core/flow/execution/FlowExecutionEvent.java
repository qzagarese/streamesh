package io.scicast.streamesh.core.flow.execution;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FlowExecutionEvent<T> {

    private T descriptor;
    private EventType type = EventType.OUTPUT_AVAILABILITY;

    public enum EventType {
        OUTPUT_AVAILABILITY;
    }

}
