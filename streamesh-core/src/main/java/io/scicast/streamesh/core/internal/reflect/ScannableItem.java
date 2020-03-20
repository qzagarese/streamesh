package io.scicast.streamesh.core.internal.reflect;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class ScannableItem {

    private Object value;
    private List<String> mountPoint;
    private Multiplicity multiplicity = Multiplicity.SINGLE;

    enum Multiplicity {
        SINGLE, MULTIPLE;
    }

}
