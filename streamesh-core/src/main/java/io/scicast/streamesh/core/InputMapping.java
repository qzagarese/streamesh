package io.scicast.streamesh.core;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@With
@NoArgsConstructor
@AllArgsConstructor
public class InputMapping {

    private String baseCmd;
    private List<CmdParameter> parameters = new ArrayList<>();

}
