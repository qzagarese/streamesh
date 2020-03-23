package io.scicast.streamesh.core.flow;

import io.scicast.streamesh.core.internal.reflect.LocallyScoped;
import io.scicast.streamesh.core.internal.reflect.Resolvable;
import lombok.*;

@Getter
@Builder
@With
@NoArgsConstructor
@AllArgsConstructor
@LocallyScoped(using = "name")
public class FlowOutput {

    private String name;

    @Resolvable(scope = "root")
    private String target;

}
