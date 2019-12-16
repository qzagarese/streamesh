package io.scicast.streamesh.core;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class InputMapping {

    private String baseCmd;
    private List<CmdParameter> parameters;

}
