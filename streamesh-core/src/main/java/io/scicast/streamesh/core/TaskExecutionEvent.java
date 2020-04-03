package io.scicast.streamesh.core;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class TaskExecutionEvent<T> {

    private T descriptor;
    private EventType type = EventType.CONTAINER_STATE_CHANGE;

    public enum EventType {
        CONTAINER_STATE_CHANGE, OUTPUT_AVAILABILITY
    }
}
