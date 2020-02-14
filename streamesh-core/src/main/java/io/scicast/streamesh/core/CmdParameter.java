package io.scicast.streamesh.core;

import lombok.*;

@Builder
@Getter
@With
@NoArgsConstructor
@AllArgsConstructor
public class CmdParameter {

    private String name;
    private String internalName;
    private boolean optional = true;
    private boolean repeatable;

}
