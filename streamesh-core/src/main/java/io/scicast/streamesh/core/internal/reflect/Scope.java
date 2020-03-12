package io.scicast.streamesh.core.internal.reflect;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.util.HashMap;
import java.util.Map;

@Builder
@Getter
@With
public class Scope {

    private Map<String, Object> structure = new HashMap<>();
    private Map<String, String> dependencies = new HashMap<>();


}
