package io.scicast.streamesh.core;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CmdParameter {

    private String externalName;
    private String internalName;
    private int index = 1;
    private boolean optional = true;
    private ParamType type = ParamType.NAMED;

    enum ParamType {
        POSITIONAL, NAMED
    }

}
