package io.scicast.streamesh.core;

import lombok.*;

@Getter
@Builder
@With
@NoArgsConstructor
@AllArgsConstructor
public class PipeOutput {

    private String target;
    private String as;

}
