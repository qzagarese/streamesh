package io.scicast.streamesh.core;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class OutputMapping {

    private OutputLocationType locationType = OutputLocationType.STDOUT;
    private String outputFilePath;

    enum OutputLocationType {

        STDOUT, FILE_SYSTEM;

    }

}
