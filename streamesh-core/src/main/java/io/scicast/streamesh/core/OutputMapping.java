package io.scicast.streamesh.core;

import lombok.*;

@Builder
@Getter
@With
@NoArgsConstructor
@AllArgsConstructor
public class OutputMapping {

    private String name;
    private String endpoint;
    private String outputDir;
    private String fileNamePattern;

}
