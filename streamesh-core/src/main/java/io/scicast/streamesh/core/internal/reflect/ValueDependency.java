package io.scicast.streamesh.core.internal.reflect;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class ValueDependency {

    private String attribute;
    private List<String> path;

    @Setter
    @Getter
    @JsonIgnore
    private Object resolvedTargetValue;

    @Builder.Default
    private List<Class<?>> expectedTargetTypes = new ArrayList<>();

    public String getStringifiedPath() {
        return path.stream().collect(Collectors.joining("."));
    }

}
