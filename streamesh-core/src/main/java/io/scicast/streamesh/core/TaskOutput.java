package io.scicast.streamesh.core;

import lombok.*;

@Builder
@Getter
@With
@NoArgsConstructor
@AllArgsConstructor
public class TaskOutput {

    private String name;
    private String outputDir;
    private String fileNamePattern;

}
