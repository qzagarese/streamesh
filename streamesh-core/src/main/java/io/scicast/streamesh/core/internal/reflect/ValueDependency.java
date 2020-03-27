package io.scicast.streamesh.core.internal.reflect;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ValueDependency {

    private String attribute;
    private List<String> path;

}
