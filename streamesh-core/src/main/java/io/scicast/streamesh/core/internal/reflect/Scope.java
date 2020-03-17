package io.scicast.streamesh.core.internal.reflect;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.util.List;
import java.util.Map;
import java.util.Queue;

@Builder
@Getter
@With
public class Scope {

    private List<String> path;
    private Object value;

    private Map<String, Scope> structure;
    private Map<String, String> dependencies;



}
