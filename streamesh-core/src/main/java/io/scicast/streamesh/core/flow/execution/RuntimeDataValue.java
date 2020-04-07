package io.scicast.streamesh.core.flow.execution;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Set;

@Builder
@Getter
@EqualsAndHashCode
public class RuntimeDataValue {

    private String refName;
    private Set<RuntimeDataValuePart> parts;


    public enum DataState {
        NOT_STARTED, FLOWING, COMPLETE;
    }

    @Builder
    @Getter
    @EqualsAndHashCode
    public static class RuntimeDataValuePart {

        private DataState state = DataState.NOT_STARTED;
        private String value;

    }
}
