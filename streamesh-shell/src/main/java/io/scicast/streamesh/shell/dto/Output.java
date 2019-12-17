package io.scicast.streamesh.shell.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Output {


    private OutputLocationType locationType = OutputLocationType.STDOUT;
    private String outputFilePath;

    enum OutputLocationType {

        STDOUT, FILE_SYSTEM;

    }

}
