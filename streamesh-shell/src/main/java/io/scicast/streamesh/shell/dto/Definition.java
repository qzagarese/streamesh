package io.scicast.streamesh.shell.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Definition {

    private String id;
    private String name;
    private String image;
    private String imageId;
    private String description;
    private int maxConcurrentJobs ;
    private Input inputMapping;
    private Output outputMapping;

}
