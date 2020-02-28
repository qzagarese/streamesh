package io.scicast.streamesh.shell.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Output {

    private String name;
    private String outputDir;
    private String fileNamePattern;
}
