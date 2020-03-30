package io.scicast.streamesh.core.flow;

import io.scicast.streamesh.core.internal.reflect.LocallyScoped;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@LocallyScoped(using = "name")
public class FlowParameterRef {

    private String name;
    private boolean repeatable;
    private boolean optional;

}
