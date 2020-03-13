package io.scicast.streamesh.core.internal.reflect;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.util.Map;

@Builder
@Getter
@With
public class Scope {

    private Map<String, Object> structure;
    private Map<String, String> dependencies;


}
