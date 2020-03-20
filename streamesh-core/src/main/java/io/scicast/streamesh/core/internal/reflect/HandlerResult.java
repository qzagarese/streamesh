package io.scicast.streamesh.core.internal.reflect;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class HandlerResult {

    private List<String> targetMountPoint;
    private Object targetValue;
    private Scope resultScope;

}
