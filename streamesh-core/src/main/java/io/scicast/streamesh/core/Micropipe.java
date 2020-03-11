package io.scicast.streamesh.core;

import lombok.*;

import java.util.List;

@Builder
@With
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Micropipe implements  Definition {

    private String id;
    private String name;
    private final String type = "micropipe";
    private String image;
    private String imageId;
    private String description;
    private TaskInput inputMapping;
    private List<TaskOutput> outputMapping;

}
