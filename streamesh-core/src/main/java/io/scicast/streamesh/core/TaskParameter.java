package io.scicast.streamesh.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.scicast.streamesh.core.internal.reflect.LocallyScoped;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@Builder
@Getter
@With
@NoArgsConstructor
@AllArgsConstructor
@LocallyScoped(using = "name")
public class TaskParameter {

    private String name;
    private String internalName;
    private boolean optional = true;
    private boolean repeatable;

}
