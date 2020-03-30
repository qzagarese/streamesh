package io.scicast.streamesh.core.flow;


import io.scicast.streamesh.core.internal.reflect.InScope;
import io.scicast.streamesh.core.internal.reflect.LocallyScoped;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
@InScope
public class FlowReference {

    @LocallyScoped(as = "input")
    private List<FlowParameterRef> input;

    @LocallyScoped(as = "output")
    private List<FlowOutputRef> output;


}
