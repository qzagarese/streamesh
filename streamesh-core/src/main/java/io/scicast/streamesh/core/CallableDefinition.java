package io.scicast.streamesh.core;

import lombok.*;

@Builder
@With
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CallableDefinition {

    private String id;
    private String name;
    private String image;
    private String imageId;
    private String description;
    private int maxConcurrentJobs = 5;
    private InputMapping inputMapping;
    private OutputMapping outputMapping;

}
