package io.scicast.streamesh.core;

import lombok.*;

@Builder
@Getter
@With
@NoArgsConstructor
@AllArgsConstructor
public class OutputMapping {

    private OutputLocationType locationType = OutputLocationType.STDOUT;
    private String outputDir;
    private String outputFileName;

    public enum OutputLocationType {

        STDOUT, FILE_SYSTEM;

    }

}
