package io.scicast.streamesh.core;

import io.scicast.streamesh.core.reflect.LocallyScoped;
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

    @LocallyScoped(as = "input")
    private TaskInput inputMapping;

    @LocallyScoped(as = "output")
    private List<TaskOutput> outputMapping;

}
