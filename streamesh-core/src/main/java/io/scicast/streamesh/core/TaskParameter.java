package io.scicast.streamesh.core;

import lombok.*;

@Builder
@Getter
@With
@NoArgsConstructor
@AllArgsConstructor
public class TaskParameter {

    private String name;
    private String internalName;
    private boolean optional = true;
    private boolean repeatable;

}
