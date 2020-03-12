package io.scicast.streamesh.core;

import io.scicast.streamesh.core.reflect.LocallyScoped;
import lombok.*;

@Builder
@Getter
@With
@NoArgsConstructor
@AllArgsConstructor
@LocallyScoped(using = "name")
public class TaskOutput {

    private String name;
    private String outputDir;
    private String fileNamePattern;

}
