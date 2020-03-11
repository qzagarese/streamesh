package io.scicast.streamesh.core;

import lombok.*;

@Getter
@Builder
@With
@NoArgsConstructor
@AllArgsConstructor
public class FlowOutput {

    private String name;
    private String target;

}
