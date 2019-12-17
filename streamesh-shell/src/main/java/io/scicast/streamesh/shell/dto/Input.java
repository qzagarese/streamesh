package io.scicast.streamesh.shell.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Input {

    private String baseCmd;
    private List<Parameter> parameters;

}
