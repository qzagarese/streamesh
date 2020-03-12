package io.scicast.streamesh.core.flow;

import io.scicast.streamesh.core.internal.reflect.LocallyScoped;
import lombok.*;

@Getter
@Builder
@With
@NoArgsConstructor
@AllArgsConstructor
@LocallyScoped(using = "name")
public class FlowOutput {

    private String name;
    private String target;

}
