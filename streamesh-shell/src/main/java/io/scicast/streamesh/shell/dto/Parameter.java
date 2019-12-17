package io.scicast.streamesh.shell.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Parameter {

    private String externalName;
    private String internalName;
    private int index = 1;
    private boolean optional = true;
    private ParamType type = ParamType.NAMED;

    enum ParamType {
        POSITIONAL, NAMED
    }

}
