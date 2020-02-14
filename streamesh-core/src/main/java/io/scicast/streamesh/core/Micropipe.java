package io.scicast.streamesh.core;

import lombok.*;

import java.util.List;

@Builder
@With
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Micropipe {

    private String id;
    private String name;
    private String image;
    private String imageId;
    private String description;
    private InputMapping inputMapping;
    private List<OutputMapping> outputMapping;

}
