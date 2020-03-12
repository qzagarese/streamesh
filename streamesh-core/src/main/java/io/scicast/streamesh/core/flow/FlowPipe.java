package io.scicast.streamesh.core.flow;

import io.scicast.streamesh.core.reflect.FlowScoped;
import io.scicast.streamesh.core.reflect.InScope;
import io.scicast.streamesh.core.reflect.LocallyScoped;
import io.scicast.streamesh.core.reflect.impl.DefinitionRefProvider;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
@FlowScoped(using = "as")
public class FlowPipe {

    private String as;

    @LocallyScoped(as = "type", factory = DefinitionRefProvider.class)
    private String type;

    @InScope
    private List<PipeInput> input;

    @LocallyScoped(as = "output")
    private List<PipeOutput> output;

}
