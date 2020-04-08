package io.scicast.streamesh.core.flow.execution;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

@Builder
@Getter
@EqualsAndHashCode
public class RuntimeDataValue {

    @Builder.Default
    private Set<RuntimeDataValuePart> parts = new HashSet<>();

    public enum DataState {
        NOT_STARTED, FLOWING, COMPLETE;

    }
    @Builder
    @Getter
    @EqualsAndHashCode(of = {"refName", "value"})
    public static class RuntimeDataValuePart {


        private String refName;
        private DataState state = DataState.NOT_STARTED;
        private String value;

    }
}
