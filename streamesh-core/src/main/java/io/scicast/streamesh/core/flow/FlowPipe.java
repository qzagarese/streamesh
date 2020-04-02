package io.scicast.streamesh.core.flow;

import io.scicast.streamesh.core.internal.reflect.FlowScoped;
import io.scicast.streamesh.core.internal.reflect.LocallyScoped;
import io.scicast.streamesh.core.internal.reflect.handler.DefinitionRefProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@FlowScoped(using = "as")
public class FlowPipe {

    public static final String INPUT_SCOPE_PATH = "input";
    private String as;

    @LocallyScoped(as = "type", factory = DefinitionRefProvider.class)
    private String type;

    @LocallyScoped(as = INPUT_SCOPE_PATH)
    private List<PipeInput> input;

    @LocallyScoped(as = "output")
    private List<PipeOutput> output;

}
