package io.scicast.streamesh.core.flow;

import io.scicast.streamesh.core.reflect.LocallyScoped;
import lombok.*;

@Getter
@Builder
@With
@NoArgsConstructor
@AllArgsConstructor
@LocallyScoped(using = "as")
public class PipeOutput {

    private String target;
    private String as;

}
