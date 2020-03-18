package io.scicast.streamesh.core.internal.reflect;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ScannableItem {

    private Object value;
    private String mountedAs;

}
