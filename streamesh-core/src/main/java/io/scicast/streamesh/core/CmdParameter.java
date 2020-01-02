package io.scicast.streamesh.core;

import lombok.*;

@Builder
@Getter
@With
@NoArgsConstructor
@AllArgsConstructor
public class CmdParameter {

    private String externalName;
    private String internalName;
    private boolean optional = true;
    private boolean repeatable;

}
